package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.vertx.MutinyHelper;
import io.vertx.core.Vertx;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * @author John J. Franey
 */
@ApplicationScoped
public class GoogleBellEventRepository implements BellEventRepository {
    @Inject
    GoogleCalendar calendar;

    /**
     * Return a Multi that emits sorted BellEventRepository.Events
     * from a remote calendar.
     * @return list of events
     */
    public Uni<List<BellEvent>> getEvents() {
        return Uni.createFrom().item(1)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transformToUni(i -> Uni.createFrom().item(calendar.getEvents()))
                .emitOn(MutinyHelper.executor(Vertx.currentContext()))
                ;
    }
}
