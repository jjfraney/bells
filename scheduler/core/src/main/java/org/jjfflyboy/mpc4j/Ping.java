package org.jjfflyboy.mpc4j;

/**
 * @Author jfraney
 */
public class Ping implements Command<Ping.Response> {
    @Override
    public String text() {
        return "ping";
    }

    @Override
    public Ping.Response response(String[] responseLines) {
        return new Response(responseLines);
    }

    public static class Response extends AbstractCommandResponse {
        Response(String[] responseLines) {
            super(responseLines);
        }
    }

}
