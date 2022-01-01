package org.flyboy.bells.player;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.core.net.NetSocket;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * @author John J. Franey
 */
@RequestScoped
public class PlayerService {

    @Inject
    Vertx vertx;

    private static final int PORT = 6600;

    /**
     * sends the mpd command to the player and returns the response as a single string.
     * @param cmd
     * @return
     */
    public Uni<String> mpd(String cmd) {
        NetClient netClient = vertx.createNetClient();
        return netClient.connect(PORT, "localhost")
                .onItem().invoke(netSocket -> {
                    netSocket.writeAndForget(cmd + "\n");
                    netSocket.writeAndForget("close\n");
                })
                .onItem().transformToMulti(NetSocket::toMulti)
                .onItem().transform(buffer -> buffer.toString())
                .flatMap(s -> Multi.createFrom().items(s.split("\n")))
                .skip().first()
                .onItem().transform(s -> s + "\n")
                .collect().with(Collectors.joining())
                ;

    }
}
