package org.flyboy.bells.belfry;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author John J. Franey
 */
public class RepeatTimerTest {

    private RepeatTimer repeatTimer;

    @BeforeEach
    public void init() {
        repeatTimer = new RepeatTimer();

        repeatTimer.vertx = Mockito.mock(Vertx.class);

        repeatTimer.mpd = Mockito.mock(Mpd.class);
    }

    final List<MpdMetadata.Song> songs = List.of(
            new MpdMetadata.Song("peal-beginning.ogg", 10000L),
            new MpdMetadata.Song("peal-middle.ogg", 10000L),
            new MpdMetadata.Song("peal-end.ogg", 10000L)
    );

    @Test
    public void testStopTimerWhenNoTimerSet() {

        repeatTimer.stop();

        Mockito.verify(repeatTimer.vertx, Mockito.times(0)).cancelTimer(any(Long.class));
        Mockito.verify(repeatTimer.mpd, Mockito.times(1)).send(RepeatTimer.MPD_DEACTIVATE_REPEAT_MODE);
    }

    @Test
    public void testStopWhenNotRepeating() {
        repeatTimer.activateRepeatTimerId = 43L;

        RepeatTimer spy = Mockito.spy(repeatTimer);
        spy.stop();

        // cancel only the start timer
        Mockito.verify(repeatTimer.vertx, Mockito.times(1)).cancelTimer(repeatTimer.activateRepeatTimerId);
        Mockito.verify(repeatTimer.vertx, Mockito.times(1)).cancelTimer(any(Long.class));

        Mockito.verify(repeatTimer.mpd, Mockito.times(1)).send(RepeatTimer.MPD_DEACTIVATE_REPEAT_MODE);
    }

    @Test
    public void testStopWhenRepeating() {
        repeatTimer.deactivateRepeatTimerId = 19L;

        RepeatTimer spy = Mockito.spy(repeatTimer);
        spy.stop();

        // cancel only the stop timer
        Mockito.verify(repeatTimer.vertx, Mockito.times(1)).cancelTimer(repeatTimer.deactivateRepeatTimerId);
        Mockito.verify(repeatTimer.vertx, Mockito.times(1)).cancelTimer(any(Long.class));

        // send repeat off command
        Mockito.verify(repeatTimer.mpd, Mockito.times(1)).send(RepeatTimer.MPD_DEACTIVATE_REPEAT_MODE);
    }

    @Test
    public void testInvalidState() {
        repeatTimer.deactivateRepeatTimerId = 12L;

        Assertions.assertThrows(IllegalStateException.class, () -> repeatTimer.start(songs, 90L));
    }

    @Test
    public void testStartTimers() {

        repeatTimer.start(songs, 100000L);
        Mockito.verify(repeatTimer.vertx, Mockito.times(1))
                .setTimer(11000, repeatTimer.activateRepeatTimerHandler);
        Mockito.verify(repeatTimer.vertx, Mockito.times(1))
                .setTimer(89000, repeatTimer.deactivateRepeatTimerHandler);
    }

    @Test
    public void testActivateRepeatSuccess() {
        List<String> response = List.of("OK MPD 0.23.5", "OK");
        Mockito.when(repeatTimer.mpd.send(RepeatTimer.MPD_ACTIVATE_REPEAT_MODE)).thenReturn(Uni.createFrom().item(response));

        repeatTimer.activateRepeatTimerId = 10L;
        repeatTimer.activateRepeatTimerHandler.accept(repeatTimer.activateRepeatTimerId);

        Assertions.assertNull(repeatTimer.activateRepeatTimerId);
    }

    @Test
    public void testActivateRepeatFail() {
        List<String> response = List.of("OK MPD 0.23.5", "OK");
        Mockito.when(repeatTimer.mpd.send(RepeatTimer.MPD_ACTIVATE_REPEAT_MODE))
                .thenReturn(Uni.createFrom().item(response).onItem().failWith(() -> new BelfryException("mock")));

        repeatTimer.activateRepeatTimerId = 10L;
        repeatTimer.activateRepeatTimerHandler.accept(repeatTimer.activateRepeatTimerId);

        Assertions.assertNull(repeatTimer.activateRepeatTimerId);
    }

    @Test
    public void testDeactivateRepeatSuccess() {
        List<String> response = List.of("OK MPD 0.23.5", "OK");
        Mockito.when(repeatTimer.mpd.send(RepeatTimer.MPD_DEACTIVATE_REPEAT_MODE)).thenReturn(Uni.createFrom().item(response));

        repeatTimer.deactivateRepeatTimerId = 10L;
        repeatTimer.deactivateRepeatTimerHandler.accept(repeatTimer.deactivateRepeatTimerId);

        Assertions.assertNull(repeatTimer.deactivateRepeatTimerId);
    }

    @Test
    public void testDeactivateRepeatFail() {
        List<String> response = List.of("OK MPD 0.23.5", "OK");
        Mockito.when(repeatTimer.mpd.send(RepeatTimer.MPD_DEACTIVATE_REPEAT_MODE))
                .thenReturn(Uni.createFrom().item(response).onItem().failWith(() -> new BelfryException("mock")));

        repeatTimer.deactivateRepeatTimerId = 10L;
        repeatTimer.deactivateRepeatTimerHandler.accept(repeatTimer.activateRepeatTimerId);

        Assertions.assertNull(repeatTimer.deactivateRepeatTimerId);
    }
}
