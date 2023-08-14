package org.flyboy.belltower.panel.scene.belfry;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * ViewModel contains the bells. This model class controls and tracks state of the bells.
 *
 * @author John J. Franey
 */
public class ViewModel {

  private SimpleObjectProperty<Status> status;

  private SimpleStringProperty activeBellPattern;

  public enum Status {RINGING, IDLE}

}
