package org.flyboy.belltower.panel.scene.belfry;

import static javafx.application.Platform.runLater;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 * UI controls of the belfry scene.
 *
 * @author John J. Franey
 */
@Singleton
public class View implements Initializable {

  public Label bellStatus;

  public Label bellPattern;

  @Inject
  ViewModel viewModel;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    // listener observes the status
    ChangeListener<String> statusListener = (observable, former, latest) ->
        runLater(() -> bellStatus.setText(latest));
    viewModel.getBelfryStatus().addListener(statusListener);

    // listener observes the pattern
    ChangeListener<String> patternListener = (observable, former, latest) ->
        runLater(() -> bellPattern.setText(latest));
    viewModel.getBelfryPattern().addListener(patternListener);
  }
}
