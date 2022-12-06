package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Multi;
import org.flyboy.bells.calendar.BellEvent;
import org.flyboy.bells.calendar.GoogleBellEventRepository;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author John J. Franey
 */
@Path("/bell/calendar")
@Produces(MediaType.APPLICATION_JSON)
public class BellCalendarResource {

    @Inject
    GoogleBellEventRepository calendarService;

    @GET
    @Path("/events")
    public Multi<BellEvent> getFromCalendar() {
        return calendarService.getEvents();
    }

}
