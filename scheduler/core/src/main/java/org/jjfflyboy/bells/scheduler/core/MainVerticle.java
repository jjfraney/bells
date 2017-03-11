package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * An example illustrating how worker verticles can be deployed and how to interact with them.
 *
 * This example prints the name of the current thread at various locations to exhibit the event loop <-> worker
 * thread switches.
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    private Map<SongEvent, Long> scheduledWithTimerId = new HashMap<>();

    // Convenience method so you can run it in your IDE
  public static void main(String[] args) {

      Json.mapper.findAndRegisterModules();
      Json.mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
      Runner.runExample(MainVerticle.class);

  }

  private void schedule(AsyncResult<Message<String>> r) {
      List<SongEvent> events;
      try {
          String bdy = r.result().body();
          ObjectReader reader = Json.mapper.readerFor(new TypeReference<List<SongEvent>>() {});
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
      toBeCancelled.forEach( e -> {
          Long timerId = scheduledWithTimerId.get(e);
          if(timerId != null) {
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

      LOGGER.info("All scheduled songs: {}", scheduledWithTimerId.keySet());
  }



  @Override
  public void start() throws Exception {
    vertx
        .deployVerticle("org.jjfflyboy.bells.scheduler.core.GoogleCalendarVerticle",
            new DeploymentOptions().setWorker(true));
    vertx.deployVerticle("org.jjfflyboy.bells.scheduler.core.PlayVerticle", new DeploymentOptions().setWorker(true));

    sendScheduleRequest();

    Settings settings = new PropertySettings();
    Duration period = settings.getCalendarQueryPeriod();
    LOGGER.info("calendar period {}s, encoded={}", period.getSeconds(), period);
    vertx.setPeriodic(period.getSeconds() * 1000, id -> sendScheduleRequest());


  }
    private void sendScheduleRequest() {
        vertx.eventBus().send("bell-tower.scheduler", "get schedule", this::schedule);
    }

    public static class SongEvent implements Calendar.Event {
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
    }
}
