package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * An example illustrating how worker verticles can be deployed and how to interact with them.
 * <p>
 * This example prints the name of the current thread at various locations to exhibit the event loop <-> worker
 * thread switches.
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        Json.mapper.findAndRegisterModules();
        Json.mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Runner.runExample(MainVerticle.class);
    }

    @Override
    public void start() throws Exception {
        DeploymentOptions asWorker = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle("org.jjfflyboy.bells.scheduler.core.GoogleCalendarVerticle", asWorker);
        vertx.deployVerticle("org.jjfflyboy.bells.scheduler.core.PlayVerticle", asWorker);

        vertx.deployVerticle("org.jjfflyboy.bells.scheduler.core.SchedulerVerticle");

    }
}
