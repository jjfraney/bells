package org.flyboy.bells.ring.events.google;

import javax.ws.rs.ext.ParamConverter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeConverter implements ParamConverter<ZonedDateTime> {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    public ZonedDateTime fromString(String value) {
        if (value == null)
            return null;
        return ZonedDateTime.parse(value, FORMATTER);
    }

    @Override
    public String toString(ZonedDateTime value) {
        if (value == null)
            return null;
        return value.format(FORMATTER);
    }

}
