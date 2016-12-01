package org.jjfflyboy.bells.scheduler.core;

import java.time.LocalDateTime;

/**
 * @author jfraney
 */
public abstract class AbstractOneShotSchedulable implements Scheduler.OneShotSchedulable {
    private final LocalDateTime firetime;

    public AbstractOneShotSchedulable(LocalDateTime firetime) {
        this.firetime = firetime;
    }
    @Override
    public LocalDateTime getFireTime() {
        return firetime;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + firetime.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractOneShotSchedulable that = (AbstractOneShotSchedulable) o;

        return firetime.equals(that.firetime);

    }

    @Override
    public int hashCode() {
        return firetime.hashCode();
    }
}
