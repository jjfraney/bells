package org.flyboy.belltower.panel.scene;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.flyboy.belltower.panel.conf.StartupScene;
import org.flyboy.belltower.panel.scene.navigation.Belltower;

public class App {

	@Inject
	@Belltower
	Scene belltower;

	public void start(@Observes @StartupScene Stage stage) {

		stage.setScene(belltower);
		stage.setTitle("Belltower Control Panel");
		stage.show();
	}

}
