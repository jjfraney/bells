package org.flyboy.bells.timetable.events.google;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.flyboy.bells.timetable.RingRequest;
import org.flyboy.bells.timetable.RingRequestRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author John J. Franey
 */
@ApplicationScoped
@Named("Google")
public class GoogleEventsService implements RingRequestRepository {

    @RestClient
    GoogleCalendar googleCalendar;

    @ConfigProperty(name="belltower.google.calendar.id")
    String calendarId;

    @ConfigProperty(name="belltower.google.calendar.query.lookAhead")
    Duration lookAhead;

    @GET
    public Uni<List<RingRequest>> getRequests() {
        ZonedDateTime min = ZonedDateTime.now();
        ZonedDateTime max = ZonedDateTime.now().plus(lookAhead);
        return googleCalendar.getEventList(calendarId, true, min, max)
                .onItem().transformToUni(events -> {
                    List<RingRequest> list = events.items().stream()
                            .map(e -> new RingRequest(e.start().dateTime(), e.summary()))
                            .toList();
                    return Uni.createFrom().item(list);
                });

    }
}
