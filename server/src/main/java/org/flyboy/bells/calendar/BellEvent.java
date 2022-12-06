package org.flyboy.bells.calendar;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public interface BellEvent {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mmZ")
    ZonedDateTime getTime();

    String getTitle();
}
