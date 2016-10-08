package org.jjfflyboy.bells.scheduler.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfraney
 */
public class MPC {
    private String host = "localhost";
    private int port = 6600;

    public MPC() {
    }
    public MPC(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public <R extends Command.Response> R send(Command command) throws IOException {
        String[] result = send(command.text());
        return (R) command.response(result);
    }

    public String[] send(String textCommand) throws IOException {
        String [] result = new String[] {};
        // try-with-resource: opens socket, a a writer toMpd, and a reader fromMpd
        SocketAddress address = new InetSocketAddress(host, port);
        try (SocketChannel channel = SocketChannel.open()) {

            channel.connect(address);

            PrintWriter toMpd = new PrintWriter(Channels.newOutputStream(channel), true);
            BufferedReader fromMpd = new BufferedReader(new InputStreamReader(Channels.newInputStream(channel)));

            String connectResponse;
            // first, expect the connect status
            while ((connectResponse = fromMpd.readLine()) != null) {
                if (responseComplete(connectResponse)) {
                    break;
                }
            }

            // now send the command as text
            toMpd.println(textCommand);

            // then read response into a List
            List<String> lines = new ArrayList<>();
            String responseSegment;
            while ((responseSegment = fromMpd.readLine()) != null) {
                lines.add(responseSegment);
                if (responseComplete(responseSegment)) {
                    break;
                }
            }

            result = lines.toArray(new String[lines.size()]);
        }
        return result;
    }

    private static boolean responseComplete(String response) {
        return response.startsWith("OK") || response.startsWith("ACK");
    }
}
