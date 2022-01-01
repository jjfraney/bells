package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;

/**
 * @author John J. Franey
 */
@RequestScoped
public class CalendarService {
    @Inject
    CalendarByGoogle calendar;

    public Multi<Calendar.Event> getEvents() {

        return Uni.createFrom().item(1)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transformToMulti(i -> Multi.createFrom().items(calendar.getEvents().stream()));
    }
}
