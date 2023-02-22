package org.flyboy.bells.ring.events.google;

import io.smallrye.mutiny.Uni;
import org.flyboy.bells.ring.RingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author John J. Franey
 */
@Path("/bell/events/google")
@Produces(MediaType.APPLICATION_JSON)
public class GoogleEventsResource {
    private final static Logger logger = LoggerFactory.getLogger(GoogleEventsResource.class);

    @Inject
    GoogleEventsService service;
    @GET
    public Uni<List<RingRequest>> getRequests() {
        return service.getRequests();
    }

}
