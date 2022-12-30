package org.flyboy.bells.tower;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.net.NetClient;
import io.vertx.mutiny.core.net.NetSocket;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service to send commands to and receive responses from
 * mpd service.  The mpd service is linux based audio player service.
 *
 * @author John J. Franey
 * @see <a href="https://mpd.readthedocs.io/en/latest/index.html">MPD Documentation</a>
 */
@ApplicationScoped
public class LinuxMPC {


    private static final Logger logger = Logger.getLogger(LinuxMPC.class);

    @Inject
    NetClient netClient;


    @ConfigProperty(name = "mpd.port", defaultValue = "6600")
    int mpdPort;

    @ConfigProperty(name = "mpd.host", defaultValue = "localhost")
    String mpdHost;

    /**
     * Return Uni which when subscribed would send command to
     * mpd and return response as list of lines.
     *
     * @param cmd as string
     * @return lines of the response - end of line removed on each.
     * @see <a href="https://mpd.readthedocs.io/en/latest/index.html">MPD Documentation</a>
     */
    public Uni<List<String>> mpc(String cmd) {
        return netClient.connect(mpdPort, mpdHost)
                .onFailure(ConnectException.class).invoke(r -> logger.error("Unable to connect to MPD service: " + r.getMessage()))
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

    /**
     * sends a command list using MPD 'command_begin_ok', 'command_end'.
     *
     * @param first command
     * @param last  commands as variable args
     * @return list of response lines isolated by 'list_OK' (per MPD protocol)
     */
    public Uni<List<String>> mpc(String first, String... last) {
        List<String> commands = new ArrayList<>();
        commands.add(first);
        commands.addAll(Arrays.asList(last));
        return mpc(commands);
    }

    public Uni<List<String>> mpc(List<String> commands) {
        Objects.requireNonNull(commands);
        commands.forEach(Objects::requireNonNull);
        if (commands.size() <= 0) {
            throw new IllegalArgumentException("one or more commands expected");
        }

        String wrappedCommands =
                "command_list_ok_begin\n"
                        + String.join("\n", commands)
                        + "\ncommand_list_end";
        return mpc(wrappedCommands);

    }
}
