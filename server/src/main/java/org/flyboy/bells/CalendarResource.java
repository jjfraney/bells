package org.flyboy.bells;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.flyboy.bells.calendar.Calendar;
import org.flyboy.bells.calendar.CalendarByGoogle;

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
    CalendarByGoogle calendar;

    @GET
    public Multi<Calendar.Event> getEvents() {

        return Uni.createFrom().item(1)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transformToMulti(i -> Multi.createFrom().items(calendar.getEvents().stream()));
    }
}
