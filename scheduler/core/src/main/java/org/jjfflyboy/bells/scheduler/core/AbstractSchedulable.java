package org.jjfflyboy.bells.scheduler.core;

import java.time.LocalDateTime;

/**
 * @author jfraney
 */
public abstract class AbstractSchedulable implements Scheduler.Schedulable {
    private LocalDateTime firetime;

    public AbstractSchedulable(LocalDateTime firetime) {
        this.firetime = firetime;
    }
    @Override
    public LocalDateTime getFireTime() {
        return firetime;
    }

    protected void setFireTime(LocalDateTime firetime) {
        this.firetime = firetime;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + firetime.toString();
    }
}
