package org.flyboy.belltower.panel.scene.belltower.model;

import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
public record Event(ZonedDateTime timestamp, String message) {

  public Event(String message) {
    this(ZonedDateTime.now(), message);
  }
}
