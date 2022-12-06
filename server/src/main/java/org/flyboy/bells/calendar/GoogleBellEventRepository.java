package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.vertx.MutinyHelper;
import io.vertx.core.Vertx;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Comparator;

/**
 * @author John J. Franey
 */
@ApplicationScoped
public class GoogleBellEventRepository implements BellEventRepository {
    @Inject
    GoogleCalendar calendar;

    int waitMinutes = 2;

    /**
     * Return a Multi that emits sorted BellEventRepository.Events
     * from a remote calendar.
     * @return list of events
     */
    public Multi<BellEvent> getEvents() {
        return Uni.createFrom().item(1)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transformToUni(i -> Uni.createFrom().item(calendar.getEvents()))
                .emitOn(MutinyHelper.executor(Vertx.currentContext()))
                .map( events -> {
                    events.sort(eventComparator);
                    return events;
                })
                .log()
                .onItem().transformToMulti((item -> Multi.createFrom().items(item.stream())))
                ;
    }
    private final Comparator<BellEvent> eventComparator = (BellEvent e1, BellEvent e2) -> e1.getTime().compareTo(e2.getTime());

}