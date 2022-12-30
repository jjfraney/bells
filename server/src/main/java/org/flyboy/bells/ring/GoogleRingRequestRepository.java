package org.flyboy.bells.ring;

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
public class GoogleRingRequestRepository implements RingRequestRepository {
    @Inject
    GoogleCalendar calendar;

    /**
     * Return a Multi that emits sorted {@link RingRequest}s
     * from a remote or local repository.
     *
     * @return list of {@link RingRequest} as a {@link Uni}
     */
    @Override
    public Uni<List<RingRequest>> getRequests() {
        return Uni.createFrom().item(1)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transformToUni(i -> Uni.createFrom().item(calendar.getEvents()))
                .emitOn(MutinyHelper.executor(Vertx.currentContext()))
                ;
    }
}
