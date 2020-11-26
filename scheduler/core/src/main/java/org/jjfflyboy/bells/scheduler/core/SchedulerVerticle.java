package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jfraney
 */
public class SchedulerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerVerticle.class);
    private Map<SongEvent, Long> scheduledWithTimerId = new HashMap<>();

    @Override
    public void start() throws Exception {


        refreshSongSchedule(true);

        vertx.eventBus().consumer("bell-tower", message -> {
            LOGGER.debug("received command, msg={}", message.body());
            JsonObject msg = (JsonObject) message.body();
            String command = msg.getString("command");
            if ("status".equals(command)) {
                publishStatus();
            } else if("schedule".equals(command)) {
                refreshSongSchedule(false);
            }
        });
    }

    private void refreshSongSchedule(boolean isReschedule) {
        final Settings settings = new PropertySettings();

        vertx.<List<Calendar.Event>> executeBlocking(future -> {
            LOGGER.info("attempting to populate song schedule");
            try {
                Duration lookAhead = settings.getCalendarQueryLookAhead();
                Calendar calendar = new CalendarByGoogle(lookAhead);
                List<Calendar.Event> events = calendar.getEvents();
                future.complete(events);
            } catch(RuntimeException e) {
                future.fail(e.getCause());
            }
        }, res -> {
            if(res.succeeded()) {
                List<Calendar.Event> events = res.result();
                events.forEach(e -> LOGGER.debug("event={}, time={}", e.getTitle(), e.getTime()));
                LOGGER.debug("calendar received.  event count={}", events.size());

                List<SongEvent> songEvents = events.stream()
                        .map(SongEvent::new)
                        .filter(e -> e.getTime().isAfter(ZonedDateTime.now()))
                        .collect(Collectors.toList());


                schedule(songEvents);

                if(isReschedule) {
                    // after success, set timer for next refresh
                    Duration period = settings.getCalendarQueryPeriod();
                    LOGGER.info("calendar period {}s, encoded={}", period.getSeconds(), period);

                    vertx.setTimer(period.getSeconds() * 1000, id -> refreshSongSchedule(true));
                }

            } else {
                int retrySeconds = 20;
                Throwable cause = res.cause();
                LOGGER.error("cannot get schedule ({}), retrying in {}s", cause.getMessage(), retrySeconds);

                if(isReschedule) {
                    // failure retry
                    vertx.setTimer(retrySeconds * 1000, id -> refreshSongSchedule(true));
                }
            }
        });
    }

    /**
     * synchronize the vertx scheduler with the song schedule.
     * Retains timer for unchanged events.  Cancels timer for events which do not appear
     * on new schedule.  Sets timer for events which are new to the schedule.
     * @param songEvents
     */
    private void schedule(List<SongEvent> songEvents) {
        Set<SongEvent> newSchedule = new HashSet<>(songEvents);

        // the existing scheduled events which do not appear on new schedule are to be canceled
        Set<SongEvent> toBeCancelled = new HashSet<>(scheduledWithTimerId.keySet());
        toBeCancelled.removeAll(newSchedule);

        // do not set another timer for events which already have active timer
        newSchedule.removeAll(scheduledWithTimerId.keySet());

        // do work of canceling timer of the canceled events
        toBeCancelled.forEach(this::cancelTimer);

        // do work of setting timer for events that are new to the schedule.
        newSchedule.forEach(songEvent -> {
            setTimer(songEvent);
        });

        publishStatus();

        LOGGER.info("All scheduled songs: {}", scheduledWithTimerId.keySet());
    }

    /**
     * set vertx timer to play song at the event's scheduled time.
     * @param songEvent
     */
    private void setTimer(SongEvent songEvent) {
        Duration duration = Duration.between(ZonedDateTime.now(), songEvent.getTime());
        Long delay = duration.toMillis();
        LOGGER.debug("setting timer for event.  event={}, delay={}, as duration={}", songEvent, delay, duration);
        Long timerId = vertx.setTimer(delay, id -> {
            startSongHandler(songEvent);
        });
        scheduledWithTimerId.put(songEvent, timerId);
    }

    // vertx timer handler which starts a song on the player
    private void startSongHandler(SongEvent songEvent) {
        LOGGER.info("running from timer: {}", songEvent);
        vertx.eventBus().send(
                "bell-tower.player",
                "play " + songEvent.getTitle()
        );
    }

    private void cancelTimer(SongEvent e) {
        Long timerId = scheduledWithTimerId.get(e);
        if (timerId != null) {
            vertx.cancelTimer(timerId);
            scheduledWithTimerId.remove(e);
        }
    }

    private void publishStatus() {
        String statusJson;
        try {
            SchedulerStatus status = new SchedulerStatus();
            List<SongEvent> songEvents = new ArrayList<>(scheduledWithTimerId.keySet());
            Collections.sort(songEvents);
            status.setScheduledSongs(songEvents);
            status.setTime(ZonedDateTime.now(ZoneId.systemDefault()));
            statusJson = Json.mapper
                    .writer()
                    .forType(SchedulerStatus.class)
                    .writeValueAsString(status);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        vertx.eventBus().publish("bell-tower.scheduler.status", statusJson);
    }

    private enum State {ACTIVE, INACTIVE};

    /**
     * controls vertx timers to play song events
     */
    private class Scheduler {
        private Map<SongEvent, SongTimer> songTimers;
        private Schedule schedule;

        private State state = State.INACTIVE;


        /**
         * stop scheduling and playing songs
         */
        public void stop() {
            if(state == State.ACTIVE) {
                songTimers.forEach((st, t) -> t.stop());
                state = State.ACTIVE;
            }
        }

        /**
         * start scheduling and playing songs
         */
        public void start() {
            if(state == State.INACTIVE) {
                songTimers.clear();
                songTimers.forEach(((st, t) -> t.start()));
                state = State.ACTIVE;
            }
        }

        /**
         * replace existing schedule with another
         * @param songEvents list of songs on the new schedule
         */
        public void replace(List<SongEvent> songEvents) {
            stop();
            this.schedule = new Schedule(songEvents);
            start();
        }

        private void timerFired(SongEvent songEvent) {
            LOGGER.info("running from timer: {}", songEvent);
            vertx.eventBus().send(
                    "bell-tower.player",
                    "play " + songEvent.getTitle()
            );
        }

        /**
         * manages the vertx timer for a specific song
         */
        private class SongTimer {
            // one timer at most is runnig
            private Optional<Long> timerId = Optional.empty();
            private final SongEvent songEvent;
            private Function<SongEvent, Void> fired;

            private SongTimer(SongEvent songEvent, Function<SongEvent, Void> fired) {
                this.songEvent = songEvent;
                this.fired = fired;
            }

            private void stop() {
                if(timerId.isPresent()) {
                    vertx.cancelTimer(timerId.get());
                }
                timerId = Optional.empty();
            }

            private void start() {
                Duration duration = Duration.between(ZonedDateTime.now(), songEvent.getTime());
                Long delay = duration.toMillis();
                LOGGER.debug("setting timer for event.  event={}, delay={}, as duration={}", songEvent, delay, duration);
                this.timerId = Optional.of(vertx.setTimer(delay, this::fired));
            }

            private void fired(Long timerId) {
                // if timer fires wrongly, ignore
                // songEvent and timer id must exist, timer id must match
                if(this.timerId.isPresent() && this.timerId.get().equals(timerId)) {
                    fired.apply(songEvent);
                    this.timerId = Optional.empty();
                }
            }
        }


    }


    /**
     * encapsulates a schedule of song events
     */
    private class Schedule {
        private List<SongEvent> songEvents;

        Schedule(List<SongEvent> songEvents) {
            this.songEvents = new ArrayList<>(songEvents);
            this.songEvents.sort(SongEvent::compareTo);
        }

        /**
         * return next song event immediately occuring after now or dateTime, which ever is later.
         * @param zonedDateTime
         * @return
         */
        Optional<SongEvent> getNext(ZonedDateTime zonedDateTime) {
            ZonedDateTime now = ZonedDateTime.now();
            return songEvents.stream()
                    .filter(se -> se.time.isAfter(now))
                    .filter(se -> se.time.isAfter(zonedDateTime))
                    .findFirst();
        }
    }

    /**
     * These describe the events in the calendar, each event tells when a song should be played.
     * The natural order of song events is the natural order of their scheduled time.
     */
    public static class SongEvent implements Calendar.Event, Comparable<SongEvent> {
        private ZonedDateTime time;
        private String title;
        private PropertySettings settings = new PropertySettings();

        private SongEvent() {}
        private SongEvent(Calendar.Event event) {
            boolean isMass = isMass(event.getTitle());
            this.setTitle(isMass ? "call-to-mass.ogg" : event.getTitle());
            this.setTime(event.getTime().minus(isMass? settings.getCallToMassDuration() : Duration.ofMillis(0)));
        }

        @Override
        public ZonedDateTime getTime() {
            return this.time;
        }

        public void setTime(ZonedDateTime time) {
            this.time = time.withZoneSameInstant(ZoneId.of("America/New_York"));
        }

        @Override
        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return this.time + " " + this.title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SongEvent songEvent = (SongEvent) o;
            return Objects.equals(getTime(), songEvent.getTime()) &&
                    Objects.equals(getTitle(), songEvent.getTitle());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getTime(), getTitle());
        }

        @Override
        public int compareTo(SongEvent songEvent) {
            return this.getTime().compareTo(songEvent.getTime());
        }


        private boolean isMass(String title) {
            return title.toLowerCase().startsWith("mass");
        }
    }

    /**
     * Data published to other verticles.
     */
    public static class SchedulerStatus {
        private ZonedDateTime time;
        private List<SongEvent> scheduledSongs;

        public ZonedDateTime getTime() {
            return time;
        }

        public void setTime(ZonedDateTime time) {
            this.time = time;
        }

        public List<SongEvent> getScheduledSongs() {
            return scheduledSongs;
        }

        public void setScheduledSongs(List<SongEvent> scheduledSongs) {
            this.scheduledSongs = scheduledSongs;
        }
    }
}
