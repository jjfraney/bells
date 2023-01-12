package org.flyboy.bells.ui.javafx.tower;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scene model supports scenes with controls of the belltower.
 * It provides asynchronous calls to the backend belltower endpoint.
 * Asynchronous calls to avoid burdening the javafx application thread with IO delays.
 * It also provides observable properties related to the belltower state.
 * Scenes can bind to these observable properties to their controls.
 *
 * @author John J. Franey
 */
@ApplicationScoped
public class BelltowerSceneModel {

  private static Logger logger = LoggerFactory.getLogger(BelltowerSceneModel.class);

  /**
   * An Observable property to obtain updates to belltower status.
   */
  private StringProperty status = new SimpleStringProperty();
  public StringProperty getStatusProperty() {
    return status;
  }



  @Inject
  @RestClient
  Belltower belltower;


  /**
   * Asynchronously send request to belltower endpoint to get
   * status and propagate result through an Observable property.
   */
  public void requestStatus() {
    belltower.getStatus()
        .subscribe().with(
            status -> getStatusProperty().setValue((status).getStatus()),
            fail -> logger.error("failed to get status.")
        );

  }


  /**
   * Asynchronously send request to belltower endpoin to
   * ring a bell pattern and propagate status result through
   * an observable property.
   *
   * @param pattern name of the bell pattern to ring
   */
  public void requestRing(String pattern) {
    belltower.ring(pattern)
        .subscribe().with(
            status -> getStatusProperty().setValue((status).getStatus()),
            fail -> logger.error("failed request to ring song={}", pattern)
        );
  }
}
