package org.flyboy.bells.ringer;

import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Layer with operations for the player.
 * @author John J. Franey
 */
@ApplicationScoped
public class BellRinger {
    private Boolean isLocked = false;


    @Inject
    LinuxMPC linuxMPC;

    /**
     * returns Uni which returns simple status of this player.
     * @see LinuxMPC
     * @return status
     */
    public Uni<Status> getStatus() {
        musicpd.protocol.Status cmd = new musicpd.protocol.Status();
        return linuxMPC.mpc(cmd)
                .onItem().transform(r -> {
                            Status res = new Status();
                            res.setState(r.getState().orElse("unknown"));
                            res.setLocked(isLocked);
                            return res;
                        });
    }


    public void lock() {
        isLocked = true;
    }
    public void unlock() {
        isLocked = false;
    }

}
