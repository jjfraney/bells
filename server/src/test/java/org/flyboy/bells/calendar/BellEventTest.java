package org.flyboy.bells.calendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public class BellEventTest {

    @Test
    public void testMassSampleName() {
        BellEvent bellEvent = new BellEvent(ZonedDateTime.now(), "mass");
        String actual = bellEvent.getTitle();
        String expected = "call-to-mass.ogg";
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testMassEventTime() {
        ZonedDateTime time = ZonedDateTime.of(2022, 1, 1,
                0, 0, 0, 0, ZoneId.systemDefault());
        BellEvent bellEvent = new BellEvent(time, "mass");

        ZonedDateTime actual = bellEvent.getTime();
        ZonedDateTime expected = time.minusMinutes(1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testEquals() {
        ZonedDateTime now = ZonedDateTime.now();

        BellEvent bell1 = new BellEvent(now, "mass");
        BellEvent bell2 = new BellEvent(now, "mass");
        Assertions.assertEquals(bell1, bell2);
    }

    @Test
    public void testLongConstructor() {
        ZonedDateTime now = ZonedDateTime.of(2022, 12, 25, 7, 15, 0, 0, ZoneId.systemDefault());

        BellEvent bell1 = new BellEvent(now.toInstant().toEpochMilli(), "mass");
        BellEvent bell2 = new BellEvent(now, "mass");

        Assertions.assertEquals(bell1, bell2);
    }

    @Test
    public void testToString() {
        ZonedDateTime now = ZonedDateTime.now();
        BellEvent bellEvent = new BellEvent(now, "mass");
        String expected = "BellEvent{" +
                "time=" + bellEvent.getTime() +
                ", title='" + bellEvent.getTitle() + '\'' +
                '}';
        Assertions.assertEquals(expected, bellEvent.toString());
    }
}
