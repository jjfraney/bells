package org.flyboy.belltower.panel.scene;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author John J. Franey
 */
@Singleton
public class CalendarScene {


  public DatePicker dtPicker;

  public ListView<String> lstCalendar;

  public Button btnBack;

  @Inject
  Scenes scenes;

  public void switchStatusScene(ActionEvent event) {
    Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
    stage.setScene(scenes.getStatus());
  }
}
