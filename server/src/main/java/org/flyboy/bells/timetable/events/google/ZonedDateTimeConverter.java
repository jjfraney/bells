package org.flyboy.bells.timetable.events.google;

import jakarta.ws.rs.ext.ParamConverter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeConverter implements ParamConverter<ZonedDateTime> {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    public ZonedDateTime fromString(String value) {
        if (value == null)
            return null;
        Instant instant = Instant.parse(value);
        return ZonedDateTime.ofInstant(instant, ZoneId.of("Z"));
    }

    @Override
    public String toString(ZonedDateTime value) {
        if (value == null)
            return null;
        return value.format(FORMATTER);
    }

}
