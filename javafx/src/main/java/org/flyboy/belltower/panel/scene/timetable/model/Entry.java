package org.flyboy.belltower.panel.scene.timetable.model;

import java.time.ZonedDateTime;

/**
 * Timetable entry.
 * @param dateTime of the event
 * @param eventDescription of the event
 * @author John J. Franey
 */
public record Entry(ZonedDateTime dateTime, String eventDescription) {

}
