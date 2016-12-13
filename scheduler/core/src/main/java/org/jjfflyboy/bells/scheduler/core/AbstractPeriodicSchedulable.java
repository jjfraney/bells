package org.jjfflyboy.bells.scheduler.core;

import java.time.Duration;

/**
 * @author jfraney
 */
public abstract class AbstractPeriodicSchedulable implements Scheduler.PeriodicSchedulable  {

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + getPeriod().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractPeriodicSchedulable that = (AbstractPeriodicSchedulable) o;

        return getPeriod().equals(that.getPeriod());

    }

    @Override
    public int hashCode() {
        return getPeriod().hashCode();
    }
}
