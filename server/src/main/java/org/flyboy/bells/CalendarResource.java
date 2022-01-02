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
@Path("/cal")
@Produces(MediaType.APPLICATION_JSON)
public class CalendarResource {
    @Inject
    CalendarService service;

    @GET
    public Multi<Calendar.Event> getEvents() {
        return service.getEvents();
    }
}
