package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author jfraney
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        if(args.length == 0) {
            args = new String[]{"run", MainVerticle.class.getName()};
        }
        new Launcher().dispatch(args);
    }

    static {
        // finds extension for jdk8 dates, ...
        Json.mapper.findAndRegisterModules();

        // big decimal retains precision
        Json.mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

    @Override
    public void start() throws Exception {
        DeploymentOptions asWorker = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle(GoogleCalendarVerticle.class.getName(), asWorker);
        vertx.deployVerticle(PlayVerticle.class.getName(), asWorker);

        vertx.deployVerticle(SchedulerVerticle.class.getName());

    }
}
