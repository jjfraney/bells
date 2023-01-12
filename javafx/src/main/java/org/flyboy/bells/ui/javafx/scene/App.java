package org.flyboy.bells.ui.javafx.scene;

import javafx.stage.Stage;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.flyboy.bells.ui.javafx.conf.StartupScene;

public class App {

	@Inject
	Scenes scenes;

	public void start(@Observes @StartupScene Stage stage) {

		stage.setScene(scenes.getStatus());
		stage.setTitle("Belltower Panel");
		stage.show();
	}

}
