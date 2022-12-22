package org.flyboy.bells.tower;

import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * Layer with operations for the player.
 *
 * @author John J. Franey
 */
@ApplicationScoped
public class Belltower {
    private Boolean isLocked = false;


    @Inject
    LinuxMPC linuxMPC;

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


    @SuppressWarnings("ReactiveStreamsThrowInOperator")
    public Uni<BelltowerStatus> ring(String name) {
        return Uni.createFrom().item(isLocked)

                .onItem().transformToUni(locked -> {
                    // not available if locked.
                    if (locked) {
                        throw new BelltowerUnavailableException("Belltower is locked.");
                    }

                    // get status of the player
                    return linuxMPC.mpc("status");
                })

                .onItem().transformToUni(response -> {
                    // not available if already busy playing a bell sample
                    String state = MpdResponse.getField(response, "state")
                            .orElseThrow(() -> new IllegalArgumentException("state is not returned"));

                    if (state.equals("play")) {
                        throw new BelltowerUnavailableException("Belltower is busy.");
                    }

                    // play the sample
                    String commandList = String.join("\n",
                            "command_list_ok_begin",
                            "clear",
                            "crossfade 0",
                            "add " + name,
                            "play",
                            "status",
                            "command_list_end"
                    );
                    return linuxMPC.mpc(commandList);
                })
                .onItem().transform(response -> {
                    // check if success
                    if (!isOk(response)) {
                        MpdResponse.Ack ack = ack(response);
                        final int sampleNotFoundError = 50;

                        // if error indicates the named sample is unknown
                        if(ack.getError() == sampleNotFoundError) {
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

        BelltowerStatus res = new BelltowerStatus();
        res.setState(state);
        res.setLocked(isLocked);
        return res;
    }
    private boolean isOk(List<String> result) {
        return MpdResponse.isOk(result);
    }
    private MpdResponse.Ack ack(List<String> result) {
        return MpdResponse.getAck(result);
    }
}
