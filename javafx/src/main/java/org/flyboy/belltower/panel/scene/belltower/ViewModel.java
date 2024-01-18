package org.flyboy.belltower.panel.scene.belltower;

import jakarta.inject.Singleton;
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


}
