package org.jjfflyboy.mpc4j;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author jfraney
 */
public class Status implements Command<Status.Response> {
    @Override
    public String text() {
        return "status";
    }

    public static class Response extends AbstractCommandResponse {
        // according to mpd source code: src/command/PlayerCommands.cxx

        public Optional<Integer> getVolume() {
            return getIntegerValue("volume");
        }
        public Optional<Toggle> getRepeat() {
            return getToggleValue("repeat");
        }
        public Optional<Toggle> getRandom() {
            return getToggleValue("random");
        }
        public Optional<Toggle> getSingle() {
            return getToggleValue("single");
        }
        public Optional<Toggle> getConsume() {
            return getToggleValue("consume");
        }
        public Optional<Integer> getPlaylist() {
            return getIntegerValue("playlist");
        }
        public Optional<Integer> getPlaylistLength() {
            return getIntegerValue("playlistlength");
        }
        public Optional<BigDecimal> getMixRampDb() {
            return getBigDecimalValue("mixrampdb");
        }
        public Optional<BigDecimal> getMixRampDelay() {
            return getBigDecimalValue("mixrampdelay");
        }
        public Optional<Integer> getUpdatingDb() {
            return getIntegerValue("updating_db");
        }
        public Optional<String> getState() {
            return getStringValue("state");
        }
        public Optional<String> getError() {
            return getStringValue("error");
        }
        // these fields appear while state == 'play'
        public Optional<Integer> getSongId() {
            return getIntegerValue("songid");
        }
        public Optional<Integer> getSong() {
            return getIntegerValue("song");
        }
        public Optional<Integer> getNextSong() {
            return getIntegerValue("nextsong");
        }
        public Optional<Integer> getNextSongId() {
            return getIntegerValue("nextsongid");
        }
        public Optional<String> getTime() {
            return getStringValue("time");
        }
        public Optional<BigDecimal> getElapsed() {
            return getBigDecimalValue("elapsed");
        }
        public Optional<Integer> getBitRate() {
            return getIntegerValue("bitrate");
        }

        /**
         * @return in format "%u:%s:%u", "44100:16:2"
         */
        public Optional<String> getAudio() {
            return getStringValue("audio");
        }
        public Optional<Integer> getXfade() {
            return getIntegerValue("xfade");
        }

        /**
         * @param name
         * @return an Optional for the value of the field referenced by 'name'
         */
        private Optional<String> findFieldValue(String name) {
            final String search = name + ": ";
            return Stream.of(getResponseLines())
                    .filter((x) -> x.startsWith(search))
                    .findFirst()
                    .map(f -> f.substring(search.length()));
        }
        private Optional<Integer> getIntegerValue(String fieldName) {
            return findFieldValue(fieldName).map((s) -> Integer.decode(s));
        }
        private Optional<Toggle> getToggleValue(String fieldName) {
            return findFieldValue(fieldName).map(s -> Toggle.decode(s));
        }
        private Optional<BigDecimal> getBigDecimalValue(String fieldName) {
            return findFieldValue(fieldName).map(s -> new BigDecimal(s));
        }
        private Optional<String> getStringValue(String fieldName) {
            return findFieldValue(fieldName);
        }

        Response(String[] responseLines) {
            super(responseLines);
        }
    }

    @Override
    public Response response(String[] responseLines) {
        return new Response(responseLines);
    }
}
