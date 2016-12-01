package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author jfraney
 */
public class GoogleCalendarSchedulable extends AbstractPeriodicSchedulable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarSchedulable.class);
    private final Scheduler songScheduler;

    public GoogleCalendarSchedulable() {
        super(Duration.ofMinutes(3));
        this.songScheduler = new SchedulerByExecutorImpl();
    }

    public List<Scheduler.OneShotSchedulable> getCalendar() {
        LOGGER.info("getting calendar");
        Calendar calendar = new CalendarByGoogle();
        List<Calendar.Event> events = calendar.getEvents();
        events.stream()
                .forEach(e -> LOGGER.debug("event={}, time={}", e.getTitle(), e.getTime().toLocalDateTime()));
        LOGGER.debug("calendar received");
        List<Scheduler.OneShotSchedulable> callToMass = events.stream()
                .filter(e -> "mass".equals(e.getTitle()))
                .map(e -> new PlaySongSchedulable("call-to-mass.ogg", e.getTime().minusMinutes(2).toLocalDateTime()))
                .collect(Collectors.toList());
        return callToMass;
    }
    @Override
    public Callable<Void> getCallable() {
        return () -> {
            songScheduler.scheduleOneShot(new ArrayList<>(getCalendar()));
            return null;
        };
    }

}
