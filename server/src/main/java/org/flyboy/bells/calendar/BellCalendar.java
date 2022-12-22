package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;

/**
 * queries all existing bell event repositories
 * to return soonest events.
 *
 * @author John J. Franey
 */
@ApplicationScoped
public class BellCalendar {

    private final Comparator<BellEvent> eventComparator = Comparator.comparing(BellEvent::getTime);
    @Inject
    BellEventRepository googleRepository;

    public Uni<List<BellEvent>> getEvents() {
        return googleRepository.getEvents().map(events -> {
            events.sort(eventComparator);
            return events;
        });
    }

}
