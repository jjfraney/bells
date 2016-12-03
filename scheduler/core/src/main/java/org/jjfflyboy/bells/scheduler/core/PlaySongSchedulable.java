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
            Player.play(song);
            LOGGER.info("playing, song={}, now={}",
                    song, LocalDateTime.now());
            return null;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlaySongSchedulable that = (PlaySongSchedulable) o;

        return song.equals(that.song);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + song.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + getFireTime().toString() + " " + song;
    }

}
