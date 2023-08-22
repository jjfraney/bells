package org.flyboy.belltower.panel.scene.navigation;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * @author John J. Franey
 */
@Singleton
public class Controller implements Initializable  {

  @FXML MenuItem timetableItem;

  @FXML
  MenuItem belltowerItem;

  @FXML
  MenuItem belfryItem;

  @Inject
  @Belltower
  Scene belltower;

  @Inject
  @Belfry
  Scene belfry;

  @Inject
  @Timetable
  Scene timetable;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    belfryItem.setOnAction(e -> getStage(belfryItem).setScene(belfry));
    belltowerItem.setOnAction(e -> getStage(belltowerItem).setScene(belltower));
    timetableItem.setOnAction(e -> getStage(timetableItem).setScene(timetable));
  }

  private Stage getStage(MenuItem menuItem) {
    return (Stage)menuItem.getParentPopup().getOwnerWindow();
  }
}
