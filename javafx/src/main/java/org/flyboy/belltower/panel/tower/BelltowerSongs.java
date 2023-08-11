package org.flyboy.belltower.panel.tower;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * @author John J. Franey
 */
@Dependent
public class BelltowerSongs {

    final List<String> songs = List.of(
            "call-to-mass",
            "wedding-peal",
            "funeral-toll"
    );

    public List<String> getSongs() {
        return songs;
    }
}
