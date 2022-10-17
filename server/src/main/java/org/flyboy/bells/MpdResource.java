package org.flyboy.bells;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import musicpd.protocol.Status;
import org.flyboy.bells.player.MpdService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/mpd")                        // <1>
public class MpdResource {

    @Inject
    MpdService service;

    @GET
    public Multi<String> mpc(@QueryParam("cmd") String cmd) {
        return service.mpc(cmd)
                .onItem().transformToMulti(list -> Multi.createFrom().items(list.stream()))
                .onItem().transform(s -> s + "\n");
    }

    @GET
    @Path("/status")
    public Uni<Status.Response> status() {
        return service.mpc(new Status());
    }
}
