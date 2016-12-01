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
    private final Set<Scheduled> scheduleds = new HashSet<>();

    @Override
    public void scheduleOneShot(List<OneShotSchedulable> schedulables) {

        // get those that exist but are not going to be added
        Set<ScheduledOneShot> exists = scheduleds.stream()
                .filter(s -> s instanceof  ScheduledOneShot)
                .map(s -> (ScheduledOneShot) s)
                .collect(Collectors.toSet());

        Set<OneShotSchedulable> toCancel = exists.stream()
                .map(s -> (OneShotSchedulable)s.schedulable)
                .collect(Collectors.toSet());
        Set<OneShotSchedulable> toSchedule = new HashSet<>(schedulables);

        // only the ones to be canceled remain
        toCancel.removeAll(toSchedule);

        // cancel the existing one shots
        exists.stream()
                .filter(s-> toCancel.contains(s.schedulable))
                .forEach(s-> s.cancel());

        // remove the existing canceled one shots for the tracking set
        List<ScheduledOneShot> canceled = exists.stream()
                .filter(s-> toCancel.contains(s.schedulable))
                .collect(Collectors.toList());
        scheduleds.removeAll(canceled);
        exists.removeAll(canceled);


        // only the ones to add remain
        List<OneShotSchedulable> remaining = exists.stream()
                .map(s->(OneShotSchedulable)s.schedulable)
                .collect(Collectors.toList());
        toSchedule.removeAll(remaining);

        // schedule the ones to add
        List<Scheduled> scheduled = toSchedule.stream()
                .map(ScheduledOneShot::new)
                .collect(Collectors.toList());
        scheduleds.addAll(scheduled);
        LOGGER.debug("after scheduling.  scheduled={}", scheduleds);
    }

    @Override
    public void schedulePeriodic(List<PeriodicSchedulable> schedulables) {
        scheduleds.stream()
                .filter(s -> s instanceof  ScheduledPeriodic)
                .forEach(Scheduled::cancel);
        List<Scheduled> ns = schedulables.stream().map(ScheduledPeriodic::new).collect(Collectors.toList());
        scheduleds.addAll(ns);
    }

    private abstract class Scheduled {
        protected final Schedulable schedulable;

        public Scheduled(OneShotSchedulable schedulable) {
            this.schedulable = schedulable;
        }
        public Scheduled(PeriodicSchedulable schedulable) {
            this.schedulable = schedulable;
         }
        public abstract void cancel();

        @Override
        public String toString() {return schedulable.toString();}
    }

    private class ScheduledPeriodic extends Scheduled {
        private ScheduledFuture<?> future;

        public ScheduledPeriodic(PeriodicSchedulable schedulable) {
            super(schedulable);
            long initialDelay = 1000L;
            long delay = schedulable.getPeriod().toMillis();
            LOGGER.debug("periodic.  schedulable={}", schedulable);
            this.future = scheduler.scheduleAtFixedRate(runnable, initialDelay, delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public void cancel() {
            LOGGER.debug("Cancelling schedulable.  schedulable={}", schedulable);
            future.cancel(true);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    schedulable.getCallable().call();
                } catch(Exception e) {
                    LOGGER.debug("call had an error", e);
                }
            }
        };
    }
    private class ScheduledOneShot extends Scheduled {
        private ScheduledFuture<Void> future;

        public ScheduledOneShot(OneShotSchedulable schedulable) {
            super(schedulable);
            Duration d = Duration.between(LocalDateTime.now(), schedulable.getFireTime());
            LOGGER.debug("one shot.  schedulable={}", schedulable);
            this.future = scheduler.schedule(callable, d.toMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public void cancel() {
            LOGGER.debug("Cancelling schedulable.  schedulable={}", schedulable);
            future.cancel(true);
        }

        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    return schedulable.getCallable().call();
                } catch(Exception e) {
                    LOGGER.debug("call had an error", e);
                }
                return null;
            }
        };

    }

    public static void main(String[] args) {
        Scheduler masterScheduler = new SchedulerByExecutorImpl();

        GoogleCalendarSchedulable s = new GoogleCalendarSchedulable();
        List<PeriodicSchedulable> schedulables = new ArrayList<>(Arrays.asList(s));
        masterScheduler.schedulePeriodic(schedulables);
    }
}
