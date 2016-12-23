package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * This is a periodic schedulable to play a song for debugging.  No external
 * dependency on google's calendar.
 * @author jfraney
 */
public class DebugPlaySongSchedulable extends AbstractPeriodicSchedulable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugPlaySongSchedulable.class);

    private final Duration period;

    public DebugPlaySongSchedulable() {
        Settings settings = new PropertySettings();
        period = settings.getDebugPlayPeriod();

        LOGGER.info("Debug play interval={}", period);
    }

    @Override
    public Duration getPeriod() {
        return period;
    }

    @Override
    public Callable<Void> getCallable() {
        return () -> {
            String song = "call-to-mass.ogg";
            LOGGER.info("playing, song={}, now={}", song, LocalDateTime.now());
            Player.play(song);
            return null;
        };
    }

}
