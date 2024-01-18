package org.flyboy.belltower.panel.scene.timetable;

import jakarta.inject.Singleton;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.flyboy.belltower.panel.scene.timetable.model.Slot;

/**
 * The bell ringing schedule and its controls.
 *
 * @author John J. Franey
 */
@Singleton
public class ViewModel {

  private final SimpleListProperty<Slot> schedule =
      new SimpleListProperty<>(FXCollections.observableArrayList());

  SimpleListProperty<Slot> getScheduled() {
    return schedule;
  }

}
