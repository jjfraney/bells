package org.flyboy.belltower.belfry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author John J. Franey
 */
public class MpdMetadataTest {
    private final String sample = """
    OK MPD 0.23.5
    file: funeral-toll-beginning.ogg
    Last-Modified: 2022-12-23T20:46:19Z
    Format: 44100:f:2
    Artist: John J. Franeuy
    Title: Funeral Toll beginning
    Time: 8
    duration: 7.592
    file: funeral-toll-end.ogg
    Last-Modified: 2022-12-23T20:46:19Z
    Format: 44100:f:2
    Artist: John J. Franeuy
    Title: Funeral Toll end
    Time: 11
    duration: 11.244
    file: funeral-toll-middle.ogg
    Last-Modified: 2022-12-23T20:46:19Z
    Format: 44100:f:2
    Artist: John J. Franeuy
    Title: Funeral Toll middle
    Time: 9
    duration: 8.571
    file: wedding-peal-beginning.ogg
    Last-Modified: 2022-12-23T20:46:19Z
    Format: 44100:f:2
    Artist: John J. Franey
    Date: 2017
    Title: wedding peal, regular, beginning
    Time: 9
    duration: 8.641
    file: wedding-peal-end.ogg
    Last-Modified: 2022-12-23T20:46:19Z
    Format: 44100:f:2
    Artist: John J. Franey
    Date: 2017
    Genre: Electronic
    Title: wedding peal end
    Time: 23
    duration: 23.312
    file: wedding-peal-middle.ogg
    Last-Modified: 2022-12-23T20:46:19Z
    Format: 44100:f:2
    Artist: John J. Franey
    Date: 2017
    Genre: Electronic
    Title: wedding peal middle
    Time: 8
    duration: 8.071
    file: missing-peal-beginning.ogg
    duration: 3.888
    file: missing-peal-middle.ogg
    duration: 8.071
    file: call-to-mass.ogg
    Last-Modified: 2022-12-23T20:46:19Z
    Format: 44100:f:2
    Genre: Instrumental
    Album: Bells of St Veronica
    Title: call to mass
    Date: 2016
    Artist: John J. Franey
    Time: 65
    duration: 65.143
    directory: aaahold
    Last-Modified: 2022-12-23T20:43:45Z
    directory: kc
    Last-Modified: 2019-09-13T00:05:41Z
    playlist: b12
    Last-Modified: 2016-10-07T01:39:16Z
    OK""";
    private final List<String> sampleAsList = Arrays.asList(sample.split("\n"));

    MpdMetadata mpdLsInfo;

    @BeforeEach
    public void init() {
        mpdLsInfo = new MpdMetadata(sampleAsList);
    }

    @Test
    public void testFindExactMatch() {
        // 7
        List<MpdMetadata.Song> match = mpdLsInfo.findExact("call-to-mass.ogg");
        Assertions.assertEquals(1, match.size());
    }

    @Test
    public void testNotExactMatch() {
        // actual file is 'call-to-mass.ogg', so exact match fails
        List<MpdMetadata.Song> match = mpdLsInfo.findExact("call-to-mass");
        Assertions.assertEquals(0, match.size());
    }

    @Test
    public void testMissingExtensionMatch() {
        // actual file is 'call-to-mass.ogg', ignoring the 'ogg' extension gives match.
        List<MpdMetadata.Song> match = mpdLsInfo.findIgnoreExtension("call-to-mass");
        Assertions.assertEquals(1, match.size());
    }

    @Test
    public void testVariableMatch() {
        List<MpdMetadata.Song> match = mpdLsInfo.findByVariableForm("wedding-peal");
        Assertions.assertEquals(3, match.size());

        // should return list in sequence: beginning -> middle -> end
        Assertions.assertEquals("wedding-peal-beginning.ogg", match.get(0).filename());
        Assertions.assertEquals("wedding-peal-middle.ogg", match.get(1).filename());
        Assertions.assertEquals("wedding-peal-end.ogg", match.get(2).filename());
    }

    @Test
    public void testVariableBrokenMissingEnd() {
        List<MpdMetadata.Song> match = mpdLsInfo.findByVariableForm("missing-peal");

        // missing the '-end', so should not match
        Assertions.assertEquals(0, match.size());
    }

    @Test
    public void checkDuration() {
        MpdMetadata.Song match = mpdLsInfo.findExact("call-to-mass.ogg").get(0);
        Assertions.assertEquals(65143L, match.duration() );
    }


}
