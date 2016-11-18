package org.jjfflyboy.bells.scheduler.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author jfraney
 */
public interface Scheduler {
    interface Schedulable {

        /**
         * get the time it should be fired
         * @return
         */
        LocalDateTime getFireTime();

        <V> Callable<V> getCallable();
    }

    /**
     * replace existing schedulable with these.
     * @param schedulables
     */
    void schedule(List<Schedulable> schedulables);
}
