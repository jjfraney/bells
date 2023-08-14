package org.flyboy.belltower.panel.scene.belltower;

import io.quarkus.scheduler.Scheduled;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.flyboy.belltower.panel.scene.belltower.model.Event;
import org.flyboy.belltower.panel.scene.belltower.model.Health;
import org.flyboy.belltower.panel.scene.belltower.model.Health.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John J. Franey
 */
public class ViewModel {

  private final SimpleObjectProperty<Health> belltowerHealth = new SimpleObjectProperty<>();
  private final SimpleListProperty<Event> belltowerEvents;
  Logger logger = LoggerFactory.getLogger(ViewModel.class);

  public ViewModel() {
    belltowerHealth.set(new Health(Status.UNKNOWN, Status.UNKNOWN));

    ObservableList<Event> events = FXCollections.observableArrayList();
    this.belltowerEvents = new SimpleListProperty<>(events);

    logger.info("starts");
    belltowerEvents.add(new Event("Ready to receive events."));

  }

  public SimpleObjectProperty<Health> getBelltowerHealth() {
    return belltowerHealth;
  }

  public SimpleListProperty<Event> getBelltowerEvents() {
    return belltowerEvents;
  }

  @Scheduled(every = "5s")
  public void publishHealth() {
    Health oldHealth = belltowerHealth.get();


    final List<Event> newEvents = new ArrayList<>();

    Random rn = new Random();
    int random = rn.nextInt(60);

    Status newCalStatus = oldHealth == null ? Status.UNKNOWN : oldHealth.getCalendarService();
    if ((random % 4) == 0) {
      newCalStatus = newCalStatus == Status.UP ? Status.DOWN : Status.UP;
      newEvents.add(new Event("Calendar service state change: " + newCalStatus));
    }

    Status newTimStatus = oldHealth == null ? Status.UNKNOWN : oldHealth.getTimeService();
    if ((random % 3) == 0) {
      newTimStatus = newTimStatus == Status.UP ? Status.DOWN : Status.UP;
      newEvents.add(new Event("Time service state change: " + newTimStatus));
    }

    final Health newHealth = new Health(newTimStatus, newCalStatus);

    Platform.runLater(() -> {
      belltowerHealth.set(newHealth);
      belltowerEvents.addAll(newEvents);

      if (belltowerEvents.size() > 5) {
        belltowerEvents.remove(0);
      }
    });

  }

  public void publishEvents() {
    Event event = new Event(ZonedDateTime.now(), "time: " + ZonedDateTime.now().toEpochSecond());

    Platform.runLater(() -> {
      logger.info("timeout");

      belltowerEvents.add(event);
      if (belltowerEvents.size() > 5) {
        belltowerEvents.remove(0);
      }
    });
  }


}
