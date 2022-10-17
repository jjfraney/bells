package org.flyboy.bells.player;

import com.github.jjfraney.mpc.Command;
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
 * Service to send commands to and receive responses from
 * mpd service.  The mpd service is linux based audio player service.
 * @see <a href="https://mpd.readthedocs.io/en/latest/index.html">MPD Documentation</a>
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
     * Return Uni which when subscribed would send {@link Command to
     * mpd and return response as {@link Command.Response}
     * @param command mpc command.
     * @return uni of command response.
     * @see Command
     * @see Command.Response
     * @see <a href="https://mpd.readthedocs.io/en/latest/index.html">MPD Documentation</a>
     */
    public <R extends Command.Response> Uni<R> mpc(Command<R> command) {
        return mpc(command.text())
                .onItem().transform(list -> command.response(list.subList(1, list.size() -1), list.get(0)));
    }

    /**
     * Return Uni which when subscribed would send command to
     * mpd and return response as list of lines.
     * @param cmd as string
     * @return lines of the response - end of line removed on each.
     * @see <a href="https://mpd.readthedocs.io/en/latest/index.html">MPD Documentation</a>
     */
    public Uni<List<String>> mpc(String cmd) {
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
