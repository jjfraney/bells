package org.flyboy.bells;

import io.smallrye.mutiny.Multi;
import org.flyboy.bells.calendar.Calendar;
import org.flyboy.bells.calendar.CalendarService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author John J. Franey
 */
@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
public class EventsResource {

    @Inject
    CalendarService calendarService;

    @GET
    @Path("/calendar")
    public Multi<Calendar.Event> getFromCalendar() {
        return calendarService.getEvents();
    }

}
