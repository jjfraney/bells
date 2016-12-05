package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
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
        super(Duration.ofMinutes(30));
        this.songScheduler = new SchedulerByExecutorImpl();
    }

    public List<Scheduler.OneShotSchedulable> getCalendar() {
        LOGGER.info("getting calendar");
        Calendar calendar = new CalendarByGoogle();
        List<Calendar.Event> events = calendar.getEvents();
        events.stream()
                .forEach(e -> LOGGER.debug("event={}, time={}", e.getTitle(), e.getTime().toLocalDateTime()));
        LOGGER.debug("calendar received.  event count={}", events.size());
        List<Scheduler.OneShotSchedulable> schedulables = events.stream()
                .map(SongEvent::new)
                .filter(e -> e.getTime().toLocalDateTime().isAfter(LocalDateTime.now()))
                .map(e -> new PlaySongSchedulable(e.getTitle(), e.getTime().toLocalDateTime()))
                .collect(Collectors.toList());
        return schedulables;
    }

    private class SongEvent implements Calendar.Event {
        private final Calendar.Event event;
        public SongEvent(Calendar.Event event) {
            this.event = event;
        }

        @Override
        public ZonedDateTime getTime() {
            ZonedDateTime result;
            if(isMass()) {
                result = event.getTime().minusMinutes(2);
            } else {
                result = event.getTime();
            }
            return result;
        }

        /**
         * reads the delegate's title and creates a song title.
         * @return
         */
        @Override
        public String getTitle() {
            String result = null;
            if(isMass()) {
                result = "call-to-mass.ogg";
            } else if(isPlay()) {
                result = parseSongTitle();
            }
            return result;
        }

        private String parseSongTitle() {
            return event.getTitle().replaceFirst("play", "").trim();
        }
        private boolean isPlay() {
            return event.getTitle().startsWith("play");
        }

        private boolean isMass() {
            return event.getTitle().toLowerCase().startsWith("mass");
        }
    }
    @Override
    public Callable<Void> getCallable() {
        return () -> {
            songScheduler.scheduleOneShot(new ArrayList<>(getCalendar()));
            return null;
        };
    }

}
