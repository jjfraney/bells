package org.jjfflyboy.mpc4j;

/**
 * @author jfraney
 */
public class SetVol implements Command<SetVol.Response> {
    private Integer vol;

    public SetVol(Integer vol) {
        this.vol = vol;
    }

    @Override
    public String text() {
        return "setvol " + vol.toString();
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
