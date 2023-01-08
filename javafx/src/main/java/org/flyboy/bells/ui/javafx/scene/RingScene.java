package org.flyboy.bells.ui.javafx.scene;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.flyboy.bells.ui.javafx.tower.BelltowerSongs;

/**
 * Scene which controls and obtains status of the belltower.
 */
@Singleton
public class RingScene implements Initializable {

    @Inject
    BelltowerSongs songs;

    @FXML
    public Button btnRing;

    @FXML
    public Button btnStatus;

    @FXML
    public Text txtState;

    @FXML
    public ListView<String> listView;

    @Inject
    RingSceneModel ringSceneModel;

    @FXML
    public void ringBell(ActionEvent actionEvent) {
        if(listView.getSelectionModel().getSelectedIndices().size() > 0) {
            Integer i = (Integer) listView.getSelectionModel().getSelectedIndices().stream().findFirst().get();
            String song = songs.getSongs().get(i);
            ringSceneModel.requestRing(song);
        }
    }



    @FXML
    public void getStatus(ActionEvent actionEvent) {
        ringSceneModel.requestStatus();
    }




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songs.getSongs().forEach(s -> listView.getItems().add(s));
        txtState.textProperty().bind(ringSceneModel.getStatusProperty());
    }
};
