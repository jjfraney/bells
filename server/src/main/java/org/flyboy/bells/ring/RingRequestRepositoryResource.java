package org.flyboy.bells.ring;

import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
