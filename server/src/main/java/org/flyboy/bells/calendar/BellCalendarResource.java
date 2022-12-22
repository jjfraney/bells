package org.flyboy.bells.calendar;

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
public class BellCalendarResource {

    @Inject
    BellCalendar calendarService;

    @GET
    @Path("/events")
    public Uni<List<BellEvent>> getFromCalendar() {
        return calendarService.getEvents();
    }

}
