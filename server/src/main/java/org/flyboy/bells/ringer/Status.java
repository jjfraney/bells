package org.flyboy.bells.ringer;

/**
 * @author John J. Franey
 */
public class Status {
    private Boolean isLocked;
    private String state;

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
