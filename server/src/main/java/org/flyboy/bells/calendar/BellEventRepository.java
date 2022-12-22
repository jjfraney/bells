package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * @author jfraney
 */
public interface BellEventRepository {

    Uni<List<BellEvent>> getEvents();
}
