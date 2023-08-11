package org.flyboy.bells.belfry;


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
        return responseLines.size() > 1 && responseLines.get(responseLines.size() - 1).contains("OK");
    }

    public static Ack getAck(List<String> responseLines) {
        if (responseLines.size() > 0) {
            String lastLine = responseLines.get(responseLines.size() - 1);
            if (lastLine.startsWith("ACK")) {
                return new Ack(lastLine);
            }
        }
        throw new IllegalArgumentException("Ack is missing from response");
    }

    public static class Ack {
        /**
         * RegEx to parse the ack line.
         * <ul>
         *     <li>group(1) : error code</li>
         *     <li>group(2) : command list number</li>
         *     <li>group(3) : current command</li>
         *     <li>group(4) : message text</li>
         * </ul>
         */
        private final static Pattern PATTERN = Pattern.compile("ACK \\[(\\d*)@(\\d*)] \\{(.*)} (.*)");
        final private Integer error;
        final private String messageText;

        public Ack(String line) {
            Matcher m = Ack.PATTERN.matcher(line);
            if (m.matches()) {
                this.error = Integer.parseInt(m.group(1));
                this.messageText = m.group(4);
            } else {
                throw new BelfryException("bad parse");
            }
        }

        public Integer getError() {
            return this.error;
        }

        public String getMessageText() {
            return this.messageText;
        }
    }
}
