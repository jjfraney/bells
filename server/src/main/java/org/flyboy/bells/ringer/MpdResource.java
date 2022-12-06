package org.flyboy.bells.ringer;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import musicpd.protocol.Status;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.ConnectException;

@Produces(MediaType.APPLICATION_JSON)
@Path("/mpd")                        // <1>
public class MpdResource {

    private static Logger log = Logger.getLogger(MpdResource.class);

    @Inject
    LinuxMPC service;

    @ServerExceptionMapper
    public Response mapException(ConnectException e) {
        log.error(e);

        ErrorRepsonseBody body = new ErrorRepsonseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }

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
