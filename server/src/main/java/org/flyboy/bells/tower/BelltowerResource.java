package org.flyboy.bells.tower;

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
@Path("/belltower")
public class BelltowerResource {
    @Inject
    Belltower belltower;

    @GET
    @Path("/status")
    public Uni<Status> getStatus() {
        return belltower.getStatus();
    }


    @DELETE
    @Path("/lock")
    public Uni<Status> unlock() {
        belltower.unlock();
        return belltower.getStatus();
    }
    @POST
    @Path("/lock")
    public Uni<Status> lock() {
        belltower.lock();
        return belltower.getStatus();
    }
}
