package org.flyboy.bells.belfry;

import java.util.UUID;

/**
 * @author John J. Franey
 */
@SuppressWarnings("unused")
public class ErrorResponseBody {
    private String details;
    private UUID errorId = UUID.randomUUID();

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public UUID getErrorId() {
        return errorId;
    }

    public void setErrorId(UUID errorId) {
        this.errorId = errorId;
    }

    @Override
    public String toString() {
        return "details: " +
                getDetails() +
                ", error id: " +
                getErrorId();
    }
}
