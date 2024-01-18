package org.flyboy.belltower.panel.scene.belfry;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author John J. Franey
 */
@Singleton
public class DataGenerator {

  @Inject
  ViewModel viewModel;

  /**
   * a test data supplier.
   */
  @Scheduled(every = "5s")
  public void updateStatus() {

    String status = viewModel.getBelfryStatus().get();

    if (status.equals("IDLE")) {
      viewModel.getBelfryStatus().set("RINGING");
      viewModel.getBelfryPattern().set("call-to-mass.ogg");
    } else {
      viewModel.getBelfryStatus().set("IDLE");
      viewModel.getBelfryPattern().set("");
    }
  }
}
