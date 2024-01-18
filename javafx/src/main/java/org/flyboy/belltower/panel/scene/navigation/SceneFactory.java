package org.flyboy.belltower.panel.scene.navigation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * @author John J. Franey
 */

@ApplicationScoped
public class SceneFactory {

  @Belltower
  @ApplicationScoped
  public Scene getBelltower() {
      return loadScene("/belltower.fxml");
  }

  @Belfry
  @ApplicationScoped
  public Scene getBelfry() {
    return loadScene("/belfry.fxml");
  }

  @Timetable
  @ApplicationScoped
  public Scene getTimetable() {
    return loadScene("/timetable.fxml");
  }

  private Scene loadScene(String path) {
    URL fxml = getClass().getResource(path);
    Objects.requireNonNull(fxml);

    Parent fxmlParent;
    try {
      var loader = createLoader(fxml);
      fxmlParent = loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new Scene(fxmlParent, 800, 400);
  }

  @Inject
  Instance<Object> instance;

  public FXMLLoader createLoader(URL url) {
    FXMLLoader loader = new FXMLLoader(url);

    // note:
    //   View beans must be @Singleton and their
    //      data members UI controls have public accessor.
    loader.setControllerFactory(param -> instance.select(param).get());
    loader.setClassLoader(Thread.currentThread().getContextClassLoader());
    return loader;
  }
}
