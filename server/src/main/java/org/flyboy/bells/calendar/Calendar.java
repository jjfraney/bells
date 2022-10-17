package org.flyboy.bells.calendar;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author jfraney
 */
public interface Calendar {
    public interface Event {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mmZ")
        ZonedDateTime getTime();
        String getTitle();
    }

    List<Event> getEvents();
}
