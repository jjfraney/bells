package org.flyboy.belltower.timetable.events.google;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;

@Provider
public class ParamConverterProvider implements jakarta.ws.rs.ext.ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(ZonedDateTime.class)) {
            //noinspection unchecked
            return (ParamConverter<T>) new ZonedDateTimeConverter();
        }
        return null;
    }
}
