package org.jjfflyboy.mpc4j;

/**
 * @author jfraney
 */
public abstract class AbstractCommandResponse implements Command.Response {
    final private boolean ok;
    final private String[] responseLines;

    AbstractCommandResponse(String[] responseLines) {
        this.responseLines = responseLines;
        boolean responseOk = false;
        if(responseLines.length > 0) {
            if(responseLines[responseLines.length - 1].startsWith("OK")) {
                responseOk = true;
            }
        }
        this.ok = responseOk;
    }

    @Override
    public boolean isOk() {
        return ok;
    }

    @Override
    public String[] getResponseLines() {
        return responseLines;
    }
}
