package org.jjfflyboy.bells.scheduler.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author jfraney
 */
public class PlayVerticle extends AbstractVerticle {
    private static Logger LOGGER = LoggerFactory.getLogger(PlayVerticle.class);
    public void start() {
        vertx.eventBus().consumer("bell-tower.player", message -> {
            String command = message.body().toString();
            if(command.startsWith("play")) {
                LOGGER.debug("received command: {}", command);
                String[] c = command.split(" ");
                if(c.length > 0) {
                    Player.play(c[1]);
                }
            }
        });
    }
}
