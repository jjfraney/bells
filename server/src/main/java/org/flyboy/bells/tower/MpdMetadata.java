package org.flyboy.bells.tower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class that parses and holds the metadata returned by lsinfo mpd command and others.
 *
 * @author John J. Franey
 * @see <a href="https://mpd.readthedocs.io/en/latest/protocol.html#command-lsinfo">MPD Protocol Document on lsinfo</a>
 */
public class MpdMetadata {
    private static final Pattern FILE_PATTERN = Pattern.compile("file: (.*)");
    private static final Pattern DURATION_PATTERN = Pattern.compile("duration: (.*)");
    private final List<Song> songs = new ArrayList<>();
    private final Pattern extensionPattern = Pattern.compile("\\.");

    /**
     * @param response from mpd to command returning metatdata.
     */
    MpdMetadata(List<String> response) {

        String filename = null;

        // MPD sends back the same
        // file will always appear before duration in a record.
        // if we see duration first, then ignore it.
        // if we see two files in a row, ignore the first one.
        // if input end without a duration to last file name....so what.
        // maybe we can process non-repeating samples without a duration.
        for (String s : response) {
            // is this the file line?
            // matches 'file: call-to-mass.ogg'
            Matcher fm = FILE_PATTERN.matcher(s);
            if (fm.matches()) {
                // forget the earlier filename
                filename = fm.group(1);
                continue;
            }

            // is this the duration line?
            // matches 'duration: 9.9'
            Matcher dm = DURATION_PATTERN.matcher(s);
            if (dm.matches()) {
                if (filename != null) {
                    try {
                        // duration in number of milliseconds
                        long duration = (long) (Double.parseDouble(dm.group(1)) * 1000);
                        songs.add(new Song(filename, duration));
                    } catch (NumberFormatException ignored) {

                    }
                    filename = null;
                }
            }
        }

    }

    /**
     * return list of available songs which match the named song.
     * <p>
     * The return can be a list of three the variable case,
     * or a list with a single item for the fixed case.
     * </p>
     *
     * @param name of song requested
     * @return list of matching songs
     */
    public List<Song> findMatch(final String name) {
        List<Function<String, List<Song>>> checks = Arrays.asList(
                this::findExact,
                this::findIgnoreExtension,
                this::findByVariableForm
        );

        return checks.stream()
                .map(f -> f.apply(name))
                .filter(l -> l.size() > 0).findFirst()
                .orElse(Collections.emptyList());
    }

    /**
     * compares the name with filenames by exact match.
     *
     * @param name of song requested
     * @return list of matching songs
     */
    List<Song> findExact(String name) {
        return songs.stream()
                .filter(s -> s.filename.equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    /**
     * compares the name with filenames without their extensions.
     *
     * @param name of song requested
     * @return list of matching songs
     */
    List<Song> findIgnoreExtension(String name) {
        return songs.stream()
                .filter(s -> extensionPattern.split(s.filename, 2)[0].equals(name))
                .collect(Collectors.toList());
    }

    /**
     * returns three samples in order (beginning, middle, end) of
     * a variable song and ignores extension.
     *
     * @param name of a bell sample in three segments.
     * @return list of segments ordered by position if found, else an empty list.
     */
    List<Song> findByVariableForm(String name) {
        List<Song> result = Stream.of("beginning", "middle", "end")
                .map(segment -> findIgnoreExtension(name + "-" + segment))
                .filter(l -> l.size() == 1)
                .map(l -> l.get(0))
                .toList();
        return result.size() == 3 ? result : Collections.emptyList();

    }

    record Song(String filename, long duration) {
    }
}
