package org.flyboy.belltower.panel.scene.belltower;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import org.flyboy.belltower.panel.scene.belltower.model.Event;
import org.flyboy.belltower.panel.scene.navigation.Controller;

/**
 * UI Controls of the belltower scene.
 */
@Singleton
public class View implements Initializable {


    @FXML
    Label calendarServiceHealth;

    @FXML
    Label timeServiceHealth;

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

        viewModel.getBelltowerHealth()
            .addListener((observableValue, oldHealth, newHealth) -> Platform.runLater(() -> {
            calendarServiceHealth.setText(newHealth.calendarService().toString());
            timeServiceHealth.setText(newHealth.timeService().toString());
            }));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        Consumer<Event> loadEvent = event -> eventText.appendText(event.timestamp().format(formatter) + ": " + event.message() + "\n");

        viewModel.getBelltowerEvents().forEach(loadEvent);

        viewModel.getBelltowerEvents()
            .addListener((observableList, oldValue, newValue) -> Platform.runLater(() -> {
            eventText.clear();
            newValue.forEach(loadEvent);
            }));
    }
}
