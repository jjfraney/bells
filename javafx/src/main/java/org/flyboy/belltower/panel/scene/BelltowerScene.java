package org.flyboy.belltower.panel.scene;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flyboy.belltower.panel.tower.BelltowerSceneModel;
import org.flyboy.belltower.panel.tower.BelltowerSongs;

/**
 * Scene which controls and obtains status of the belltower.
 */
@Singleton
public class BelltowerScene implements Initializable {


    public Button btnRing;

    public Button btnStatus;

    public Text txtState;

    public ListView<String> listView;

    public Button btnBack;

    @Inject
    BelltowerSceneModel belltowerSceneModel;

    @Inject
    BelltowerSongs songs;

    @Inject
    Scenes scenes;

    @FXML
    public void ringBell(@SuppressWarnings("unused") ActionEvent actionEvent) {
        if(listView.getSelectionModel().getSelectedIndices().size() > 0) {
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            Integer i = listView.getSelectionModel().getSelectedIndices().stream().findFirst().get();
            String song = songs.getSongs().get(i);
            belltowerSceneModel.requestRing(song);
        }
    }



    @FXML
    public void getStatus(@SuppressWarnings("unused") ActionEvent actionEvent) {
        belltowerSceneModel.requestStatus();
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songs.getSongs().forEach(s -> listView.getItems().add(s));
        txtState.textProperty().bind(belltowerSceneModel.getStatusProperty());
    }

    public void switchStatusScene(ActionEvent event) {
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scenes.getStatus());
    }
}
