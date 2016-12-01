package org.jjfflyboy.bells.scheduler.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author jfraney
 */
public interface Scheduler {
    interface Schedulable {


        Callable<Void> getCallable();
    }
    interface OneShotSchedulable extends Schedulable {
        /**
         * get the time it should be fired
         * @return
         */
        LocalDateTime getFireTime();
    }
    interface PeriodicSchedulable extends Schedulable {
        Duration getPeriod();
    }

    /**
     * replace existing schedulable with these.
     * @param schedulables
     */
    void schedulePeriodic(List<PeriodicSchedulable> schedulables);
    void scheduleOneShot(List<OneShotSchedulable> schedulables);

}
