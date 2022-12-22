package org.flyboy.bells.calendar;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.flyboy.bells.tower.Belltower;
import org.flyboy.bells.tower.BelltowerStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author John J. Franey
 */
public class BellCalendarIntervalometerTest {

    BelltowerIntervalometer bellCalendarIntervalometer;

    BellCalendar mockBellCalendar;

    Belltower mockBelltower;

    Vertx mockVertx;
    @BeforeEach
    public void beforeEach() {
        bellCalendarIntervalometer = new BelltowerIntervalometer();

        mockVertx = Mockito.mock(Vertx.class);
        bellCalendarIntervalometer.vertx = mockVertx;

        mockBellCalendar = Mockito.mock(BellCalendar.class);
        bellCalendarIntervalometer.bellCalendar = mockBellCalendar;

        mockBelltower = Mockito.mock(Belltower.class);
        bellCalendarIntervalometer.belltower = mockBelltower;
    }

    @Test
    public void testRing() {
        BellEvent one = new BellEvent(ZonedDateTime.now().plusSeconds(30), "myogg.ogg");
        bellCalendarIntervalometer.currentTimers = new HashMap<>() {{
            put(1L, one);
        }};


        BelltowerStatus result = new BelltowerStatus();
        result.setState("play");
        result.setLocked(false);

        Mockito.when(mockBelltower.ring("myogg.ogg")).thenReturn(Uni.createFrom().item(result));

        bellCalendarIntervalometer.ring(1L);
        Assertions.assertEquals(0, bellCalendarIntervalometer.currentTimers.size());
    }
    @Test
    public void testScheduleRingEvents() {
        BellEvent one = new BellEvent(ZonedDateTime.now().plusSeconds(30), "myogg.ogg");
        List<BellEvent> events = List.of(one);

        //noinspection unchecked
        Mockito.when(mockVertx.setTimer(any(Long.class), any(Consumer.class))).thenReturn(1L);

        Assertions.assertEquals(0, bellCalendarIntervalometer.currentTimers.size());
        bellCalendarIntervalometer.scheduleRing(events);
        Assertions.assertEquals(1, bellCalendarIntervalometer.currentTimers.size());
    }

    @Test
    public void testScheduleRing() {

        BellEvent one = new BellEvent(ZonedDateTime.now(), "myogg.ogg");

        List<BellEvent> events = List.of(one);
        Mockito.when(mockBellCalendar.getEvents()).thenReturn(Uni.createFrom().item(events));

        BelltowerIntervalometer spy = Mockito.spy(bellCalendarIntervalometer);
        Mockito.doNothing().when(spy).scheduleRing(events);

        spy.scheduleRing();
        Mockito.verify(spy).scheduleRing(events);

    }

    @Test
    public void testScheduleRingFailedEventQuery() {

        Mockito.when(mockBellCalendar.getEvents()).thenReturn(Uni.createFrom().failure(new IOException("mock no connection")));

        BelltowerIntervalometer spy = Mockito.spy(bellCalendarIntervalometer);

        spy.scheduleRing();

    }

}
