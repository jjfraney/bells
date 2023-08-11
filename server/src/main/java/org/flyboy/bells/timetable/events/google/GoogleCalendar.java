package org.flyboy.bells.timetable.events.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.flyboy.bells.security.oauth2.BearerTokenClientHeadersFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Rest client for google calendar api.
 * <p>
 *     Providers add authorization header, a logger, and param converters.
 *     Loads only the fields of interest of a {@link org.flyboy.bells.timetable.RingRequest}
 * </p>
 * @author John J. Franey
 */
@RegisterRestClient(configKey = "calendar-service")
@RegisterClientHeaders(BearerTokenClientHeadersFactory.class)
@RegisterProvider(GoogleCalendarLoggerFilter.class)
@RegisterProvider(ParamConverterProvider.class)
public interface GoogleCalendar {
    @SuppressWarnings("unused")
    record Event(String id, DateTime start, String summary) {
        public record DateTime(
                @JsonProperty String date,
                @JsonProperty("dateTime") ZonedDateTime dateTime,
                String timezone
        ) {}
    }
    record EventList(
            List<Event> items) {}

    @GET
    @Path("/{calendarId}/events")
    Uni<EventList> getEventList(@PathParam("calendarId") String calendarId,
                                @QueryParam("singleEvents") Boolean singleEvents,
                                @QueryParam("timeMin") ZonedDateTime timeMin,
                                @QueryParam("timeMax") ZonedDateTime timeMax
    );

}


