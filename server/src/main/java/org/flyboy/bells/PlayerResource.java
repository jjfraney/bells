package org.flyboy.bells;

import io.smallrye.mutiny.Uni;
import org.flyboy.bells.player.PlayerService;
import org.flyboy.bells.player.Status;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Resource offering controls of the music player.
 * @author John J. Franey
 */
@Path("/player")                        // <1>
public class PlayerResource {
    @Inject
    PlayerService playerService;

    @GET
    @Path("/status")
    public Uni<Status> getStatus() {
        return playerService.getStatus();
    }



    @DELETE
    @Path("/lock")
    public Uni<Status> unlock() {
        playerService.unlock();
        return playerService.getStatus();
    }
    @POST
    @Path("/lock")
    public Uni<Status> lock() {
        playerService.lock();
        return playerService.getStatus();
    }
}
