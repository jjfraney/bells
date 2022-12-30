package org.flyboy.bells.tower;

import io.vertx.mutiny.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * To implement a variable length bell pattern, use MPD's repeat feature.
 * Turn on repeats for the middle segment to control the length of the bell pattern.
 * Turn off repeats to end the bell pattern within the requested duration.
 * <p>
 * Timers are required to implement the behavior.
 * A timer is fired at the right time to send the 'repeat on' command.
 * The right time to turn on repeats is in the midst of the middle sample of the full bell pattern.
 * Another timer is fired at the right time to send the 'repeat off' command.
 * The right time to turn off repeats is after the full duration of the bell pattern can be assured.
 * </p>
 *
 * @author John J. Franey
 */
@Dependent
public class RepeatTimer {
    private final Logger logger = LoggerFactory.getLogger(RepeatTimer.class);

    /**
     * Timer controlling activation of repeat mode.
     */
    Long onRepeatTimerId;

    /**
     * Timer controlling deactivation of repeat mode.
     */
    Long offRepeatTimerId;
    RepeatMode repeatMode = RepeatMode.INACTIVE;
    @Inject
    Vertx vertx;
    @Inject
    LinuxMPC linuxMPC;
    /**
     * a timer handler that would activate repeat mode,
     */
    final Consumer<Long> activateRepeat = (l) -> linuxMPC.mpc("repeat 1", "single 1")
            .subscribe().with(
                    response -> {
                        repeatMode = RepeatMode.ACTIVE;
                        logger.debug("repeat activate succeeded, timer id={}", onRepeatTimerId);
                        onRepeatTimerId = null;
                    },
                    fail -> {
                        onRepeatTimerId = null;
                        repeatMode = RepeatMode.UNKNOWN;
                        logger.error("repeat activate failed: {}", fail.getMessage());
                    });
    /**
     * A timer handler that would deactivate repeat mode.
     */
    final Consumer<Long> deactivateRepeat = (l) ->
            linuxMPC.mpc("repeat 0", "single 0")
                    .subscribe().with(
                            response -> {
                                repeatMode = RepeatMode.INACTIVE;
                                logger.debug("repeat deactivate succeeded, timer id={}", offRepeatTimerId);
                                offRepeatTimerId = null;
                            },
                            fail -> {
                                offRepeatTimerId = null;
                                repeatMode = RepeatMode.UNKNOWN;
                                logger.error("repeat deactivate failed: {}", fail.getMessage());
                            }
                    );

    /**
     * Calculate when to activate repeat mode and for how long.
     * Create a timer to activate repeat and set a new timer to deactivate repeats.
     *
     * @param songs            from the {@link MpdMetadata}
     * @param playbackDuration to play the repeatable song in milliseconds
     */
    public void start(List<MpdMetadata.Song> songs, long playbackDuration) {
        if (onRepeatTimerId != null || offRepeatTimerId != null) {
            throw new IllegalStateException("Timer is already active.");
        }

        // MpdMetadata ensures that the songs are ordered
        MpdMetadata.Song beg = songs.get(0);
        MpdMetadata.Song mid = songs.get(1);
        MpdMetadata.Song end = songs.get(2);

        // at start of bell pattern, repeat mode is inactive for these milliseconds
        // one second into the first iteration of the middle segment
        long durationInactiveRepeatMode = beg.duration();

        // calculate number of times to repeat the middle segment.
        // total playbackDuration is sum of times of the beginning and the end, and of some number of repeats of the middle.
        // total playbackDuration = beg + end + mid + mid * repeats
        // total playbackDuration - beg - end - mid = mid * repeats
        // (total playbackDuration - beg - end - mid) / mid = repeats
        int repeats = (int) ((playbackDuration - beg.duration() - end.duration()) / mid.duration());

        // Then, repeat mode would be active for these number of milliseconds.
        long durationActiveRepeatMode = mid.duration() * repeats;

        logger.debug("requiredTime={}, beg={}, mid={}, end={}, repeats={}, durationInactiveRepeatMode={}, durationActiveRepeatMode={}",
                playbackDuration, beg.duration(), mid.duration(), end.duration(), repeats, durationInactiveRepeatMode, durationActiveRepeatMode);

        if (repeats == 0) {
            // no repeats
            return;
        }

        // activate repeat mode after some duration of inactive repeat mode
        long repeatModeActivateMark = durationInactiveRepeatMode + 1000;
        onRepeatTimerId = vertx.setTimer(repeatModeActivateMark, activateRepeat);
        logger.debug("set 'activate repeat mode' timer, timer id={}, fires at {}",
                onRepeatTimerId, LocalTime.now().plus(repeatModeActivateMark, ChronoUnit.MILLIS));

        // deactivate repeat mode after some duration of active repeat mode.
        // one second before the end of the last iteration of the middle segment
        long repeatModeDeactivateMark = beg.duration() + durationActiveRepeatMode - 1000;
        offRepeatTimerId = vertx.setTimer(repeatModeDeactivateMark, deactivateRepeat);
        logger.debug("set 'deactivate repeat mode' timer, timer id={}, fires at {}",
                offRepeatTimerId, LocalTime.now().plus(repeatModeDeactivateMark, ChronoUnit.MILLIS));
    }

    public void stop() {
        Arrays.stream(new Long[]{onRepeatTimerId, offRepeatTimerId})
                .filter(Objects::nonNull)
                .forEach(id -> vertx.cancelTimer(id));
        onRepeatTimerId = null;
        offRepeatTimerId = null;

        if (repeatMode != RepeatMode.INACTIVE) {
            linuxMPC.mpc("repeat 0").subscribe().with(
                    result -> repeatMode = RepeatMode.INACTIVE,
                    fail -> {
                        repeatMode = RepeatMode.UNKNOWN;
                        logger.error("Unable to deactivate repeat mode.", fail);
                    }
            );
        }
    }


    enum RepeatMode {UNKNOWN, ACTIVE, INACTIVE}


}
