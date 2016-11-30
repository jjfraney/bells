package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * @author jfraney
 */
public class SchedulerByExecutorImpl implements Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(1);
    private final List<Scheduled> scheduleds = new ArrayList<>();

    @Override
    public void schedule(List<Schedulable> schedulables) {
        scheduleds.stream().forEach(Scheduled::cancel);
        scheduleds.clear();
        List<Scheduled> ns = schedulables.stream().map(Scheduled::new).collect(Collectors.toList());
        scheduleds.addAll(ns);
    }


    private class Scheduled {
        private final Schedulable schedulable;
        private ScheduledFuture<Void> future;


        public Scheduled(Schedulable schedulable) {
            this.schedulable = schedulable;

            Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() {
                    try {
                        schedulable.getCallable().call();
                    } catch(Exception e) {
                        LOGGER.debug("call had an error", e);
                    }
                    return null;
                }
                public String toString() {
                    return schedulable.getFireTime().toString();
                }
            };

            Duration d = Duration.between(LocalDateTime.now(), schedulable.getFireTime());
            long milli = d.getSeconds() * 1000 + d.getNano()/1000000;
            LOGGER.debug("scheduling.  duration={}, duration={}ms, schedulable={}", d, milli, schedulable);
            this.future = scheduler.schedule(callable, milli, TimeUnit.MILLISECONDS);
        }
        public void cancel() {
            LOGGER.debug("cancelling a scheduled call, this={}", schedulable);
            this.future.cancel(true);
        }
    }

    public static void main(String[] args) {
        SchedulerByExecutorImpl impl = new SchedulerByExecutorImpl();

        Schedulable s = new GoogleCalendarSchedulable(impl);
        List<Schedulable> schedulables = new ArrayList<>(Arrays.asList(s));
        impl.schedule(schedulables);
    }
}
