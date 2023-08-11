package org.flyboy.belltower.belfry;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.ConnectException;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Path("/mpd")                        // <1>
public class MpdResource {

    private static final Logger logger = Logger.getLogger(MpdResource.class);

    @Inject
    Mpd service;

    @SuppressWarnings("unused")
    @ServerExceptionMapper
    public Response mapException(ConnectException e) {
        logger.error(e);

        ErrorResponseBody body = new ErrorResponseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }

    @GET
    public Multi<String> mpc(@QueryParam("cmd") String cmd) {
        return service.send(cmd)
                .onItem().transformToMulti(list -> Multi.createFrom().items(list.stream()))
                .onItem().transform(s -> s + "\n");
    }

    @GET
    @Path("/status")
    public Uni<List<String>> status() {
        return service.send("status");
    }
}
