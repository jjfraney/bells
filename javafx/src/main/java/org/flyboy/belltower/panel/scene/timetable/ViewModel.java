package org.flyboy.belltower.panel.scene.timetable;

import java.time.ZonedDateTime;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;

/**
 * The bell ringing schedule and its controls.
 *
 * @author John J. Franey
 */
public class ViewModel {

  private SimpleObjectProperty<List<RingEvent>> ringEvents;

  public record RingEvent(ZonedDateTime dateTime, String patternName) {

  }

}
