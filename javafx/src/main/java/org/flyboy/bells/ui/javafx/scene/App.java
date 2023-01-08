package org.flyboy.bells.ui.javafx.scene;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.flyboy.bells.ui.javafx.conf.StartupScene;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;

public class App {

	@Inject
	FXMLLoader fxmlLoader;

	public void start(@Observes @StartupScene Stage stage) {

		try {
			URL fxml = getClass().getResource("/belltower.fxml");
			Parent fxmlParent = fxmlLoader.load(fxml.openStream());
			stage.setScene(new Scene(fxmlParent, 400, 200));
			stage.setTitle("Belltower Panel");
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
