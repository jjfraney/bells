package org.flyboy.bells.calendar;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZonedDateTime.ofInstant;

/**
 * Represent the data of a schedulable bell event.
 * Of interest in a bell event is only the name of the smple to play
 * and the time to play it.
 * @author John J. Franey
 */
public class BellEvent {
    private final ZonedDateTime time;
    private final String title;

    /**
     * @param milliseconds of epoch (from jan 1, 1970 midnight UTC) of bell event
     * @param title of bell event
     */
    public BellEvent(Long milliseconds, String title) {
        this(ofInstant(ofEpochMilli(milliseconds), ZoneId.systemDefault()), title);
    }

    /**
     * @param dateTime of time of the bell event
     * @param title of bell event
     */
    public BellEvent(ZonedDateTime dateTime, String title) {
        Objects.requireNonNull(title);
        Objects.requireNonNull(dateTime);

        this.title = title;
        this.time = dateTime;
    }

    public ZonedDateTime getTime() {
        if(title.equalsIgnoreCase("mass")) {
            // TODO: Observe the configuration parameter for this time offset
            return time.minus(1, ChronoUnit.MINUTES);
        } else {
            return time;
        }
    }

    public String getTitle() {
        if(title.equalsIgnoreCase("mass")) {
            return "call-to-mass.ogg";
        } else {
            return title;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BellEvent bellEvent = (BellEvent) o;
        return getTime().equals(bellEvent.getTime()) && getTitle().equals(bellEvent.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getTitle());
    }

    @Override
    public String toString() {
        return "BellEvent{" +
                "time=" + getTime() +
                ", title='" + getTitle() + '\'' +
                '}';
    }
}
