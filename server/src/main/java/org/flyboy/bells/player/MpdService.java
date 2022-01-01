package org.flyboy.bells.player;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.core.net.NetSocket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to send mpd commands and receive mpd responses.
 * @author John J. Franey
 */
@ApplicationScoped
public class MpdService {
    @Inject
    Vertx vertx;

    NetClient netClient;

    NetClient getNetClient() {
        if(netClient == null) {
            netClient = vertx.createNetClient();
        }
        return netClient;
    }

    private static final int PORT = 6600;
    private static final String HOST = "localhost";

    /**
     * sends the mpd command to the player and returns the response as list of lines from MPD
     * @param cmd
     * @retur
     */
    public Uni<List<String>> mpd(String cmd) {
        Uni<List<String>> result = getNetClient().connect(PORT, "localhost").log()
                .onItem().invoke(netSocket -> {
                    netSocket.writeAndForget(cmd + "\n");

                    // Closes connection from MPD server side.
                    // Must close the socket for the stream to terminate
                    netSocket.writeAndForget("close\n");
                })
                .onItem().transformToMulti(NetSocket::toMulti)
                .onItem().transform(buffer -> buffer.toString())
                .flatMap(s -> Multi.createFrom().items(s.split("\n")))
                .collect().with(Collectors.toList())
                ;
        return result;
    }
}
