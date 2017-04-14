package org.jjfflyboy.bells.scheduler.core;

import com.github.jjfraney.mpc.Command;
import com.github.jjfraney.mpc.MPC;
import com.github.jjfraney.mpc.QueueQueryResponse;
import com.github.jjfraney.mpc.Toggle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;
import musicpd.protocol.Add;
import musicpd.protocol.Clear;
import musicpd.protocol.CommandList;
import musicpd.protocol.Crossfade;
import musicpd.protocol.Play;
import musicpd.protocol.PlaylistInfo;
import musicpd.protocol.Repeat;
import musicpd.protocol.Single;
import musicpd.protocol.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A bell song can have three segments, a beginning, a repeatable middle,
 * and a trailing end.  This verticle plays and controls such a song.
 * <p>
 *     The verticle plays the beginning and middle segment.  While
 *     the middle segment is plays, the verticle sets the player to
 *     repeat the song indefinitely.  The verticle will set the player
 *     to stop repeating when the song has played for the desired duration.
 * </p>
 * @author jfraney
 */
public class PlaySegmentedVerticle extends AbstractVerticle {
    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        if(args.length == 0) {
            args = new String[]{"run", PlaySegmentedVerticle.class.getName()};
        }
        new Launcher().dispatch(args);
    }
    private static Logger LOGGER = LoggerFactory.getLogger(PlaySegmentedVerticle.class);

    private final Repeat REPEAT_OFF = new Repeat(Toggle.OFF);
    private final Single SINGLE_OFF = new Single(Toggle.OFF);
    private final Repeat REPEAT_ON = new Repeat(Toggle.ON);
    private final Single SINGLE_ON = new Single(Toggle.ON);
    private final Crossfade CROSSFADE_ON = new Crossfade(1);

    private MPC mpc;

    // id of timer which fires to turn off repeat
    private long timerIdRepeatOff;
    // id of timer which fires to turn on repeat
    private long timerIdRepeatOn;

    public void start() {
        mpc = new MPC();

        vertx.eventBus().consumer("bell-tower.segmented", message -> {
            LOGGER.debug("received command, msg={}", message.body());
            JsonObject msg = (JsonObject)message.body();
            String command = msg.getString("command");
            String root = msg.getString("song");
            Integer playTime = msg.getInteger("playTime");

            if(command.startsWith("play")) {
                playSegmented(root, playTime);
            }
            if(command.startsWith("stop")) {
                stopSegmented();
            }
        });
    }

    private void playSegmented(String song, Integer playTime) {
        LOGGER.info("Playing, {}", song);
        final String BEGINNING = "-beginning";
        final String MIDDLE = "-middle";
        final String END = "-end";


        String leadSegment = song + BEGINNING + ".ogg";
        String midSegment = song + MIDDLE + ".ogg";
        String trailSegment = song + END + ".ogg";


        if(isPlayerBusy()) {
            LOGGER.info("Cannot play this song because another is already playing.");
        } else {
            Clear clear = new Clear();
            Add beg = new Add(leadSegment);
            Add mid = new Add(midSegment);
            Add end = new Add(trailSegment);
            PlaylistInfo info = new PlaylistInfo();
            CommandList list = new CommandList(clear, beg, mid, end, info, REPEAT_OFF, SINGLE_OFF, CROSSFADE_ON);

            CommandList.Response response = sendCommand(list);

            // info command is #4 in command list.
            QueueQueryResponse infoResponse = (QueueQueryResponse)(response.getResponses().get(4));
            infoResponse.getSongMetadata().forEach(r -> LOGGER.debug("song data, name={}, duration={}", r.getFile(), r.getTime()));
            Integer begTime = infoResponse.getSongMetadata().get(0).getTime().orElse(0);
            Integer midTime = infoResponse.getSongMetadata().get(1).getTime().orElse(0);
            Integer endTime = infoResponse.getSongMetadata().get(2).getTime().orElse(0);

            Play play = new Play();
            Play.Response playResponse = sendCommand(play);

            playResponse.getConnectResponse();

            LOGGER.debug("begTime={}, midTime={}, endTime={}, playTime={}", begTime, midTime, endTime, playTime);
            if(begTime + midTime + endTime < playTime) {
                // repeat the middle this many times
                Integer repeats = (playTime - begTime - endTime) / midTime;
                Integer total = begTime + endTime + midTime * repeats;
                repeats = repeats + ((playTime - total) + midTime / 2) / midTime;
                LOGGER.debug("calculated repeats={}, overallTime={}", repeats, begTime + endTime + midTime * repeats);

                // delay from beginning to start repeat middle segment
                int repeatOnDelay = begTime * 1000 + 500;
                // delay from beginning: to stop repeat middle segment
                int repeatOffDelay = (begTime + midTime * repeats) * 1000 - 500;
                LOGGER.debug("repeatOnDelay={},repeatOffDelay={}", repeatOnDelay, repeatOffDelay);


                // the middle segment is repeated
                // timer to turn on repeat
                timerIdRepeatOn = vertx.setTimer(repeatOnDelay, h -> {
                    startRepeat();
                });

                // timer to turn off repeat
                timerIdRepeatOff = vertx.setTimer(repeatOffDelay, h -> {
                    stopRepeat();
                });

            } else {
                LOGGER.debug("no repeats");
            }
        }
    }

    private void stopSegmented() {
        stopRepeat();
    }

    private void startRepeat() {
        LOGGER.debug("starting repeats");
        sendCommand(new CommandList(REPEAT_ON, SINGLE_ON));
    }

    private void stopRepeat() {
        if(timerIdRepeatOff != 0) {
            LOGGER.debug("stopping repeats");
            vertx.cancelTimer(timerIdRepeatOn);
            timerIdRepeatOn = 0;
            vertx.cancelTimer(timerIdRepeatOff);
            timerIdRepeatOff = 0;
            sendCommand(new CommandList(REPEAT_OFF, SINGLE_OFF));
        }
    }

    private boolean isPlayerBusy() {
        Status.Response statusResponse = sendCommand(new Status());
        String state = statusResponse.getState().orElseThrow(() ->  new RuntimeException("state is not available"));
        return "play".equals(state);
    }

    private <R extends Command.Response, C extends Command<R>> R sendCommand( C command) {
        R response;
        try {
            response = mpc.send(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(!response.isOk()) {
            String cmdName = command.getClass().getSimpleName();
            Command.Response.Ack ack = response.getAck().orElseThrow(() -> new RuntimeException(cmdName + " fail, no ack"));
            LOGGER.error("The {} command failed: {}", cmdName, ack.getMessageText());
            throw new RuntimeException(cmdName + " not ok.");
        }
        return response;
    }
}
