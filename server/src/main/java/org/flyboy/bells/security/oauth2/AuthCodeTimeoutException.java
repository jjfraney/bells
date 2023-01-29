package org.flyboy.bells.security.oauth2;

import java.time.Duration;
import java.util.Objects;

/**
 * To indicate we are impatient to receive the auth code callback.
 * @author John J. Franey
 */
public class AuthCodeTimeoutException extends  RuntimeException {
    final Duration duration;
    AuthCodeTimeoutException(Duration duration) {
        Objects.requireNonNull(duration);
        this.duration = duration;
    }
    @Override
    public String getMessage() {
        return "Auth code timeout after duration: " + duration;
    }
}
