package org.flyboy.bells.tower;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.core.net.NetSocket;
import musicpd.protocol.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author John J. Franey
 */
public class LinuxMPCTest {

    LinuxMPC linuxMPC;

    @BeforeEach
    public void beforeEach() {
        NetClient netClient = Mockito.mock(NetClient.class);
        NetSocket netSocket = Mockito.mock(NetSocket.class);

        linuxMPC = new LinuxMPC();
        linuxMPC.netClient = netClient;
        linuxMPC.mpdHost = "localhost";
        linuxMPC.mpdPort = 6600;

        Mockito
                .when(netClient.connect(anyInt(), anyString()))
                .thenReturn(Uni.createFrom().item(netSocket));

        Mockito.doNothing().when(netSocket).writeAndForget(anyString());

        Buffer buffers = Buffer.buffer("OK MPD 0.23.5\nstate: stop\nvolume: 10\nOK\n");
        Mockito
                .when(netSocket.toMulti())
                .thenReturn(Multi.createFrom().item(buffers));
    }

    @Test
    public void testMpcCmdString() {
        linuxMPC.mpc("status").subscribe().with(l -> {
            Assertions.assertEquals(4, l.size());
            Assertions.assertEquals("state: stop", l.get(1));
            Assertions.assertEquals("volume: 10", l.get(2));
        });
    }

    @Test
    public void testMpcCommand() {
        musicpd.protocol.Status status = new Status();
        linuxMPC.mpc(status).subscribe().with(r -> {
            Assertions.assertEquals("stop", r.getState().orElse("absent"));
            Assertions.assertEquals(10, r.getVolume().orElse(-1000));

        });
    }
}
