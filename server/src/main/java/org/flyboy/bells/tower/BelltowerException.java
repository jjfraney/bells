package org.flyboy.bells.tower;

/**
 * @author John J. Franey
 */
public class BelltowerException extends RuntimeException {
    public BelltowerException() {
    }

    public BelltowerException(String message) {
        super(message);
    }

    public BelltowerException(String message, Throwable cause) {
        super(message, cause);
    }

    public BelltowerException(Throwable cause) {
        super(cause);
    }

    public BelltowerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
