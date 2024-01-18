package org.flyboy.belltower.panel.scene.timetable.model;

import java.time.ZonedDateTime;

/**
 * Timetable entry.
 * @param dateTime of the slot
 * @param title of the slot
 * @author John J. Franey
 */
public record Slot(ZonedDateTime dateTime, String title) {

}
