package org.flyboy.belltower.panel.scene;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

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
    Objects.requireNonNull(fxml);

    Parent fxmlParent;
    try {
      fxmlParent = createLoader().load(fxml.openStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new Scene(fxmlParent, 400, 200);
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
