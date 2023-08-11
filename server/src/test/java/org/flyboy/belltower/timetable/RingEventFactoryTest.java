package org.flyboy.belltower.timetable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public class RingEventFactoryTest {

    RingEventFactory ringEventFactory;

    @BeforeEach
    public void beforeEach() {
        ringEventFactory = new RingEventFactory();
        ringEventFactory.massOffset = "-PT2M";
    }

    @Test
    public void testMassOffset() {
        ZonedDateTime now = ZonedDateTime.now();
        RingRequest request = new RingRequest(now, "mass");

        RingEvent actual = ringEventFactory.createFrom(request);
        RingEvent expected = new RingEvent(now.minusMinutes(2), "call-to-mass.ogg", request);

        Assertions.assertEquals(expected, actual);
    }
}
