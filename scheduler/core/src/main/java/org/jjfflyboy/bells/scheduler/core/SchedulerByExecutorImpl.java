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
        return scheduled;
    }
    private ScheduledPeriodic schedule(PeriodicSchedulable schedulable) {
        LOGGER.debug("Scheduling periodic.  schedulable={}", schedulable);
        ScheduledPeriodic scheduled = new ScheduledPeriodic(schedulable);
        return scheduled;
    }
    private void cancel(OneShotSchedulable schedulable) {
        LOGGER.debug("Cancelling one shot.  schedulable={}", schedulable);
        scheduledOneShots.get(schedulable).cancel();
    }
    private void cancel(PeriodicSchedulable schedulable) {
        LOGGER.debug("Cancelling periodic.  schedulable={}", schedulable);
        scheduledPeriodics.get(schedulable).cancel();
    }


    private abstract class Scheduled {
        protected final Callable callable;
        protected final Schedulable schedulable;

        public Scheduled(OneShotSchedulable schedulable) {
            this.callable = schedulable.getCallable();
            this.schedulable = schedulable;
        }
        public Scheduled(PeriodicSchedulable schedulable) {
            this.callable = schedulable.getCallable();
            this.schedulable = schedulable;
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
            scheduledPeriodics.put(schedulable, this);

        }

        @Override
        public void cancel() {
            future.cancel(true);
            scheduledPeriodics.remove(schedulable);
        }

        Runnable wrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    callable.call();
                } catch(Exception e) {
                    LOGGER.debug("call had an error", e);
                } finally {
                    scheduledPeriodics.remove(schedulable);
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
            scheduledOneShots.put(schedulable, this);
        }

        @Override
        public void cancel() {
            future.cancel(true);
            scheduledOneShots.remove(schedulable);
        }

        Callable<Void> wrapper = new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    callable.call();
                } catch(Exception e) {
                    LOGGER.debug("call had an error", e);
                } finally {
                    scheduledOneShots.remove(schedulable);
                }
                return null;
            }
        };

    }

    public static void main(String[] args) {
        Scheduler masterScheduler = new SchedulerByExecutorImpl();
        Settings settings = new PropertySettings();

        GoogleCalendarSchedulable s = new GoogleCalendarSchedulable();
        List<PeriodicSchedulable> schedulables = new ArrayList<>(Arrays.asList(s));
        if(settings.isDebug()) {
            schedulables.add(new DebugPlaySongSchedulable());
        }
        masterScheduler.schedulePeriodic(schedulables);
    }
}
