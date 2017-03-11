package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An example of worker verticle
 */
public class GoogleCalendarVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarVerticle.class);

  @Override
  public void start() throws Exception {
    LOGGER.info("Starts.");


    vertx.eventBus().consumer("bell-tower.scheduler", message -> {
      String command = message.body().toString();
      if("get schedule".equals(command)) {
        Settings settings = new PropertySettings();
        Duration lookAhead = settings.getCalendarQueryLookAhead();
        Calendar calendar = new CalendarByGoogle(lookAhead);
        LOGGER.debug("getting calendar");
        List<Calendar.Event> events = calendar.getEvents();
        events.forEach(e -> LOGGER.debug("event={}, time={}", e.getTitle(), e.getTime().toLocalDateTime()));
        LOGGER.debug("calendar received.  event count={}", events.size());

        List<SongEvent> songEvents = events.stream()
                .map(SongEvent::new)
                .filter(e -> e.getTime().toLocalDateTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        String json;
        try {
          json = Json.mapper
                  .writer()
                  .forType(new TypeReference<List<SongEvent>>() {
                  })
                  .writeValueAsString(songEvents);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
        LOGGER.info("songEvents forward: {}", json);
        message.reply(json);
      }
    });
  }

  private class SongEvent implements Calendar.Event {
    private final Calendar.Event event;
    private SongEvent(Calendar.Event event) {
      this.event = event;
    }

    @Override
    public ZonedDateTime getTime() {
      ZonedDateTime result;
      if(isMass()) {
        result = event.getTime().minus(Duration.ofMinutes(2));
      } else {
        result = event.getTime();
      }
      return result;
    }

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
}
