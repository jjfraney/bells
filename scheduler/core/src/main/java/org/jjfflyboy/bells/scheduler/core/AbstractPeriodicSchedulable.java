package org.jjfflyboy.bells.scheduler.core;

import java.time.Duration;

/**
 * @author jfraney
 */
public abstract class AbstractPeriodicSchedulable implements Scheduler.PeriodicSchedulable  {
    private final Duration period;

    public AbstractPeriodicSchedulable(Duration period) {
        this.period = period;
    }

    @Override
    public Duration getPeriod() {
        return period;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + period.toString();
    }

}
