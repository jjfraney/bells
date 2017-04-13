package org.jjfflyboy.bells.scheduler.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfraney
 */
public class WeddingPealVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeddingPealVerticle.class);
    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        if(args.length == 0) {
            args = new String[]{"run", WeddingPealVerticle.class.getName()};
        }
        new Launcher().dispatch(args);
    }

    @Override
    public void start() throws Exception {
        vertx.deployVerticle(PlaySegmentedVerticle.class.getName());

        vertx.setTimer(1000, h -> {
            LOGGER.debug("sending message");
            JsonObject msg = new JsonObject()
                    .put("command", "play")
                    .put("song", "peal-single")
                    .put("playTime", 60);
            vertx.eventBus().publish("bell-tower.segmented", msg);
        });

        vertx.setTimer(10000, h -> {
            LOGGER.debug("sending stop");
            JsonObject msg = new JsonObject().put("command", "stop");
            vertx.eventBus().publish("bell-tower.segmented", msg);
        });
    }
}
