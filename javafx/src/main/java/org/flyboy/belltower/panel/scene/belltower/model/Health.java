package org.flyboy.belltower.panel.scene.belltower.model;

/**
 * @author John J. Franey
 */
public record Health(Status timeService, Status calendarService) {

  public enum Status {UP, DOWN, UNKNOWN}

  public Status getTimeService() {
    return timeService;
  }
  public Status getCalendarService() {
    return calendarService;
  }

}
