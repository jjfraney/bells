package org.jjfflyboy.bells.scheduler.core;

import com.github.jjfraney.mpc.Command;
import com.github.jjfraney.mpc.MPC;
import musicpd.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author jfraney
 */
public class Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);

    public static void play(String song) {
        Settings settings = new PropertySettings();
        String host = settings.getMpdHost();
        Integer port = settings.getMpdPort();
        MPC mpc = new MPC(host, port);
        play(song, mpc);
    }

    private static void play(String song, MPC mpc) {

        Status status = new Status();
        Status.Response statusResponse = null;
        try {
            statusResponse = send(mpc, status);
        } catch (RuntimeException e) {
            LOGGER.error("Unable to get status. song={}, message={}", song, e.getMessage());
            return;
        }

        String state = statusResponse.getState().orElseThrow(() ->  new RuntimeException("state is not available"));

        if("play".equals(state)) {
            LOGGER.info("Cannot play this song because another is already playing.  song={}", song);
            return;
        }

        Clear clear = new Clear();
        Add add = new Add(song);
        Play play = new Play();

        CommandList commandList = new CommandList(clear, add, play, status);
        try {
            CommandList.Response commandListResponse = send(mpc, commandList);
            int statusIndx = commandListResponse.getResponses().size() - 1;
            Status.Response sr = (Status.Response) commandListResponse.getResponses().get(statusIndx);
            LOGGER.debug("player status: {}, play error: {},", sr.getState().orElse("unknown"), sr.getError().orElse("no error"));
        } catch (RuntimeException e) {
            LOGGER.error("Unable to play song. song={}, message={}", song, e.getMessage());
        }
    }

    private static <C extends Command<R>, R extends Command.Response>
        R send(MPC mpc, C command) {

        R response;
        try {
            response = mpc.send(command);
        } catch(IOException e) {
            throw new RuntimeException("IO Error");
        }

        if(! response.isOk()) {
            Command.Response.Ack ack = response.getAck().orElseThrow(() -> new RuntimeException("command fail, no ack"));

            String commandText = command.text();
            if(command instanceof CommandList) {
                commandText = ack.getCurrentCommand();
            }
            String msg = new StringBuilder()
                    .append("command fail. ")
                    .append("command=").append(commandText)
                    .append(", ")
                    .append("error=").append(ack.getError())
                    .append(",")
                    .append("message=").append(ack.getMessageText())
                    .toString();
            throw new RuntimeException(msg);
        }
        return response;
    }

    public static void main(String[] args) {
        Player.play("call-to-mass.ogg");
    }
}