package org.flyboy.bells.player;

import com.github.jjfraney.mpc.Command;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Layer with operations for the player.
 * @author John J. Franey
 */
@ApplicationScoped
public class PlayerService {
    private Boolean isLocked = false;


    @Inject
    MpdService mpdService;

    /**
     * returns status of the player and status of mpd.
     * @return status
     */
    public Uni<Status> getStatus() {
        musicpd.protocol.Status cmd = new musicpd.protocol.Status();
        return toMpd(cmd)
                .onItem().transform(r -> {
                            Status res = new Status();
                            res.setMpdStatus(r);
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

    /**
     * creates a uni to send command to mpd and obtain and parse response
     * @param command to send
     * @return response from mpd
     */
    private <R extends Command.Response> Uni<R> toMpd(Command<R> command) {
        return mpdService.mpd(command.text())
                .onItem().transform(list -> command.response(list.subList(1, list.size() -1), list.get(0)));
    }
}
