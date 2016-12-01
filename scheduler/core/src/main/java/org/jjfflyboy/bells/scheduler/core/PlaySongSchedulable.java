package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * @author jfraney
 */
public class PlaySongSchedulable extends AbstractOneShotSchedulable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaySongSchedulable.class);
    private final String song;

    public PlaySongSchedulable(String song, LocalDateTime firetime) {
        super(firetime);
        this.song = song;
    }

    @Override
    public Callable<Void> getCallable() {
        return () -> {
            LOGGER.info("playing, song={}, now={}",
                    song, LocalDateTime.now());
            return null;
        };
    }

}
