package org.flyboy.bells.timetable;

import java.time.ZonedDateTime;

/**
 * The date/dateTime and ring sample which the intervalometer will command
 * MPD to ring the bells.
 *
 * @author John J. Franey
 */
@SuppressWarnings("unused")
public record RingEvent(ZonedDateTime dateTime, String sampleName, RingRequest request) {
}

