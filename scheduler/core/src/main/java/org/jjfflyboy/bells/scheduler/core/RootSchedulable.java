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
public class RootSchedulable implements Scheduler.OneShotSchedulable {
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
    public Callable<Void> getCallable() {
        return () -> {
            LOGGER.info("rescheduling.  firetime={}, now={}",
                    getFireTime(), LocalDateTime.now());
            Scheduler.OneShotSchedulable newRoot = new RootSchedulable(scheduler);
            List<Scheduler.OneShotSchedulable> schedulables = new ArrayList<>(Arrays.asList(newRoot, new LoggerSchedulable()));
            scheduler.scheduleOneShot(schedulables);

            return null;
        };
    }
}
