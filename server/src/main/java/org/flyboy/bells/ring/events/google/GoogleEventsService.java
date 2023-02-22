package org.flyboy.bells.ring.events.google;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.flyboy.bells.ring.RingRequest;
import org.flyboy.bells.ring.RingRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.ws.rs.GET;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author John J. Franey
 */
@ApplicationScoped
@Named("Google")
public class GoogleEventsService implements RingRequestRepository {

    private static final Logger logger = LoggerFactory.getLogger(GoogleEventsService.class);

    @RestClient
    GoogleCalendar googleCalendar;

    @ConfigProperty(name="belltower.google.calendar.id")
    String calendarId;

    @ConfigProperty(name="belltower.google.calendar.query.lookAhead")
    Duration lookAhead;

    @GET
    public Uni<List<RingRequest>> getRequests() {
        RingRequest event = new RingRequest(ZonedDateTime.now(), "no-sample");

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
