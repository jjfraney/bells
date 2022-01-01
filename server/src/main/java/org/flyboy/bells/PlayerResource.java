package org.flyboy.bells;

import io.smallrye.mutiny.Uni;
import org.flyboy.bells.player.PlayerService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/bells")                        // <1>
public class PlayerResource {

    @Inject
    PlayerService service;

    @GET
    @Path("/mpd/{cmd}")
    public Uni<String> mpd(@PathParam("cmd") String cmd) {
        return service.mpd(cmd);
    }
}
