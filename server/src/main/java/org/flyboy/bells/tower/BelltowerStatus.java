package org.flyboy.bells.tower;

/**
 * @author John J. Franey
 */
public class BelltowerStatus {
    private Boolean isLocked;
    private String state;

    @SuppressWarnings("unused")
    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }

    @SuppressWarnings("unused")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "BelltowerStatus{" +
                "isLocked=" + isLocked +
                ", state='" + state + '\'' +
                '}';
    }
}
