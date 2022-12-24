package org.flyboy.bells.tower;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Assertions;
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
class BelltowerTest {


    Belltower belltower;

    @BeforeEach
    public void beforeEach() {
        belltower = new Belltower();
        belltower.linuxMPC = Mockito.mock(LinuxMPC.class);
    }

    @Test
    public void testNoConnection() {
        Mockito.when(belltower.linuxMPC.mpc(ArgumentMatchers.any(String.class)))
                .thenAnswer(invocation -> { throw new ConnectException("Connection refused: localhost/127.0.0.1:6600"); });
        //noinspection ReactiveStreamsUnusedPublisher
        Assertions.assertThrows(ConnectException.class, () -> belltower.getStatus());
    }

    @Test
    void testStatus() {

        Mockito.when(belltower.linuxMPC.mpc(anyString())).thenReturn(Uni.createFrom().item(List.of("state: stop")));

        BelltowerStatus expected = new BelltowerStatus(false, "stop");

        Uni<BelltowerStatus> actual = belltower.getStatus();

        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);
    }


    @Test
    void testLock() {

        Mockito.when(belltower.linuxMPC.mpc(anyString())).thenReturn(Uni.createFrom().item(List.of("state: stop")));

        BelltowerStatus expected = new BelltowerStatus(true, "stop");

        belltower.lock();
        Uni<BelltowerStatus> actual = belltower.getStatus();

        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);
    }

    @Test
    void testUnlock() {
        Mockito.when(belltower.linuxMPC.mpc(anyString())).thenReturn(Uni.createFrom().item(List.of("state: stop", "OK")));

        BelltowerStatus expected = new BelltowerStatus(false, "stop");

        Uni<BelltowerStatus> actual = belltower.getStatus();

        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);
    }

    @Test
    public void testRingWhenLocked() {
        // when locked, resource returns http status code LOCKED
        belltower.lock();

        Uni<BelltowerStatus> actual = belltower.ring("call-to-mass");
        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed().assertFailedWith(BelltowerUnavailableException.class);

    }

    @Test
    public void testRingWhenBusy() {
        Mockito.when(belltower.linuxMPC.mpc(anyString())).thenReturn(Uni.createFrom().item(List.of("state: play", "OK")));

        Uni<BelltowerStatus> actual = belltower.ring("call-to-mass");
        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed().assertFailedWith(BelltowerUnavailableException.class);

    }

    @Test
    public void testRingSuccess() {
        String sampleName = "call-to-mass";

        //noinspection unchecked
        Mockito.when(belltower.linuxMPC.mpc(anyString())).thenReturn(
                Uni.createFrom().item(List.of("state: stop", "OK")),
                Uni.createFrom().item(List.of("state: play", "OK"))
        );

        Uni<BelltowerStatus> actual = belltower.ring(sampleName);
        BelltowerStatus expected = new BelltowerStatus(false, "play");

        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted().assertItem(expected);

    }

    @Test
    public void testRingFail() {
        String sampleName = "call-to-mass";

        //noinspection unchecked
        Mockito.when(belltower.linuxMPC.mpc(anyString())).thenReturn(
                Uni.createFrom().item(List.of("state: stop", "OK")),
                Uni.createFrom().item(List.of("ACK [33@0] {add} some mock error"))
        );

        Uni<BelltowerStatus> actual = belltower.ring(sampleName);
        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed().assertFailedWith(BelltowerException.class);
    }


    @Test
    public void testRingSampleNotFound() {
        String sampleName = "call-to-mass";

        //noinspection unchecked
        Mockito.when(belltower.linuxMPC.mpc(anyString())).thenReturn(
                Uni.createFrom().item(List.of("state: stop", "OK")),
                Uni.createFrom().item(List.of("ACK [50@0] {add} No such directory"))
        );
        Uni<BelltowerStatus> actual = belltower.ring(sampleName);
        actual.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertFailed().assertFailedWith(BelltowerSampleNotFoundException.class);
    }

}
