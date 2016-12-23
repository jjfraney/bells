package org.jjfflyboy.bells.scheduler.core;

import com.github.jjfraney.mpc.Command;
import com.github.jjfraney.mpc.MPC;
import musicpd.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author jfraney
 */
public class Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);
    private static String host;
    private static Integer port;
    private static String stragegy;

    static {
        Settings settings = new PropertySettings();
        host = settings.getMpdHost();
        port = settings.getMpdPort();
        stragegy = settings.getPlayerStrategy();
        LOGGER.info("Player host={}, port={}, strategy={}", host, port, stragegy);
    }

    public static void play(String song) {
        MPC mpc = new MPC(host, port);
        play(song, mpc);
    }

    interface Strategy {
        void play(MPC mpc, Command ... commands);
    }
    public static class StrategyCommandList implements Strategy {
        public void play(MPC mpc, Command ... commands) {
            LOGGER.debug("player strategy={}", this.getClass().getSimpleName());

            Command[] list = Arrays.copyOf(commands, commands.length + 1);
            list[list.length - 1] = new Status();

            CommandList commandList = new CommandList(list);
            CommandList.Response commandListResponse = send(mpc, commandList);
            LOGGER.trace("play commands response: {}", commandListResponse.getResponseLines());
            int statusIndx = commandListResponse.getResponses().size() - 1;
            Status.Response sr = (Status.Response) commandListResponse.getResponses().get(statusIndx);
            LOGGER.debug("player status: {}, play error: {},", sr.getState().orElse("unknown"), sr.getError().orElse("no error"));
        }
    }
    public static class StrategySingleStep implements Strategy {
        public void play(MPC mpc, Command ... commands) {
            LOGGER.debug("player strategy={}", this.getClass().getSimpleName());
            for(Command command: commands) {
                Command.Response response = send(mpc, command);
                LOGGER.debug("sent command={}, isOk={}", command.text(), response.isOk());
                LOGGER.trace("command={}, response={}", command.text(), response.getResponseLines());
                if(!response.isOk()) {
                    break;
                }
            }

            Status.Response sr = send(mpc, new Status());
            LOGGER.trace("command=status, response={}", sr.getResponseLines());
            LOGGER.debug("player status: {}, play error: {},", sr.getState().orElse("unknown"), sr.getError().orElse("no error"));
        }
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

        try {
            getStrategy().play(mpc, clear, add , play);
        } catch (RuntimeException e) {
            LOGGER.error("Unable to play song. song={}, message={}", song, e.getMessage());
        }
    }

    private static Strategy getStrategy() {
        if(stragegy.equals("list")) {
            return new StrategyCommandList();
        } else {
            return new StrategySingleStep();
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
