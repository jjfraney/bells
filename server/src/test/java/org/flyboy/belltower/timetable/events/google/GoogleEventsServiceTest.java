package org.flyboy.belltower.timetable.events.google;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.flyboy.belltower.timetable.RingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author John J. Franey
 */
public class GoogleEventsServiceTest {
    GoogleEventsService googleEventsService;

    @BeforeEach
    public void beforeEach() {
        googleEventsService = new GoogleEventsService();
        googleEventsService.lookAhead = Duration.ofHours(1);
        googleEventsService.calendarId = "calendar-id";
        googleEventsService.googleCalendar = Mockito.mock(GoogleCalendar.class);
    }

    @Test
    public void test() {
        // setup the data returned from mocked google calendar rest client
        ZonedDateTime expectedZonedDateTime = ZonedDateTime.now();
        GoogleCalendar.Event.DateTime dateTime = new GoogleCalendar.Event.DateTime("today",
                expectedZonedDateTime, "UTC");
        GoogleCalendar.Event event = new GoogleCalendar.Event("eventid", dateTime, "mass");
        GoogleCalendar.EventList eventList = new GoogleCalendar.EventList(List.of(event));
        Uni<GoogleCalendar.EventList> uniEventList = Uni.createFrom().item(eventList);

        Mockito.when(googleEventsService.googleCalendar.getEventList(any(String.class), any(Boolean.class), any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(uniEventList);

        Uni<List<RingRequest>> actual = googleEventsService.getRequests();

        UniAssertSubscriber<List<RingRequest>> subscriber = actual
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        List<RingRequest> expected = List.of(new RingRequest(expectedZonedDateTime, "mass"));
        subscriber.assertCompleted().assertItem(expected);

    }
}
