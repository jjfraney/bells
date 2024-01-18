package org.flyboy.belltower.panel.scene.belltower;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.flyboy.belltower.panel.scene.belltower.model.Event;
import org.flyboy.belltower.panel.scene.belltower.model.Health;
import org.flyboy.belltower.panel.scene.belltower.model.Health.Status;

/**
 * Periodically updates the belltower ViewModel
 * @author John J. Franey
 */
@Singleton
public class DataGenerator {
  @Inject
  ViewModel viewModel;

  /**
   * A test data supplier
   */
  @Scheduled(every = "5s")
  public void publishHealth() {
    Health oldHealth = viewModel.getBelltowerHealth().get();

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

    viewModel.getBelltowerHealth().set(newHealth);
    viewModel.getBelltowerEvents().addAll(newEvents);

    if (viewModel.getBelltowerEvents().size() > 5) {
      viewModel.getBelltowerEvents().remove(0);
    }
  }
}
