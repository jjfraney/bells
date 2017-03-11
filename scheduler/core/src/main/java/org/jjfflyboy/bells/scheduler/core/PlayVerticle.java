package org.jjfflyboy.bells.scheduler.core;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
