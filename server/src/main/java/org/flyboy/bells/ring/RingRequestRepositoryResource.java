package org.flyboy.bells.ring;

import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author John J. Franey
 */
@Path("/bell/calendar")
@Produces(MediaType.APPLICATION_JSON)
public class RingRequestRepositoryResource {

    @Inject
    RingRequestMultiRepository repository;

    @GET
    @Path("/events")
    public Uni<List<RingRequest>> getRingRequests() {
        return repository.getRequests();
    }

}
