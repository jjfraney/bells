package org.flyboy.bells.player;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
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
     * @param cmd mpd command as string
     * @return lines (newlines omitted) of the response.
     */
    public Uni<List<String>> mpd(String cmd) {
       return getNetClient().connect(PORT, "localhost")
                .onItem().invoke(netSocket -> {
                    netSocket.writeAndForget(cmd + "\nclose\n");

                    // Must close the socket for the mutiny stream to complete promptly
                    // after receiving the mpd response.

                    // Ignoring write failures.  Expecting
                    // socket fails to raise in the following reads.
                    // If fails do not raise, then could lead to hang.
                    // Too ridiculous to think about and test,
                    // so leaving it here.
                })
                .onItem().transformToMulti(NetSocket::toMulti)
                .onItem().transform(Buffer::toString)
                .flatMap(s -> Multi.createFrom().items(s.split("\n")))
                .collect().with(Collectors.toList())
                ;
    }
}
