package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author jfraney
 */
public class SchedulerImpl implements Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private List<Scheduled> scheduleds = new ArrayList<>();

    @Override
    public void schedule(List<Schedulable> schedulables) {
        scheduleds.stream().forEach(Scheduled::cancel);
        scheduleds.clear();
        List<Scheduled> ns = schedulables.stream().map(Scheduled::new).collect(Collectors.toList());
        scheduleds.addAll(ns);
    }

    private final Timer timer = new Timer();

    private class Scheduled {
        private final Scheduler.Schedulable schedulable;

        private final TimerTask timerTask;

        public Scheduled(Scheduler.Schedulable schedulable) {
            this.schedulable = schedulable;

            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        schedulable.getCallable().call();
                    } catch(Exception e) {
                        LOGGER.debug("call had an error", e);
                    }
                }
                public String toString() {
                    return schedulable.getFireTime().toString();
                }
            };

            Duration d = Duration.between(LocalDateTime.now(), schedulable.getFireTime());
            long milli = d.getSeconds() * 1000 + d.getNano()/1000000;
            LOGGER.debug("scheduling timertask.  duration={}, duration={}ms, timerTask={}", d, milli, timerTask);
            timer.schedule(timerTask, milli);
        }
        public void cancel() {
            LOGGER.debug("cancelling a scheduled call, timerTask={}", timerTask);
            timerTask.cancel();
        }
    }

    public static void main(String[] args) {
        SchedulerImpl impl = new SchedulerImpl();

        Schedulable s = new RootSchedulable(impl);
        List<Schedulable> schedulables = new ArrayList<>(Arrays.asList(s));
        impl.schedule(schedulables);
    }
}
