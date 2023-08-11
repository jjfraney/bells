package org.flyboy.belltower.timetable;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZonedDateTime.ofInstant;

/**
 * Represent the data of a request from the user to ring a bell sample.
 *
 * @author John J. Franey
 */
public record RingRequest(ZonedDateTime dateTime, String title) {
    public RingRequest(Long milliseconds, String title) {
        this(ofInstant(ofEpochMilli(milliseconds), ZoneId.systemDefault()), title);
    }
}
