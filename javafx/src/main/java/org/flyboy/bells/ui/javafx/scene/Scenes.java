package org.flyboy.bells.ui.javafx.scene;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * @author John J. Franey
 */

@ApplicationScoped
public class Scenes {

  @Inject
  FXMLLoader fxmlLoader;

  private Scene belltower;
  public Scene getBelltower() {
    if(belltower == null) {
      belltower = loadScene("/belltower.fxml");
    }
    return belltower;
  }

  private Scene calendar;
  public Scene getCalendar() {
    if(calendar == null) {
      calendar = loadScene("/calendar.fxml");
    }
    return calendar;
  }

  private Scene status;
  public Scene getStatus() {
    if(status == null) {
      status = loadScene("/status.fxml");
    }
    return status;
  }

  private Scene loadScene(String path) {
    URL fxml = getClass().getResource(path);
    Parent fxmlParent = null;
    try {
      fxmlParent = createLoader().load(fxml.openStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Scene scene = new Scene(fxmlParent, 400, 200);
    return scene;
  }

  @Inject
  Instance<Object> instance;

  public FXMLLoader createLoader() {
    FXMLLoader loader = new FXMLLoader();
    loader.setControllerFactory(param -> instance.select(param).get());
    loader.setClassLoader(Thread.currentThread().getContextClassLoader());
    return loader;
  }
}
