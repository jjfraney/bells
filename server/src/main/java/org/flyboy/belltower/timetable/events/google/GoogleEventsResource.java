package org.flyboy.belltower.timetable.events.google;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.flyboy.belltower.timetable.RingRequest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author John J. Franey
 */
@Path("/bell/events/google")
@Produces(MediaType.APPLICATION_JSON)
public class GoogleEventsResource {
    @Inject
    GoogleEventsService service;
    @GET
    @Operation(description = "Get scheduled ring requests from google calendar service.")
    public Uni<List<RingRequest>> getRequests() {
        return service.getRequests();
    }

}
