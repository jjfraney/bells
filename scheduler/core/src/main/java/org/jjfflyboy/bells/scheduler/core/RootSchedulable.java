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
public class RootSchedulable extends AbstractOneShotSchedulable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RootSchedulable.class);
    private final Scheduler scheduler;

    public RootSchedulable(Scheduler scheduler) {
        super(LocalDateTime.now().plus(Duration.ofMinutes(10)));
        this.scheduler = scheduler;
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
