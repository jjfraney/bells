package org.flyboy.belltower.panel.scene.belfry;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Singleton;
import javafx.beans.property.SimpleStringProperty;

/**
 * ViewModel contains the bells. This model class controls and tracks state of the bells.
 *
 * @author John J. Franey
 */

@Singleton
public class ViewModel {

  private final SimpleStringProperty belfryStatus = new SimpleStringProperty();

  private final SimpleStringProperty belfryPattern = new SimpleStringProperty();

  SimpleStringProperty getBelfryStatus() {
    return belfryStatus;
  }

  SimpleStringProperty getBelfryPattern() {
    return belfryPattern;
  }

  public ViewModel() {
    belfryStatus.set("UNKNOWN");
    belfryPattern.set("");
  }

  /**
   * a test data supplier.
   */
  @Scheduled(every = "5s")
  public void updateStatus() {
    String status = belfryStatus.get();

    if (status.equals("IDLE")) {
      belfryStatus.set("RINGING");
      belfryPattern.set("call-to-mass.ogg");
    } else {
      belfryStatus.set("IDLE");
      belfryPattern.set("");
    }
  }
}
