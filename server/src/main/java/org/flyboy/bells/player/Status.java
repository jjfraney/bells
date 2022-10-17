package org.flyboy.bells.player;

/**
 * @author John J. Franey
 */
public class Status {
    private Boolean isLocked;
    private musicpd.protocol.Status.Response mpdStatus;

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }

    public musicpd.protocol.Status.Response getMpdStatus() {
        return mpdStatus;
    }

    public void setMpdStatus(musicpd.protocol.Status.Response mpdStatus) {
        this.mpdStatus = mpdStatus;
    }
}
