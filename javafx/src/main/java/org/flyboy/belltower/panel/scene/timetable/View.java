package org.flyboy.belltower.panel.scene.timetable;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URL;
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
import org.flyboy.belltower.panel.scene.timetable.model.Slot;

/**
 * @author John J. Franey
 */
@Singleton
public class View implements Initializable {

  @FXML
  TextArea slotText;

  @FXML
  MenuBar navigation;

  @FXML
  Controller navigationController;

  @Inject
  ViewModel viewModel;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    viewModel.getScheduled().addListener(new ScheduleChangeListener(this));
  }

  /**
   * Class that responds to new change of the
   * @author John J. Franey
   */
  static class ScheduleChangeListener implements ChangeListener<ObservableList<Slot>> {

    private final View view;
    private final SlotFormatter slotFormatter = new SlotFormatter();

    public ScheduleChangeListener(View view) {
      this.view = view;
    }

    /**
     * @param observableValue of the playlist
     * @param former          content of the playlist.
     * @param latest          content of the playlist
     */
    @Override
    public void changed(ObservableValue<? extends ObservableList<Slot>> observableValue,
        ObservableList<Slot> former, ObservableList<Slot> latest) {

      Platform.runLater(() -> updateFrom(latest));
    }

    /**
     * updates the UI control from the observed list.
     *
     * @param observableList of events
     */
    private void updateFrom(ObservableList<Slot> observableList) {
      view.slotText.clear();
      observableList.forEach(event -> {
        view.slotText.appendText(slotFormatter.toView(event));
      });
    }
  }
}


