package org.flyboy.belltower.panel.scene.belltower;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import org.flyboy.belltower.panel.scene.belltower.model.Event;
import org.flyboy.belltower.panel.scene.belltower.model.Health;
import org.flyboy.belltower.panel.scene.belltower.model.Health.Status;

/**
 * @author John J. Franey
 */
@Singleton
public class ViewModel {

  private final SimpleObjectProperty<Health> belltowerHealth = new SimpleObjectProperty<>();
  private final SimpleListProperty<Event> belltowerEvents =
      new SimpleListProperty<>(FXCollections.observableArrayList());

  public ViewModel() {
    belltowerHealth.set(new Health(Status.UNKNOWN, Status.UNKNOWN));
    belltowerEvents.add(new Event("Ready to receive events."));
  }

  SimpleObjectProperty<Health> getBelltowerHealth() {
    return belltowerHealth;
  }

  SimpleListProperty<Event> getBelltowerEvents() {
    return belltowerEvents;
  }

  /**
   * A test data supplier
   */
  @Scheduled(every = "5s")
  public void publishHealth() {
    Health oldHealth = belltowerHealth.get();

    final List<Event> newEvents = new ArrayList<>();

    final int random = new Random().nextInt(60);

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

    belltowerHealth.set(newHealth);
    belltowerEvents.addAll(newEvents);

    if (belltowerEvents.size() > 5) {
      belltowerEvents.remove(0);
    }
  }
}
