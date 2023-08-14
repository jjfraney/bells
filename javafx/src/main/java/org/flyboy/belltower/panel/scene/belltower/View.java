package org.flyboy.belltower.panel.scene.belltower;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.flyboy.belltower.panel.scene.belltower.model.Event;

/**
 * UI Controls of the belltower scene.
 */
@Singleton
public class View implements Initializable {


    public Label calendarServiceHealth;

    public Label timeServiceHealth;

    public TextArea eventText;

    @Inject
    ViewModel model;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        model.getBelltowerHealth().addListener((observableValue, oldHealth, newHealth) -> {
            calendarServiceHealth.setText(newHealth.calendarService().toString());
            timeServiceHealth.setText(newHealth.timeService().toString());
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        Consumer<Event> loadEvent = event -> eventText.appendText(event.timestamp().format(formatter) + ": " + event.message() + "\n");

        model.getBelltowerEvents().forEach(loadEvent);

        model.getBelltowerEvents().addListener((observableList, oldValue, newValue) -> {
            eventText.clear();
            newValue.forEach(loadEvent);
        });


    }
}
