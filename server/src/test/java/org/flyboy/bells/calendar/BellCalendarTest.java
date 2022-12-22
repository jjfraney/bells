package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John J. Franey
 */
public class BellCalendarTest {

    BellCalendar bellCalendar;
    GoogleBellEventRepository mockGoogleBellEventRepository;

    @BeforeEach
    public void beforeEach() {
        bellCalendar = new BellCalendar();
        mockGoogleBellEventRepository = Mockito.mock(GoogleBellEventRepository.class);
        bellCalendar.googleRepository = mockGoogleBellEventRepository;
    }

    @Test
    public void testSort() {
        BellEvent earlier = new BellEvent(ZonedDateTime.now().minusSeconds(30), "earlier.ogg");
        BellEvent later = new BellEvent(ZonedDateTime.now().plusSeconds(30), "later.ogg");

        List<BellEvent> queryResult = new ArrayList<>(List.of(later, earlier));
        // return list in reverse chronological order
        Mockito.when(mockGoogleBellEventRepository.getEvents()).thenReturn(Uni.createFrom().item(queryResult));

        List<BellEvent> expected = List.of(earlier, later);
        bellCalendar.getEvents().subscribe().with(
                bellEvents -> Assertions.assertEquals(expected, bellEvents)
        );
    }
}
