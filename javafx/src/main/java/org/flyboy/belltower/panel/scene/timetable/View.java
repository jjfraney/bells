package org.flyboy.belltower.panel.scene.timetable;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import org.flyboy.belltower.panel.scene.navigation.Controller;
import org.flyboy.belltower.panel.scene.timetable.model.Entry;

/**
 * @author John J. Franey
 */
@Singleton
public class View implements Initializable {

  @FXML
  TextArea eventText;

  @FXML
  MenuBar navigation;

  @FXML
  Controller navigationController;

  @Inject
  ViewModel viewModel;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    viewModel.getScheduled().addListener(new ScheduleChangeListener());
  }


  private class ScheduleChangeListener implements ChangeListener<ObservableList<Entry>> {

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    /**
     * @param observableValue of the ring event list
     * @param former content of the list.
     * @param latest content of the list
     */
    @Override
    public void changed(ObservableValue<? extends ObservableList<Entry>> observableValue,
        ObservableList<Entry> former, ObservableList<Entry> latest) {

      Platform.runLater(() -> updateFrom(latest));
    }

    /**
     * updates the UI control from the observed list.
     * @param observableList of events
     */
    private void updateFrom(ObservableList<Entry> observableList) {
      eventText.clear();
      observableList.forEach(event -> {
        String text = event.dateTime().format(formatter) + ": " + event.eventDescription() + "\n";
        eventText.appendText(text);
      });
    }
  }

}
