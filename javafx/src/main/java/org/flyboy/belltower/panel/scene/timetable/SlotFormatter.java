package org.flyboy.belltower.panel.scene.timetable;

import static java.util.Objects.isNull;

import java.time.format.DateTimeFormatter;
import org.flyboy.belltower.panel.scene.timetable.model.Slot;

/**
 * @author John J. Franey
 */
public class SlotFormatter {
  final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

  String toView(Slot slot) {
    String result = "<null>";
    if (!isNull(slot) && !isNull(slot.dateTime()) && !isNull(slot.title())) {
      result = slot.dateTime().format(formatter) + ": " + slot.title() + "\n";
    }
    return  result;
  }

}
