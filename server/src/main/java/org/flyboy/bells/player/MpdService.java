package org.flyboy.bells.player;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.core.net.NetSocket;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

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

    private static Logger log = Logger.getLogger(MpdService.class);

    NetClient netClient;

    NetClient getNetClient() {
        if(netClient == null) {
            netClient = vertx.createNetClient();
        }
        return netClient;
    }

    @ConfigProperty(name = "mpd.port", defaultValue = "6600")
    int mpdPort;

    @ConfigProperty(name = "mpd.host", defaultValue = "localhost")
    String mpdHost;

    /**
     * sends the mpd command to the player and returns the response as list of lines from MPD
     * @param cmd mpd command as string
     * @return lines (newlines omitted) of the response.
     */
    public Uni<List<String>> mpd(String cmd) {
        log.infof("getting mpd net socket, port: %d, host: %s", mpdPort, mpdHost);

       return getNetClient().connect(mpdPort, "localhost")
                .onItem().invoke(netSocket -> {
                    netSocket.writeAndForget(cmd + "\nclose\n");

                    // Must close the socket for the mutiny stream to complete promptly
                    // after receiving the mpd response.

                    // reusing the netSocket might not be worth it.
                    // sure would like to know how, though.

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
