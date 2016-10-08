package org.jjfflyboy.mpc4j;


/**
 * A command and response of the MPD protocol.
 * <p>
 *     This interface encapsulates the protocol's text representation
 *     of commands and responses.  An implementation knows how to
 *     generate the command-text and parse the response-text of the protocol.
 *     An implementation of 'Command' can define instance fields for command
 *     arguments. An implementation of 'Command.Response' can define instance
 *     fields for response data.
 * </p>
 * @author jfraney
 */
interface Command<R extends Command.Response> {
    /**
     * this command as an mpd protocol text-command.
     * @return text representation of this command (according to mpd protocol).
     */
    String text();

    /**
     * parse (eagerly or lazily) mpd's text-response to this command.
     * @param responseLines given by mpd server in response to this command.
     * @return an actual Response to this Command.
     */
    R response(String[] responseLines);

    /**
     * Response of this command.
     */
    interface Response {
        String[] getResponseLines();

        boolean isOk();
    }
}
