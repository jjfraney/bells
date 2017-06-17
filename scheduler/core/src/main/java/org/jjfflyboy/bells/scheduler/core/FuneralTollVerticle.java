package org.jjfflyboy.bells.scheduler.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfraney
 */
public class FuneralTollVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuneralTollVerticle.class);
    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        if(args.length == 0) {
            args = new String[]{"run", FuneralTollVerticle.class.getName()};
        }
        new Launcher().dispatch(args);
    }

    @Override
    public void start() throws Exception {
        JsonObject mpcOptions = new JsonObject()
                .put("mpdHost", "localhost")
                .put("mpdPort", 6600);
        DeploymentOptions options = new DeploymentOptions().setConfig(mpcOptions);
        vertx.deployVerticle(PlayerVerticle.class.getName(), options);

        vertx.setTimer(1000, h -> {
            LOGGER.debug("sending message");
            JsonObject msg = new JsonObject()
                    .put("command", "play")
                    .put("song", "funeral-toll")
                    .put("playTime", 120);
            vertx.eventBus().publish("bell-tower.segmented", msg);
        });

        vertx.setTimer(150000, h -> {
            LOGGER.debug("sending stop");
            JsonObject msg = new JsonObject().put("command", "stop");
            vertx.eventBus().publish("bell-tower.segmented", msg);
        });
    }
}
