package org.flyboy.belltower.belfry;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.net.ConnectException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author John J. Franey
 */
class BellTest {


    public static final List<String> PEAL_LSINFO_RESULT = List.of(
            "file: peal-beginning.ogg",
            "duration: 2.2",
            "file: peal-middle.ogg",
            "duration: 3.3",
            "file: peal-end.ogg",
            "duration: 4.4",
            "list_OK",
            "state: stop",
            "list_OK",
            "OK");

    public static final List<String> PEAL_COMMANDS = Bell.mpdRingSongCommands(List.of(
            new MpdMetadata.Song("peal-beginning.ogg", 2200L),
            new MpdMetadata.Song("peal-middle.ogg", 3300L),
            new MpdMetadata.Song("peal-end.ogg", 4400L))
    );

    Bell bell;

    @BeforeEach
    public void beforeEach() {
        bell = new Bell();
        bell.mpd = Mockito.mock(Mpd.class);
        bell.repeatTimer = Mockito.mock(RepeatTimer.class);
        bell.defaultPealDuration = 60;
    }

    @Test
    public void testNoConnection() {
        Mockito.when(bell.mpd.send(ArgumentMatchers.any(String.class)))
                .thenReturn(Uni.createFrom().failure(new ConnectException("Connection refused: localhost/127.0.0.1:6600")));

        bell.getStatus().subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(ConnectException.class);
    }

    @Test
    void testStatus() {

        Mockito.when(bell.mpd.send(anyString())).thenReturn(Uni.createFrom().item(List.of("state: stop")));

        BellStatus expected = new BellStatus(false, "stop");

        Uni<BellStatus> actual = bell.getStatus();

        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);
    }


    @Test
    void testLock() {

        Mockito.when(bell.mpd.send("status")).thenReturn(Uni.createFrom().item(List.of("state: stop")));

        BellStatus expected = new BellStatus(true, "stop");

        Uni<BellStatus> actual = bell.lock();

        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);
    }

    @Test
    void testUnlock() {
        Mockito.when(bell.mpd.send(anyString())).thenReturn(Uni.createFrom().item(List.of("state: stop", "OK")));

        BellStatus expected = new BellStatus(false, "stop");

        Uni<BellStatus> actual = bell.getStatus();

        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);
    }

    @Test
    public void testRingWhenLocked() {
        // when locked, resource returns http status code LOCKED
        bell.lock().subscribe().withSubscriber(UniAssertSubscriber.create()).assertCompleted();

        Uni<BellStatus> actual = bell.ring("call-to-mass");
        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed().assertFailedWith(BellsUnavailableException.class);

    }

    @Test
    public void testRingWhenBusy() {
        Mockito.when(bell.mpd.send(Bell.MPD_PRE_PLAY_STATUS)).thenReturn(Uni.createFrom().item(List.of("state: play", "OK")));

        Uni<BellStatus> actual = bell.ring("call-to-mass");
        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed().assertFailedWith(BellsUnavailableException.class);

    }

    @Test
    public void testRingSuccess() {
        String sampleName = "call-to-mass";

        Mockito.when(bell.mpd.send(Bell.MPD_PRE_PLAY_STATUS)).thenReturn(
                Uni.createFrom().item(List.of(
                        "file: call-to-mass.ogg",
                        "duration: 3.3",
                        "list_OK",
                        "state: stop",
                        "list_OK",
                        "OK")));
        List<String> play = List.of("clear", "add call-to-mass.ogg", "play", "status");
        Mockito.when(bell.mpd.send(play)).thenReturn(
                Uni.createFrom().item(List.of("state: play", "OK")));


        Uni<BellStatus> actual = bell.ring(sampleName);
        BellStatus expected = new BellStatus(false, "play");

        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);

    }


    @BeforeAll
    public static void checkPealCommands() {
        List<String> expected = List.of("clear",
                "add peal-beginning.ogg",
                "add peal-middle.ogg",
                "add peal-end.ogg",
                "play",
                "status");
        MpdMetadata.Song beg = new MpdMetadata.Song("peal-beginning.ogg", 2200L);
        MpdMetadata.Song mid = new MpdMetadata.Song("peal-middle.ogg", 3300L);
        MpdMetadata.Song end = new MpdMetadata.Song("peal-end.ogg", 4400L);
        List<String> actual = Bell.mpdRingSongCommands(List.of(beg, mid, end));

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testVariablePealSuccess() {
        String sampleName = "peal";

        Mockito.when(bell.mpd.send(Bell.MPD_PRE_PLAY_STATUS)).thenReturn(
                Uni.createFrom().item(PEAL_LSINFO_RESULT));
        Mockito.when(bell.mpd.send(PEAL_COMMANDS)).thenReturn(
                Uni.createFrom().item(List.of("state: play", "OK")));


        BellStatus expected = new BellStatus(false, "play");

        bell.ring(sampleName).subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);

        Mockito.verify(bell.repeatTimer, Mockito.times(1)).start(List.of(
                new MpdMetadata.Song("peal-beginning.ogg", 2200),
                new MpdMetadata.Song("peal-middle.ogg", 3300),
                new MpdMetadata.Song("peal-end.ogg", 4400)
        ), 60000);

        Mockito.verify(bell.repeatTimer, Mockito.times(0)).stop();
    }

    @Test
    public void testRingFail() {
        String sampleName = "call-to-mass";

        Mockito.when(bell.mpd.send(Bell.MPD_PRE_PLAY_STATUS)).thenReturn(
                Uni.createFrom().item(List.of(
                        "file: call-to-mass.ogg",
                        "duration: 3.3",
                        "list_OK",
                        "state: stop",
                        "list_OK",
                        "OK")));
        List<String> play = List.of("clear", "add call-to-mass.ogg", "play", "status");
        MpdResponse.Ack ack = MpdResponse.getAck(List.of("ACK [50@0] {add} directory not found"));
        MpdCommandException mpdCommandException = new MpdCommandException(ack);
        Mockito.when(bell.mpd.send(play)).thenReturn(Uni.createFrom().failure(mpdCommandException));

        Uni<BellStatus> actual = bell.ring(sampleName);
        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed().assertFailedWith(BellPatternNotFoundException.class);


    }

    @Test
    public void testVariablePealFail() {
        String sampleName = "peal";

        Mockito.when(bell.mpd.send(Bell.MPD_PRE_PLAY_STATUS)).thenReturn(
                Uni.createFrom().item(PEAL_LSINFO_RESULT));

        MpdResponse.Ack ack = MpdResponse.getAck(List.of("ACK [50@0] {add} directory not found"));
        MpdCommandException mpdCommandException = new MpdCommandException(ack);
        Mockito.when(bell.mpd.send(PEAL_COMMANDS)).thenReturn(Uni.createFrom().failure(mpdCommandException));

        bell.ring(sampleName).subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailedWith(BelfryException.class);

        Mockito.verify(bell.repeatTimer, Mockito.times(0)).start(List.of(
                new MpdMetadata.Song("peal-beginning.ogg", 2200),
                new MpdMetadata.Song("peal-middle.ogg", 3300),
                new MpdMetadata.Song("peal-end.ogg", 4400)
        ), 60000);

        Mockito.verify(bell.repeatTimer, Mockito.times(0)).stop();
    }

    @Test
    public void testRingSampleNotFound() {
        String sampleName = "call-to-mass";

        Mockito.when(bell.mpd.send(Bell.MPD_PRE_PLAY_STATUS)).thenReturn(
                Uni.createFrom().item(List.of("state: stop", "OK")));

        Uni<BellStatus> actual = bell.ring(sampleName);
        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed().assertFailedWith(BellPatternNotFoundException.class);

    }

}
