package org.flyboy.bells.calendar;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author jfraney
 */
public interface Calendar {
    public interface Event {
        ZonedDateTime getTime();
        String getTitle();
    }

    List<Event> getEvents();
}
