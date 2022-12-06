package org.flyboy.bells.tower;

import java.util.UUID;

/**
 * @author John J. Franey
 */
public class ErrorRepsonseBody {
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
}
