package org.flyboy.bells.tower;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Layer with operations for the player.
 *
 * @author John J. Franey
 */
@ApplicationScoped
public class Belltower {
    private static final Logger logger = LoggerFactory.getLogger(Belltower.class);

    // in seconds
    @ConfigProperty(name = "belltower.peal.duration.default", defaultValue = "60")
    long defaultPealDuration;

    @Inject
    LinuxMPC linuxMPC;
    @Inject
    RepeatTimer repeatTimer;

    private Boolean isLocked = false;

    /**
     * returns Uni which returns simple status of this player.
     *
     * @return status
     * @see LinuxMPC
     */
    public Uni<BelltowerStatus> getStatus() {
        return linuxMPC.mpc("status")
                .onItem().transform(this::getBelltowerStatus);
    }


    /*
    1) check locked, get status and list of songs from mpd
    2) check status, select songs from list, then clear, add, play, get lengths, status from mpd
    3) if repeats, set timers, return status
     */
    @SuppressWarnings("ReactiveStreamsThrowInOperator")
    public Uni<BelltowerStatus> ring(String name) {
        return Uni.createFrom().nullItem()
                .onItem().transform(nullItem -> {
                    // not available if locked.
                    if (isLocked) {
                        throw new BelltowerUnavailableException("Belltower is locked.");
                    }
                    return null;
                })

                .onItem().transformToUni(n -> {
                    // get list of songs and status of the player
                    return linuxMPC.mpc("lsinfo", "status");
                })

                .onItem().transform(response -> {
                    // not available if already busy playing a bell sample
                    String state = MpdResponse.getField(response, "state")
                            .orElseThrow(() -> new IllegalArgumentException("state is not returned"));

                    if (state.equals("play")) {
                        throw new BelltowerUnavailableException("Belltower is busy.");
                    }
                    return response;
                })

                .onItem().transform(response -> {

                    List<String> commands = new ArrayList<>();
                    commands.add("clear");
                    commands.add("crossfade 0");

                    MpdMetadata mpdMetadata = new MpdMetadata(response);
                    List<MpdMetadata.Song> songs = mpdMetadata.findMatch(name);
                    switch (songs.size()) {
                        case 1 -> commands.add("add " + songs.get(0).filename());
                        case 3 -> {
                            commands.add("add " + songs.get(0).filename());
                            commands.add("add " + songs.get(1).filename());
                            commands.add("add " + songs.get(2).filename());
                            repeatTimer.start(songs, defaultPealDuration * 1000);
                        }
                        default -> {
                            logger.warn("sample not found, name={}, songs={}", name, songs);
                            throw new BelltowerSampleNotFoundException(name);
                        }
                    }

                    commands.add("play");
                    commands.add("status");
                    return commands;
                })

                .onItem().transformToUni(commands -> {
                    logger.debug("sending mpc commands: {}", commands);
                    return linuxMPC.mpc(commands);
                })

                .onItem().transform(response -> {
                    // check if success
                    if (!isOk(response)) {
                        repeatTimer.stop();

                        MpdResponse.Ack ack = ack(response);
                        final int sampleNotFoundError = 50;

                        // if error indicates the named sample is unknown
                        if (ack.getError() == sampleNotFoundError) {
                            throw new BelltowerSampleNotFoundException(name);
                        } else {
                            throw new BelltowerException("Failed to play sample, error="
                                    + ack.getError() + ", text=" + ack.getMessageText());
                        }
                    } else {
                        return getBelltowerStatus(response);
                    }
                });
    }

    public void lock() {
        isLocked = true;
    }

    public void unlock() {
        isLocked = false;
    }

    private BelltowerStatus getBelltowerStatus(List<String> result) {
        String state = MpdResponse.getField(result, "state").orElse("unknown");

        return new BelltowerStatus(isLocked, state);
    }

    private boolean isOk(List<String> result) {
        return MpdResponse.isOk(result);
    }

    private MpdResponse.Ack ack(List<String> result) {
        return MpdResponse.getAck(result);
    }
}
