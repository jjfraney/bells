package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;


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
        toCancel.forEach(this::cancel);

        // schedule those to be scheduled and add them to the registry
        toSchedule.forEach(this::schedule);

        LOGGER.debug("all scheduled one shots{}", scheduledOneShots.keySet());
    }

    @Override
    public void schedulePeriodic(List<PeriodicSchedulable> schedulables) {
        scheduledPeriodics.keySet().forEach(this::cancel);
        schedulables.forEach(this::schedule);
        LOGGER.debug("all scheduled periodics. {}", scheduledPeriodics.keySet());
    }

    private ScheduledOneShot schedule(OneShotSchedulable schedulable) {
        LOGGER.debug("Scheduling one shot.  schedulable={}", schedulable);
        ScheduledOneShot scheduled = new ScheduledOneShot(schedulable);
        scheduledOneShots.put(schedulable, scheduled);
        return scheduled;
    }
    private ScheduledPeriodic schedule(PeriodicSchedulable schedulable) {
        LOGGER.debug("Scheduling periodic.  schedulable={}", schedulable);
        ScheduledPeriodic scheduled = new ScheduledPeriodic(schedulable);
        scheduledPeriodics.put(schedulable, scheduled);
        return scheduled;
    }
    private void cancel(OneShotSchedulable schedulable) {
        LOGGER.debug("Cancelling one shot.  schedulable={}", schedulable);
        scheduledOneShots.get(schedulable).cancel();
        scheduledOneShots.remove(schedulable);
    }
    private void cancel(PeriodicSchedulable schedulable) {
        LOGGER.debug("Cancelling periodic.  schedulable={}", schedulable);
        scheduledPeriodics.get(schedulable).cancel();
        scheduledPeriodics.remove(schedulable);
    }


    private abstract class Scheduled {
        protected final Callable callable;

        public Scheduled(OneShotSchedulable schedulable) {
            this.callable = schedulable.getCallable();
        }
        public Scheduled(PeriodicSchedulable schedulable) {
            this.callable = schedulable.getCallable();
         }
        public abstract void cancel();

    }

    private class ScheduledPeriodic extends Scheduled {
        private ScheduledFuture<?> future;

        public ScheduledPeriodic(PeriodicSchedulable schedulable) {
            super(schedulable);
            long initialDelay = 1000L;
            long delay = schedulable.getPeriod().toMillis();
            this.future = scheduler.scheduleAtFixedRate(wrapper, initialDelay, delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public void cancel() {
            future.cancel(true);
        }

        Runnable wrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    callable.call();
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
            this.future = scheduler.schedule(wrapper, d.toMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public void cancel() {
            future.cancel(true);
        }

        Callable<Void> wrapper = new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    callable.call();
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
