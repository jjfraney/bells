package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author jfraney
 */
public class GoogleCalendarSchedulable extends AbstractSchedulable {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarSchedulable.class);
    private final Scheduler rootScheduler;

    public GoogleCalendarSchedulable(Scheduler rootScheduler) {
        super(LocalDateTime.now().plus(Duration.ofMinutes(1)));
        this.rootScheduler = rootScheduler;
        getCalendar();
    }

    private List<Scheduler.Schedulable> getCalendar() {
        LOGGER.info("getting calendar");
        Calendar calendar = new CalendarByGoogle();
        List<Calendar.Event> events = calendar.getEvents();
        events.stream()
                .forEach(e -> LOGGER.debug("event={}, time={}", e.getTitle(), e.getTime().toLocalDateTime()));
        LOGGER.debug("calendar received");
        List<Scheduler.Schedulable> callToMass = events.stream()
                .filter(e -> "mass".equals(e.getTitle()))
                .map(e -> new PlaySongSchedulable("call-to-mass.ogg", e.getTime().minusMinutes(2).toLocalDateTime()))
                .collect(Collectors.toList());
        return callToMass;
    }
    @Override
    public Callable<LocalDateTime> getCallable() {
        return () -> {
            List<Scheduler.Schedulable> songTimes = getCalendar();
            List<Scheduler.Schedulable> schedulables = new ArrayList<>(songTimes);

            Scheduler.Schedulable newRoot = new GoogleCalendarSchedulable(rootScheduler);
            schedulables.add(newRoot);

            rootScheduler.schedule(schedulables);
            return getFireTime();
        };
    }

}
