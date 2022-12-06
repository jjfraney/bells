package org.flyboy.bells.ringer;

import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Resource offering controls of the music player.
 * @author John J. Franey
 */
@Path("/bell/ringer")
public class BellRingerResource {
    @Inject
    BellRinger bellRinger;

    @GET
    @Path("/status")
    public Uni<Status> getStatus() {
        return bellRinger.getStatus();
    }



    @DELETE
    @Path("/lock")
    public Uni<Status> unlock() {
        bellRinger.unlock();
        return bellRinger.getStatus();
    }
    @POST
    @Path("/lock")
    public Uni<Status> lock() {
        bellRinger.lock();
        return bellRinger.getStatus();
    }
}
