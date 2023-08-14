package org.flyboy.belltower.panel.scene;

import javafx.stage.Stage;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.flyboy.belltower.panel.conf.StartupScene;

public class App {

	@Inject
	Scenes scenes;

	public void start(@Observes @StartupScene Stage stage) {

		stage.setScene(scenes.getBelltower());
		stage.setTitle("BelltowerClient Panel");
		stage.show();
	}

}
