package org.flyboy.bells.player;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.core.net.NetSocket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author John J. Franey
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MpdServiceTest {



    @Test
    public void testSunnyDay() {
        List<String> expected = List.of("OK MPD 1.2.3", "volume: 0", "OK");

        // partial mock of service, so we can mock result of getNetClient()
        MpdService mpdService = Mockito.spy(new MpdService());

        // data returned by the mocked net socket
        String mpdResponse = expected.stream().collect(Collectors.joining("\n", "", "\n"));
        Multi<Buffer> buffers = Multi.createFrom().items(Buffer.buffer(mpdResponse));
        NetSocket mockNetSocket = Mockito.mock(NetSocket.class);
        Mockito.when(mockNetSocket.toMulti()).thenReturn(buffers);

        // set up mock for mpdService->getNetClient()
        NetClient mockNetClient = Mockito.mock(NetClient.class);
        Mockito.doReturn(mockNetClient).when(mpdService).getNetClient();

        // setup mock for netClient->connect
        Uni<NetSocket> mockUniNetSocket = Uni.createFrom().item(mockNetSocket);
        Mockito.when(mockNetClient.connect(6600, "localhost")).thenReturn(mockUniNetSocket);

        // now, with mocks in place, get the read from mpdService.mpd
        List<String> actual = new ArrayList<>();
        mpdService.mpd("status")
                .subscribe()
                .with(actual::addAll);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testConnectFail() {
        List<String> expected = Collections.emptyList();

        // partial mock of service, so we can mock result of getNetClient()
        MpdService mpdService = Mockito.spy(new MpdService());

        // set up mock for mpdService->getNetClient()
        NetClient mockNetClient = Mockito.mock(NetClient.class);
        Mockito.doReturn(mockNetClient).when(mpdService).getNetClient();

        // setup mock for netClient->connect
        Uni<NetSocket> fail = Uni.createFrom().failure(new IOException("no connection"));
        Mockito.when(mockNetClient.connect(6600, "localhost")).thenReturn(fail);
        mpdService.mpd("status")
                .subscribe()
                .with(a -> Assertions.fail(), t -> Assertions.assertTrue(true));
    }
}
