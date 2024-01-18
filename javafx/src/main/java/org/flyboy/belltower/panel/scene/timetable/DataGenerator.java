package org.flyboy.belltower.panel.scene.timetable;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.ZonedDateTime;
import org.flyboy.belltower.panel.scene.timetable.model.Slot;

/**
 * Periodically updates the timetable ViewModel
 * @author John J. Franey
 */

@Singleton
public class DataGenerator {
  @Inject ViewModel viewModel;

  @Scheduled(every = "10s")
  public void update() {
    ZonedDateTime dateTime = ZonedDateTime.now()
        .plusHours(2)
        .withMinute(0)
        .withSecond(0);
    String pattern = "call-to-mass.ogg";
    Slot ringEvent = new Slot(dateTime, pattern);
    viewModel.getScheduled().add(ringEvent);
    while (viewModel.getScheduled().size() >= 5) {
      viewModel.getScheduled().remove(0);
    }
  }
}
