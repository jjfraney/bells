package org.flyboy.belltower.panel.scene.timetable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.flyboy.belltower.panel.scene.timetable.model.Slot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author John J. Franey
 */
public class SlotFormatterTest {

  SlotFormatter slotFormatter;

  @BeforeEach
  public void beforeEach() {
    this.slotFormatter = new SlotFormatter();
  }

  @Test
  public void testNull() {
    String result = slotFormatter.toView(null);
    Assertions.assertEquals("<null>", result);
  }

  @Test
  public void testNullDate() {
    Slot slot = new Slot(null, "my.ogg");
    String result = slotFormatter.toView(slot);
    Assertions.assertEquals("<null>", result);
  }

  @Test
  public void testNullTitle() {
    ZonedDateTime someTime = ZonedDateTime.now();
    Slot slot = new Slot(someTime, null);
    String result = slotFormatter.toView(slot);
    Assertions.assertEquals("<null>", result);
  }

  @Test
  public void testValidSlot() {
    ZonedDateTime someTime = ZonedDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
    String title = "theSong.ogg";
    String expected = formatter.format(someTime) + ": " + title + "\n";

    Slot slot = new Slot(someTime, title);
    String actual = slotFormatter.toView(slot);
    Assertions.assertEquals(expected, actual);
  }

}
