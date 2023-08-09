package org.flyboy.bells.security.oauth2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author John J. Franey
 */
public class AuthCodeTimeoutExceptionTest {

    @Test
    public void test() {
        String duration = "PT2M";
        AuthCodeTimeoutException exception = new AuthCodeTimeoutException(Duration.parse(duration));

        Assertions.assertEquals(duration, exception.duration.toString());
        Assertions.assertEquals("Auth code timeout after duration: " + duration , exception.getMessage());
    }
}
