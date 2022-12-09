package org.flyboy.bells.tower;


import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author John J. Franey
 */
public class MpdResponse {

    public static Optional<String> getField(List<String> responseLines, String name) {
        final String search = name + ": ";
        return responseLines.stream()
                .filter((x) -> x.startsWith(search))
                .findFirst()
                .map(f -> f.substring(search.length()))
                .filter(l -> l.trim().length() != 0);
    }

    public static Boolean isOk(List<String> responseLines) {
        return responseLines.size() > 1 && responseLines.get(responseLines.size() - 1).equals("OK");
    }
    public static Optional<String> getAckLine(List<String> responseLines) {
        Optional<String> result = Optional.ofNullable(null);
        if(responseLines.size() > 0 && responseLines.get(responseLines.size() - 1).startsWith("ACK")) {
            result = Optional.of(responseLines.get(responseLines.size() - 1));
        }
        return result;
    }

    public static Ack getAck(List<String> responseLines) {
        // you must test for Ack first, as positively present, or because OK is missing.
        String ack = getAckLine(responseLines).orElseThrow(() -> new IllegalArgumentException("Ack is missing from response"));
        return new Ack(ack);
    }

    public static class Ack {
        private final Pattern PATTERN = Pattern.compile("ACK \\[(\\d*)@(\\d*)\\] \\{(.*)\\} (.*)");
        final private Integer error;
        final private Integer commandListNum;
        final private String currentCommand;
        final private String messageText;

        public Ack(String line) {
            Matcher m = this.PATTERN.matcher(line);
            if (m.matches()) {
                this.error = Integer.parseInt(m.group(1));
                this.commandListNum = Integer.parseInt(m.group(2));
                this.currentCommand = m.group(3);
                this.messageText = m.group(4);
            } else {
                throw new BelltowerException("bad parse");
            }
        }

        public Integer getError() {
            return this.error;
        }

        public Integer getCommandListNum() {
            return this.commandListNum;
        }

        public String getCurrentCommand() {
            return this.currentCommand;
        }

        public String getMessageText() {
            return this.messageText;
        }
    }
}
