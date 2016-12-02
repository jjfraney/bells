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
    private final Map<OneShotSchedulable, ScheduledOneShot> scheduledOneShots = new HashMap<>();
    private final Map<PeriodicSchedulable, ScheduledPeriodic> scheduledPeriodics = new HashMap<>();

    @Override
    public void scheduleOneShot(List<OneShotSchedulable> schedulables) {

        Set<OneShotSchedulable> toSchedule = new HashSet<>(schedulables);

        Set<OneShotSchedulable> toCancel = new HashSet<>(scheduledOneShots.keySet());

        // only the ones to be canceled remain
        toCancel.removeAll(toSchedule);

        // only the new schedulables remain
        toSchedule.removeAll(scheduledOneShots.keySet());

        // then cancel those to be canceled....
        toCancel.stream()
                .map(scheduledOneShots::get)
                .forEach(ScheduledOneShot::cancel);

        // ..remove them from our registry
        toCancel.forEach(scheduledOneShots::remove);

        // schedule those to be scheduled and add them to the registry
        toSchedule.stream()
                .map(ScheduledOneShot::new)
                .forEach(sd -> scheduledOneShots.put((OneShotSchedulable)sd.schedulable, sd));
        LOGGER.debug("after scheduling.  scheduled={}", scheduledOneShots.values());
    }

    @Override
    public void schedulePeriodic(List<PeriodicSchedulable> schedulables) {
        scheduledPeriodics.values().forEach(ScheduledPeriodic::cancel);
        scheduledPeriodics.clear();
        schedulables.stream()
                .map(ScheduledPeriodic::new)
                .forEach(sd -> scheduledPeriodics.put((PeriodicSchedulable)sd.schedulable, sd));
        LOGGER.debug("after scheduling.  scheduled={}", scheduledPeriodics.values());

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
