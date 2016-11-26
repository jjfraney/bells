package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author jfraney
 */
public class RootSchedulable implements Scheduler.Schedulable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RootSchedulable.class);
    private LocalDateTime firetime = LocalDateTime.now().plus(Duration.ofMinutes(10));
    private final Scheduler scheduler;

    public RootSchedulable(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public LocalDateTime getFireTime() {
        return firetime;
    }

    @Override
    public Callable<LocalDateTime> getCallable() {
        return () -> {
            LOGGER.info("rescheduling.  firetime={}, now={}",
                    getFireTime(), LocalDateTime.now());
            Scheduler.Schedulable newRoot = new RootSchedulable(scheduler);
            List<Scheduler.Schedulable> schedulables = new ArrayList<>(Arrays.asList(newRoot, new LoggerSchedulable()));
            scheduler.schedule(schedulables);

            return newRoot.getFireTime();
        };
    }
}
