package org.flyboy.bells.tower;

import io.vertx.mutiny.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * To implement a variable length bell pattern, use repeat feature of media player.
 * Turn on repeats for the middle segment to control the length of the bell pattern.
 * Turn off repeats to end the bell pattern within the requested duration.
 * Another design/implementation is required by a media player without a similar repeat feature to MPD:
 * individual songs of a playlist can be singularly repeated and repeats can be controlled during playback.
 * In MPD, 'repeat' and 'single' commands are used to implement this feature.
 * <p>
 * Timers are required to implement the behavior.
 * A timer is fired at the right time to activate repeat of the middle sample.
 * The right time to turn on repeats is in the midst of the middle sample of the full bell pattern.
 * Another timer is fired at the right time to deactivate repeat of the middle sample..
 * The right time to turn off repeats is after the full duration of the bell pattern is accomplished.
 * </p>
 *
 * @author John J. Franey
 */
@Dependent
public class RepeatTimer {
    private final Logger logger = LoggerFactory.getLogger(RepeatTimer.class);

    /**
     * MPD protocol messages to activate repeat mode.
     */
    static final List<String> MPD_ACTIVATE_REPEAT_MODE = List.of("repeat 1", "single 1");

    /**
     * MPD protocol messages to deactivate repeat mode.
     */
    static final List<String> MPD_DEACTIVATE_REPEAT_MODE = List.of("repeat 0", "single 0");

    @Inject
    Vertx vertx;
    @Inject
    Mpd mpd;
    /**
     * Timer controlling activation of repeat mode.
     */
    Long activateRepeatTimerId;
    /**
     * a timer handler that would activate repeat mode,
     */
    final Consumer<Long> activateRepeatTimerHandler = (l) -> {
        long timerId = activateRepeatTimerId;
        activateRepeatTimerId = null;
        mpd.send(MPD_ACTIVATE_REPEAT_MODE)
                .subscribe().with(
                        response -> logger.debug("Repeat activate succeeded, timer id={}", timerId),
                        fail -> logger.error("Repeat activate failed, timer id={}: {}", timerId, fail.getMessage())
                );
    };

    /**
     * Timer controlling deactivation of repeat mode.
     */
    Long deactivateRepeatTimerId;
    /**
     * A timer handler that would deactivate repeat mode.
     */
    final Consumer<Long> deactivateRepeatTimerHandler = (l) -> {
        long timerId = deactivateRepeatTimerId;
        deactivateRepeatTimerId = null;
        mpd.send(MPD_DEACTIVATE_REPEAT_MODE)
                .subscribe().with(
                        response -> logger.debug("Repeat deactivate succeeded, timer id={}", timerId),
                        fail -> logger.error("Repeat deactivate failed, timer id={}: {}", timerId, fail.getMessage())
                );
    };


    /**
     * Calculate when to activate repeat mode and for how long.
     * Create a timer to activate repeat and set a new timer to deactivate repeats.
     *
     * @param songs            from the {@link MpdMetadata}
     * @param playbackDuration to play the repeatable song in milliseconds
     */
    public void start(List<MpdMetadata.Song> songs, long playbackDuration) {
        if (activateRepeatTimerId != null || deactivateRepeatTimerId != null) {
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
        activateRepeatTimerId = vertx.setTimer(repeatModeActivateMark, activateRepeatTimerHandler);
        logger.debug("set 'activate repeat mode' timer, timer id={}, fires at {}",
                activateRepeatTimerId, LocalTime.now().plus(repeatModeActivateMark, ChronoUnit.MILLIS));

        // deactivate repeat mode after some duration of active repeat mode.
        // one second before the end of the last iteration of the middle segment
        long repeatModeDeactivateMark = beg.duration() + durationActiveRepeatMode - 1000;
        deactivateRepeatTimerId = vertx.setTimer(repeatModeDeactivateMark, deactivateRepeatTimerHandler);
        logger.debug("set 'deactivate repeat mode' timer, timer id={}, fires at {}",
                deactivateRepeatTimerId, LocalTime.now().plus(repeatModeDeactivateMark, ChronoUnit.MILLIS));
    }

    /**
     * cancel timers (if any) and send MPD commands to deactivate repeat mode.
     */
    public void stop() {
        Arrays.stream(new Long[]{activateRepeatTimerId, deactivateRepeatTimerId})
                .filter(Objects::nonNull)
                .forEach(id -> vertx.cancelTimer(id));
        activateRepeatTimerId = null;
        deactivateRepeatTimerId = null;

        mpd.send(MPD_DEACTIVATE_REPEAT_MODE).subscribe().with(
                response -> logger.info("MPD has successfully deactivated repeat mode."),
                fail -> logger.error("MPD error when deactivating repeat mode: {}", fail.getMessage())
        );
    }
}
