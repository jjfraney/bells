package org.flyboy.belltower.panel.tower;

/**
 * @author John J. Franey
 */
public class BelltowerStatus {
    private String status;
    private String locked;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }
}