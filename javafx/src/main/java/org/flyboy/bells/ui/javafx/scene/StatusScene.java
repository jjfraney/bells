package org.flyboy.bells.ui.javafx.scene;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flyboy.bells.ui.javafx.tower.BelltowerSceneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scene which controls and obtains status of the belltower.
 */
@Singleton
public class StatusScene implements Initializable {
    private final Logger logger = LoggerFactory.getLogger(StatusScene.class);

    public Button btnBelltower;
    public Button btnCalendar;

    public Button btnStatus;

    public Text txtState;

    public ListView<String> lstCalendar;

    @Inject
    BelltowerSceneModel belltowerSceneModel;

    @Inject
    Scenes scenes;


    public void getStatus(@SuppressWarnings("unused") ActionEvent actionEvent) {
        belltowerSceneModel.requestStatus();
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("starting: url={}, bundle={}", url, resourceBundle);
        txtState.textProperty().bind(belltowerSceneModel.getStatusProperty());
    }

    public void switchBelltowerScene(ActionEvent event) {
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scenes.getBelltower());
    }

    public void switchCalendarScene(ActionEvent event) {
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scenes.getCalendar());
    }
}
