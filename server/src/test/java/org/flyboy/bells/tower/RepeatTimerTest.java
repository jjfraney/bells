package org.flyboy.bells.tower;

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

        repeatTimer.linuxMPC = Mockito.mock(LinuxMPC.class);
    }

    final List<MpdMetadata.Song> songs = List.of(
            new MpdMetadata.Song("peal-beginning.ogg", 10000L),
            new MpdMetadata.Song("peal-middle.ogg", 10000L),
            new MpdMetadata.Song("peal-end.ogg", 10000L)
    );

    @Test
    public void testStopTimerWhenNoTimerSet() {
        repeatTimer.repeatMode = RepeatTimer.RepeatMode.INACTIVE;

        repeatTimer.stop();

        Mockito.verify(repeatTimer.vertx, Mockito.times(0)).cancelTimer(any(Long.class));
        Mockito.verify(repeatTimer.linuxMPC, Mockito.times(0)).mpc("repeat off");
    }

    @Test
    public void testStopWhenNotRepeating() {
        repeatTimer.onRepeatTimerId = 43L;
        repeatTimer.repeatMode = RepeatTimer.RepeatMode.INACTIVE;

        RepeatTimer spy = Mockito.spy(repeatTimer);
        spy.stop();

        // cancel only the start timer
        Mockito.verify(repeatTimer.vertx, Mockito.times(1)).cancelTimer(repeatTimer.onRepeatTimerId);
        Mockito.verify(repeatTimer.vertx, Mockito.times(1)).cancelTimer(any(Long.class));

        // do not send 'repeat off' command
        Mockito.verify(repeatTimer.linuxMPC, Mockito.times(0)).mpc("repeat off");
    }

    @Test
    public void testStopWhenRepeating() {
        repeatTimer.offRepeatTimerId = 19L;
        repeatTimer.repeatMode = RepeatTimer.RepeatMode.ACTIVE;

        RepeatTimer spy = Mockito.spy(repeatTimer);
        spy.stop();

        // cancel only the stop timer
        Mockito.verify(repeatTimer.vertx, Mockito.times(1)).cancelTimer(repeatTimer.offRepeatTimerId);
        Mockito.verify(repeatTimer.vertx, Mockito.times(1)).cancelTimer(any(Long.class));

        // send repeat off command
        Mockito.verify(repeatTimer.linuxMPC, Mockito.times(1)).mpc("repeat 0");
    }

    @Test
    public void testInvalidState() {
        repeatTimer.offRepeatTimerId = 12L;
        repeatTimer.repeatMode = RepeatTimer.RepeatMode.INACTIVE;

        Assertions.assertThrows(IllegalStateException.class, () -> repeatTimer.start(songs, 90L));
    }

    @Test
    public void testStartTimers() {

        repeatTimer.start(songs, 100000L);
        Mockito.verify(repeatTimer.vertx, Mockito.times(1))
                .setTimer(11000, repeatTimer.activateRepeat);
        Mockito.verify(repeatTimer.vertx, Mockito.times(1))
                .setTimer(89000, repeatTimer.deactivateRepeat);
    }

    @Test
    public void testActivateRepeatSuccess() {
        List<String> response = List.of("OK MPD 0.23.5", "OK");
        Mockito.when(repeatTimer.linuxMPC.mpc("repeat 1", "single 1")).thenReturn(Uni.createFrom().item(response));

        repeatTimer.onRepeatTimerId = 10L;
        repeatTimer.repeatMode = RepeatTimer.RepeatMode.INACTIVE;
        repeatTimer.activateRepeat.accept(repeatTimer.onRepeatTimerId);

        Assertions.assertEquals(RepeatTimer.RepeatMode.ACTIVE, repeatTimer.repeatMode);
        Assertions.assertNull(repeatTimer.onRepeatTimerId);
    }

    @Test
    public void testActivateRepeatFail() {
        List<String> response = List.of("OK MPD 0.23.5", "OK");
        Mockito.when(repeatTimer.linuxMPC.mpc("repeat 1", "single 1"))
                .thenReturn(Uni.createFrom().item(response).onItem().failWith(() -> new BelltowerException("mock")));

        repeatTimer.onRepeatTimerId = 10L;
        repeatTimer.repeatMode = RepeatTimer.RepeatMode.INACTIVE;
        repeatTimer.activateRepeat.accept(repeatTimer.onRepeatTimerId);

        Assertions.assertEquals(RepeatTimer.RepeatMode.UNKNOWN, repeatTimer.repeatMode);
        Assertions.assertNull(repeatTimer.onRepeatTimerId);
    }

    @Test
    public void testDeactivateRepeatSuccess() {
        List<String> response = List.of("OK MPD 0.23.5", "OK");
        Mockito.when(repeatTimer.linuxMPC.mpc("repeat 0", "single 0")).thenReturn(Uni.createFrom().item(response));

        repeatTimer.offRepeatTimerId = 10L;
        repeatTimer.repeatMode = RepeatTimer.RepeatMode.ACTIVE;
        repeatTimer.deactivateRepeat.accept(repeatTimer.offRepeatTimerId);

        Assertions.assertEquals(RepeatTimer.RepeatMode.INACTIVE, repeatTimer.repeatMode);
        Assertions.assertNull(repeatTimer.offRepeatTimerId);
    }

    @Test
    public void testDeactivateRepeatFail() {
        List<String> response = List.of("OK MPD 0.23.5", "OK");
        Mockito.when(repeatTimer.linuxMPC.mpc("repeat 0", "single 0"))
                .thenReturn(Uni.createFrom().item(response).onItem().failWith(() -> new BelltowerException("mock")));

        repeatTimer.onRepeatTimerId = 10L;
        repeatTimer.repeatMode = RepeatTimer.RepeatMode.ACTIVE;
        repeatTimer.deactivateRepeat.accept(repeatTimer.onRepeatTimerId);

        Assertions.assertEquals(RepeatTimer.RepeatMode.UNKNOWN, repeatTimer.repeatMode);
        Assertions.assertNull(repeatTimer.offRepeatTimerId);
    }
}
