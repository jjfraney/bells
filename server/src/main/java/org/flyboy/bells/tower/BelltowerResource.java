package org.flyboy.bells.tower;

import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.ConnectException;

/**
 * Resource offering controls of the music player.
 * @author John J. Franey
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/belltower")
public class BelltowerResource {
    @Inject
    Belltower belltower;

    @GET
    @Path("/status")
    public Uni<BelltowerStatus> getStatus() {
        return belltower.getStatus();
    }

    @POST
    @Path("/ring")
    public Uni<BelltowerStatus> ring(@QueryParam("name") String name) {
        return belltower.ring(name);
    }

    @DELETE
    @Path("/lock")
    public Uni<BelltowerStatus> unlock() {
        belltower.unlock();
        return belltower.getStatus();
    }
    @POST
    @Path("/lock")
    public Uni<BelltowerStatus> lock() {
        belltower.lock();
        return belltower.getStatus();
    }

    @ServerExceptionMapper
    public Response mapException(ConnectException e) {
        ErrorRepsonseBody body = new ErrorRepsonseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }

    @ServerExceptionMapper
    public Response mapException(BelltowerUnavailableException e) {
        ErrorRepsonseBody body = new ErrorRepsonseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }

    @ServerExceptionMapper
    public Response mapException(BelltowerSampleNotFoundException e) {
        ErrorRepsonseBody body = new ErrorRepsonseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
    @ServerExceptionMapper
    public Response mapException(BelltowerException e) {
        ErrorRepsonseBody body = new ErrorRepsonseBody();
        body.setDetails(e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }
}
