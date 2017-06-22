package org.jjfflyboy.bells.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.jjfraney.mpc.Command;
import com.github.jjfraney.mpc.MPC;
import com.github.jjfraney.mpc.QueueQueryResponse;
import com.github.jjfraney.mpc.Toggle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import musicpd.protocol.Add;
import musicpd.protocol.Clear;
import musicpd.protocol.CommandList;
import musicpd.protocol.Crossfade;
import musicpd.protocol.Play;
import musicpd.protocol.PlaylistId;
import musicpd.protocol.PlaylistInfo;
import musicpd.protocol.Repeat;
import musicpd.protocol.Single;
import musicpd.protocol.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Plays bells songs as per received event from vertx event bus.
 * <p>
 *     A bell song has fixed or variable length.  This verticle plays either.
 * </p>
 * <p>
 *     A fixed length song is a single media file.  The verticle plays the media
 *     file without repeats.
 * </p>
 * <p>
 *     A variable length song has 3 media files: a beginning, middle and end.
 *     The media files are specially created from a single media file.
 *     A one second crossfade must be built into the files for a single second (1s) crossfade.
 *     This design supports a seamless play from one segment file to the next and allows
 *     the middle file to be repeated any number of times for variable length play.
 *     The media files can be created with the Audacity audio editor from a
 *     single media sample with a pattern of bell ringing repeated 3 times.
 *     <ul>
 *         <li>The beginning starts the song, and the last second overlaps the middle's first second</li>
 *         <li>The middle's first second overlaps the beginning's last second,
 *         and the middle's last second overlaps its own first second, and the end's first second.
 *         <li>The end's first second overlaps the middle's last second
 *         and ends the song.</li>
 *     </ul>
 *     As an example, from a sample of a bell tolling three times at constant frequency of 5 seconds and
 *     the last toll trailing off for 10 seconds, the three segments would be isolated this way:
 *     <ul>
 *         <li>The beginning would start at the start of the original sample and end just before the second toll.
 *         The length of the beginning would be 5 seconds.</li>
 *         <li>The middle would start at 4 second offset from the original sample to overlap
 *         with the last second of the beginning, and end just before
 *         the third toll at 10 seconds.  The length of the middle is 6 seconds.</li>
 *         <li>The end would start at 9 second offset of the original sample
 *         to overlap with the last second of the middle,
 *         and end after the full trailing tone of the bell.
 *         The length of the end is 11 seconds.</li>
 *     </ul>
 *     The files are played with MPC's crossfade option set to one second (1s).  The crossfade creates a clean segueway
 *     from one file to the next.  The song is variable because the middle can repeat any number of times
 *     with clean sequeway to itself.
 *     This scheme fails with an original sample of randomized bell strikes.
 *     The frequency of bell strikes,
 *     and the tone and amplitude of the bell must demonstrate a regular pattern.
 * </p>
 * @author jfraney
 */
public class PlayerVerticle extends AbstractVerticle {
    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        if(args.length == 0) {
            args = new String[]{"run", PlayerVerticle.class.getName()};
        }
        new Launcher().dispatch(args);
    }
    private static Logger LOGGER = LoggerFactory.getLogger(PlayerVerticle.class);

    private final Clear CLEAR = new Clear();
    private final Status STATUS = new Status();
    private final Play PLAY = new Play();
    private final Repeat REPEAT_OFF = new Repeat(Toggle.OFF);
    private final Single SINGLE_OFF = new Single(Toggle.OFF);
    private final Repeat REPEAT_ON = new Repeat(Toggle.ON);
    private final Single SINGLE_ON = new Single(Toggle.ON);
    private final Crossfade CROSSFADE_ON = new Crossfade(1);
    private final Crossfade CROSSFADE_OFF = new Crossfade(0);

    private MPC mpc;

    // id of timer which fires to turn off repeat
    private long timerIdRepeatOff;
    // id of timer which fires to turn on repeat
    private long timerIdRepeatOn;

    public void start() {
        String mpdHost = config().getString("mpdHost");
        Integer mpdPort = config().getInteger("mpdPort");
        mpc = new MPC(mpdHost, mpdPort);

        vertx.eventBus().consumer("bell-tower.segmented", message -> {
            LOGGER.debug("received command, msg={}", message.body());
            JsonObject msg = (JsonObject)message.body();
            String command = msg.getString("command");
            String root = msg.getString("song");
            Integer playTime = msg.getInteger("playTime", 5*60);

            if(command.startsWith("play")) {
                playSegmented(root, playTime);
            }
            if(command.startsWith("stop")) {
                stopSegmented();
            }
        });

        vertx.eventBus().consumer("bell-tower.player", message -> {
            String command = message.body().toString();
            if(command.startsWith("play")) {
                LOGGER.debug("received command: {}", command);
                String[] c = command.split(" ");
                if(c.length > 0) {
                    playSong(c[1]);
                }
            }
        });

        vertx.eventBus().consumer("bell-tower", message -> {
            LOGGER.debug("received command, msg={}", message.body());
            JsonObject msg = (JsonObject) message.body();
            String command = msg.getString("command");
            if ("status".equals(command)) {
                publishStatus();
            }
        });

    }

    private void publishStatus() {
        String statusJson;

        PlayerStatus status = new PlayerStatus();

        Status.Response statusResponse = sendCommand(new Status());
        String state = statusResponse.getState().orElse("unknown");
        status.setState(state);


        if(state.equals("play") ) {
            String songFile = "unknown";
            Integer songId = statusResponse.getSongId().orElse(-1);
            if(songId > 0) {
                QueueQueryResponse playlistIdResponse = sendCommand(new PlaylistId(songId));
                List<QueueQueryResponse.QueuedSongMetadata> songs = playlistIdResponse.getSongMetadata();
                if(songs.size() > 0) {
                    songFile = songs.get(0).getFile().orElse("unknown");
                }
            }
            status.setSongFile(songFile);
        }

        status.setTime(ZonedDateTime.now(ZoneId.systemDefault()));

        try {
            statusJson = Json.mapper
                    .writer()
                    .forType(PlayerStatus.class)
                    .writeValueAsString(status);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        vertx.eventBus().publish("bell-tower.player.status", statusJson);

    }
    private void playSong(String song) {

        if(isPlayerBusy()) {
            LOGGER.info("Cannot play this song because another is already playing.  song={}", song);
            return;
        }

        CommandList commandList = new CommandList(CLEAR, CROSSFADE_OFF, new Add(song), PLAY, STATUS);

        try {
            CommandList.Response commandListResponse = sendCommand(commandList);
            LOGGER.trace("play commands response: {}", commandListResponse.getResponseLines());
            int statusIndx = commandListResponse.getResponses().size() - 1;
            Status.Response sr = (Status.Response) commandListResponse.getResponses().get(statusIndx);
            LOGGER.debug("player status: {}, play error: {},", sr.getState().orElse("unknown"), sr.getError().orElse("no error"));

        } catch (RuntimeException e) {
            LOGGER.error("Unable to play song. song={}, message={}", song, e.getMessage());
        }
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
            Add beg = new Add(leadSegment);
            Add mid = new Add(midSegment);
            Add end = new Add(trailSegment);
            PlaylistInfo info = new PlaylistInfo();
            CommandList list = new CommandList(CLEAR, beg, mid, end, info, REPEAT_OFF, SINGLE_OFF, CROSSFADE_ON);

            CommandList.Response response = sendCommand(list);

            // info command is #4 in command list.
            QueueQueryResponse infoResponse = (QueueQueryResponse)(response.getResponses().get(4));
            infoResponse.getSongMetadata().forEach(r -> LOGGER.debug("song data, name={}, duration={}", r.getFile(), r.getTime()));
            Integer begTime = infoResponse.getSongMetadata().get(0).getTime().orElse(0);
            Integer midTime = infoResponse.getSongMetadata().get(1).getTime().orElse(0);
            Integer endTime = infoResponse.getSongMetadata().get(2).getTime().orElse(0);

            Play.Response playResponse = sendCommand(PLAY);

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

    public static class PlayerStatus {
        private ZonedDateTime time;
        private String songFile;
        private String state;

        public ZonedDateTime getTime() {
            return time;
        }

        public void setTime(ZonedDateTime time) {
            this.time = time;
        }


        public String getSongFile() {
            return songFile;
        }

        public void setSongFile(String songFile) {
            this.songFile = songFile;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
