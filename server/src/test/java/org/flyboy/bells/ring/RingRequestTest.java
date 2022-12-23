package org.flyboy.bells.ring;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public class RingRequestTest {

    @Test
    public void testEquals() {
        ZonedDateTime now = ZonedDateTime.now();

        RingRequest bell1 = new RingRequest(now, "mass");
        RingRequest bell2 = new RingRequest(now, "mass");
        Assertions.assertEquals(bell1, bell2);
    }

    @Test
    public void testLongConstructor() {
        ZonedDateTime now = ZonedDateTime.of(2022, 12, 25, 7, 15, 0, 0, ZoneId.systemDefault());

        RingRequest bell1 = new RingRequest(now.toInstant().toEpochMilli(), "mass");
        RingRequest bell2 = new RingRequest(now, "mass");

        Assertions.assertEquals(bell1, bell2);
    }
}
