package org.flyboy.belltower.panel.scene.timetable;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Singleton;
import java.time.ZonedDateTime;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.flyboy.belltower.panel.scene.timetable.model.Entry;

/**
 * The bell ringing schedule and its controls.
 *
 * @author John J. Franey
 */
@Singleton
public class ViewModel {

  private final SimpleListProperty<Entry> schedule =
      new SimpleListProperty<>(FXCollections.observableArrayList());

  SimpleListProperty<Entry> getScheduled() {
    return schedule;
  }

  @Scheduled(every = "10s")
  public void update() {
    ZonedDateTime dateTime = ZonedDateTime.now()
        .plusHours(2)
        .withMinute(0)
        .withSecond(0);
    String pattern = "call-to-mass.ogg";
    Entry ringEvent = new Entry(dateTime, pattern);
    schedule.add(ringEvent);
    while (schedule.size() >= 5) {
      schedule.remove(0);
    }
  }
}
