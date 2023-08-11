package org.flyboy.bells.timetable.events.google;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public class ZonedDateTimeConverterTest {

    private final ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter();

    ZonedDateTime asZonedDateTime = ZonedDateTime.of(2011, 12, 11,
            10, 15, 30, 0, ZoneId.of("Z"));
    String asText = "2011-12-11T10:15:30Z";


    @Test
    public void testToZonedDateTime() {
        ZonedDateTime zonedDateTime = zonedDateTimeConverter.fromString(asText);
        Assertions.assertEquals(asZonedDateTime, zonedDateTime);
    }

    @Test
    public void testFromZoneDateTime() {
        String datetime = zonedDateTimeConverter.toString(asZonedDateTime);
        Assertions.assertEquals(asText, datetime);
    }


}
