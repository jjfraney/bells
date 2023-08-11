package org.flyboy.bells.timetable;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.flyboy.bells.belfry.Bell;
import org.flyboy.bells.belfry.BellStatus;
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
public class RingIntervalometerTest {

    RingIntervalometer ringIntervalometer;

    RingRequestMultiRepository mockRingRequestMultiRepository;

    Bell mockBell;

    Vertx mockVertx;
    @BeforeEach
    public void beforeEach() {
        ringIntervalometer = new RingIntervalometer();

        mockVertx = Mockito.mock(Vertx.class);
        ringIntervalometer.vertx = mockVertx;

        mockRingRequestMultiRepository = Mockito.mock(RingRequestMultiRepository.class);
        ringIntervalometer.ringRequestMultiRepository = mockRingRequestMultiRepository;

        mockBell = Mockito.mock(Bell.class);
        ringIntervalometer.bell = mockBell;

        ringIntervalometer.ringEventFactory = new RingEventFactory();
    }

    @Test
    public void testRing() {
        RingRequest one = new RingRequest(ZonedDateTime.now().plusSeconds(30), "myogg.ogg");
        ringIntervalometer.currentTimers = new HashMap<>() {{
            put(1L, new RingEvent(one.dateTime(), one.title(), one));
        }};


        BellStatus result = new BellStatus(false, "play");

        Mockito.when(mockBell.ring("myogg.ogg")).thenReturn(Uni.createFrom().item(result));

        ringIntervalometer.ring(1L);
        Assertions.assertEquals(0, ringIntervalometer.currentTimers.size());
    }
    @Test
    public void testScheduleRingEvents() {
        RingRequest one = new RingRequest(ZonedDateTime.now().plusSeconds(30), "myogg.ogg");
        List<RingRequest> events = List.of(one);

        //noinspection unchecked
        Mockito.when(mockVertx.setTimer(any(Long.class), any(Consumer.class))).thenReturn(1L);

        Assertions.assertEquals(0, ringIntervalometer.currentTimers.size());
        ringIntervalometer.scheduleRingRequests(events);
        Assertions.assertEquals(1, ringIntervalometer.currentTimers.size());
    }

    @Test
    public void testScheduleRing() {

        RingRequest one = new RingRequest(ZonedDateTime.now(), "myogg.ogg");

        List<RingRequest> events = List.of(one);
        Mockito.when(mockRingRequestMultiRepository.getRequests()).thenReturn(Uni.createFrom().item(events));

        RingIntervalometer spy = Mockito.spy(ringIntervalometer);
        Mockito.doNothing().when(spy).scheduleRingRequests(events);

        spy.scheduleRingRequests();
        Mockito.verify(spy).scheduleRingRequests(events);

    }

    @Test
    public void testScheduleRingFailedEventQuery() {

        Mockito.when(mockRingRequestMultiRepository.getRequests()).thenReturn(Uni.createFrom().failure(new IOException("mock no connection")));

        RingIntervalometer spy = Mockito.spy(ringIntervalometer);

        spy.scheduleRingRequests();

    }

}
