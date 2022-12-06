package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Multi;

/**
 * @author jfraney
 */
public interface BellEventRepository {

    Multi<BellEvent> getEvents();
}
