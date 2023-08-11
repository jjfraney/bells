package org.flyboy.bells.belfry;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Layer with operations for the player.
 *
 * @author John J. Franey
 */
@ApplicationScoped
public class Belltower {

    // in seconds
    @ConfigProperty(name = "belltower.peal.duration.default", defaultValue = "60")
    long defaultPealDuration;

    static final List<String> MPD_PRE_PLAY_STATUS = List.of("lsinfo", "status");
    @Inject
    RepeatTimer repeatTimer;

    private Boolean isLocked = false;
    @Inject
    Mpd mpd;

    /**
     * creates list of commands to play the named bell songs.
     *
     * @param songs to play
     * @return list of MPD commands.
     */
    static List<String> mpdRingSongCommands(List<MpdMetadata.Song> songs) {
        List<String> adds = songs.stream().map(s -> "add " + s.filename()).toList();
        return Stream
                .of(List.of("clear"), adds, List.of("play", "status"))
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * returns Uni which returns simple status of this player.
     *
     * @return status
     * @see Mpd
     */
    public Uni<BellStatus> getStatus() {
        return mpd.send("status")
                .onItem().transform(this::getBelltowerStatus);
    }

    /*
    1) check locked, get status and list of songs from mpd
    2) check status, select songs from list, then clear, add, play, get lengths, status from mpd
    3) if repeats, set timers, return status
     */
    @SuppressWarnings("ReactiveStreamsThrowInOperator")
    public Uni<BellStatus> ring(String name) {
        return Uni.createFrom().nullItem()
                .onItem().transform(nullItem -> {
                    // not available if locked.
                    if (isLocked) {
                        throw new BellsUnavailableException("Belfry is locked.");
                    }
                    return null;
                })

                .onItem().transformToUni(n -> {
                    // get list of songs and status of the player
                    return mpd.send(MPD_PRE_PLAY_STATUS);
                })

                .onItem().transform(response -> {
                    // belltower is not available if already busy playing a bell sample
                    String state = MpdResponse.getField(response, "state")
                            .orElseThrow(() -> new IllegalArgumentException("state is not returned"));

                    if (state.equals("play")) {
                        throw new BellsUnavailableException("Belfry is busy.");
                    }
                    return response;
                })

                .onItem().transformToUni(response -> {
                    MpdMetadata mpdMetadata = new MpdMetadata(response);
                    List<MpdMetadata.Song> songs = mpdMetadata.findMatch(name);
                    switch (songs.size()) {
                        case 1 -> {

                            final List<String> mpdCommands = mpdRingSongCommands(songs);
                            return mpd.send(mpdCommands);
                        }

                        case 3 -> {
                            List<String> mpdCommands = mpdRingSongCommands(songs);
                            return mpd.send(mpdCommands)
                                    .onItem().transform(r -> {
                                        // skipped if error when starting the player (per Mutiny behavior)
                                        repeatTimer.start(songs, defaultPealDuration * 1000);
                                        return r;
                                    });
                        }
                        default -> {
                            return Uni.createFrom().failure(() -> new BellPatternNotFoundException(name));
                        }
                    }
                })
                .onItem().transform(this::getBelltowerStatus)

                .onFailure(MpdCommandException.class).transform(thrown -> {
                    // Note: this error implies the repeat timer is not running.

                    MpdResponse.Ack ack = ((MpdCommandException) thrown).getAck();
                    final int sampleNotFoundError = 50;
                    if (ack.getError() == sampleNotFoundError) {
                        return new BellPatternNotFoundException(name);
                    } else {
                        return new BelfryException("error="
                                + ack.getError() + ", text=" + ack.getMessageText());
                    }
                })

                ;

    }

    public Uni<BellStatus> stop() {
        return Uni.createFrom().nullItem()
                .onItem().invoke(() -> repeatTimer.stop())
                .onItem().transformToUni(o -> getStatus());
    }

    public Uni<BellStatus> lock() {
        isLocked = true;
        return getStatus();
    }

    public Uni<BellStatus> unlock() {
        isLocked = false;
        return getStatus();
    }

    private BellStatus getBelltowerStatus(List<String> result) {
        String state = MpdResponse.getField(result, "state").orElse("unknown");

        return new BellStatus(isLocked, state);
    }
}
