package org.flyboy.belltower.belfry;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.core.net.NetSocket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author John J. Franey
 */
public class MpdTest {

    Mpd mpd;

    @BeforeEach
    public void beforeEach() {
        NetClient netClient = Mockito.mock(NetClient.class);
        NetSocket netSocket = Mockito.mock(NetSocket.class);

        mpd = new Mpd();
        mpd.netClient = netClient;
        mpd.mpdHost = "localhost";
        mpd.mpdPort = 6600;

        Mockito
                .when(netClient.connect(anyInt(), anyString()))
                .thenReturn(Uni.createFrom().item(netSocket));

        Mockito.doNothing().when(netSocket).writeAndForget(anyString());

        Buffer buffers = Buffer.buffer("OK MPD 0.23.5\nrepeatMode: stop\nvolume: 10\nOK\n");
        Mockito
                .when(netSocket.toMulti())
                .thenReturn(Multi.createFrom().item(buffers));
    }

    @Test
    public void testMpcCmdString() {
        mpd.send("status").subscribe().with(l -> {
            Assertions.assertEquals(4, l.size());
            Assertions.assertEquals("repeatMode: stop", l.get(1));
            Assertions.assertEquals("volume: 10", l.get(2));
        });
    }

    @Test
    public void testMpcCommand() {
        mpd.send("status").subscribe().with(r -> {
            Assertions.assertEquals("stop", MpdResponse.getField(r, "repeatMode").orElse("absent"));
            Assertions.assertEquals("10", MpdResponse.getField(r, "volume").orElse("-1000"));

        });
    }
}
