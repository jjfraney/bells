package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author jfraney
 */
public class SchedulerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerVerticle.class);
    private Map<SongEvent, Long> scheduledWithTimerId = new HashMap<>();

    private void schedule(AsyncResult<Message<String>> r) {
        List<SongEvent> events;
        try {
            Objects.nonNull(r);
            Objects.nonNull(r.result());
            Objects.nonNull(r.result().body());
            String bdy = r.result().body();
            ObjectReader reader = Json.mapper.readerFor(new TypeReference<List<SongEvent>>() {
            });
            events = reader.readValue(bdy);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Set<SongEvent> toBeScheduled = new HashSet<>(events);
        Set<SongEvent> toBeCancelled = new HashSet<>(scheduledWithTimerId.keySet());

        // only the ones to be canceled remain
        toBeCancelled.removeAll(toBeScheduled);

        // only the ones that are not already scheduled will remain.
        toBeScheduled.removeAll(scheduledWithTimerId.keySet());

        // now cancel some.
        toBeCancelled.forEach(e -> {
            Long timerId = scheduledWithTimerId.get(e);
            if (timerId != null) {
                vertx.cancelTimer(timerId);
                scheduledWithTimerId.remove(e);
            }
        });

        // now schedule some.
        toBeScheduled.forEach(songEvent -> {
            Duration duration = Duration.between(LocalDateTime.now(), songEvent.getTime());
            Long delay = duration.toMillis();
            LOGGER.debug("setting timer for event.  event={}, delay={}, as duration={}", songEvent, delay, duration);
            Long timerId = vertx.setTimer(delay, id -> {
                LOGGER.info("running from timer: {}", songEvent);
                vertx.eventBus().send(
                        "bell-tower.player",
                        "play " + songEvent.getTitle()
                );
            });
            scheduledWithTimerId.put(songEvent, timerId);
        });

        publishStatus();

        LOGGER.info("All scheduled songs: {}", scheduledWithTimerId.keySet());
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


    @Override
    public void start() throws Exception {

        sendScheduleRequest();

        Settings settings = new PropertySettings();
        Duration period = settings.getCalendarQueryPeriod();
        LOGGER.info("calendar period {}s, encoded={}", period.getSeconds(), period);
        vertx.setPeriodic(period.getSeconds() * 1000, id -> sendScheduleRequest());

        vertx.eventBus().consumer("bell-tower", message -> {
            LOGGER.debug("received command, msg={}", message.body());
            JsonObject msg = (JsonObject) message.body();
            String command = msg.getString("command");
            if ("status".equals(command)) {
                publishStatus();
            }
        });
    }

    private void sendScheduleRequest() {
        vertx.eventBus().send("bell-tower.scheduler", "get schedule", this::schedule);
    }

    /**
     * These describe the events in the calendar, each event tells when a song should be played.
     * The natural order of song events is the natural order of their scheduled time.
     */
    public static class SongEvent implements Calendar.Event, Comparable<SongEvent> {
        private ZonedDateTime time;
        private String title;

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
