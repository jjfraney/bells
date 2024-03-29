package org.flyboy.belltower.timetable;

import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * @author jfraney
 */
public interface RingRequestRepository {

    Uni<List<RingRequest>> getRequests();
}
