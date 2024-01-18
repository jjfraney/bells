package org.flyboy.belltower.panel.scene.belfry;

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


}
