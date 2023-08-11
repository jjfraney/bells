package org.flyboy.belltower.belfry;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.ConnectException;

/**
 * Resource offering controls of the music player.
 *
 * @author John J. Franey
 */

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/belltower")
public class BellResource {
    private static final String VACANT = "__vacant__";

    @Inject
    Bell bell;

    @GET
    @Operation(description = "Return bell status.")
    public Uni<BellStatus> getStatus() {
        return bell.getStatus();
    }

    @PUT
    @Path("/ring")
    @Operation(description = "Ring bells specified by the named pattern.")
    public Uni<BellStatus> startPattern(@DefaultValue(VACANT) @QueryParam("pattern") String pattern) {
        return Uni.createFrom().nullItem()
                .onItem().transformToUni(n -> {
                    if (pattern.equals(VACANT)) {
                        return Uni.createFrom().failure(new BadRequestException("'pattern' parameter is required and missing."));
                    } else {
                        return bell.ring(pattern);
                    }
                });

    }


    @DELETE
    @Path("/ring")
    @Operation(description = "Stop ringing the bells.  Some patterns cannot be stopped and must ring through.")
    public Uni<BellStatus> ringStop() {
        return bell.stop();
    }

    @DELETE
    @Path("/lock")
    @Operation(description = "Release the lock.  Requests to ring the bells will be ignored when locked.")
    public Uni<BellStatus> unlock() {
        return bell.unlock();
    }

    //@PUT
    @PUT
    @Path("/lock")
    @Operation(description = "Engage the lock.  Requests to ring the bells will be ignored when locked.")
    public Uni<BellStatus> lock() {
        return bell.lock();
    }

    @SuppressWarnings("unused")
    @ServerExceptionMapper
    public Response mapException(BadRequestException e) {
        ErrorResponseBody body = new ErrorResponseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
    @SuppressWarnings("unused")
    @ServerExceptionMapper
    public Response mapException(ConnectException e) {
        ErrorResponseBody body = new ErrorResponseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }

    @SuppressWarnings("unused")
    @ServerExceptionMapper
    public Response mapException(BellsUnavailableException e) {
        ErrorResponseBody body = new ErrorResponseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }

    @SuppressWarnings("unused")
    @ServerExceptionMapper
    public Response mapException(BellPatternNotFoundException e) {
        ErrorResponseBody body = new ErrorResponseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }

    @SuppressWarnings("unused")
    @ServerExceptionMapper
    public Response mapException(BelfryException e) {
        ErrorResponseBody body = new ErrorResponseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }
}
