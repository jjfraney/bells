package org.jjfflyboy.mpc4j;

/**
 * @Author jfraney
 */
public class Update implements Command<Update.Response> {
    @Override
    public String text() {
        return "update";
    }

    @Override
    public Response response(String[] responseLines) {
        return new Response(responseLines);
    }
    public static class Response extends AbstractCommandResponse {
        Response(String[] responseLines) {
            super(responseLines);
        }
    }
}
