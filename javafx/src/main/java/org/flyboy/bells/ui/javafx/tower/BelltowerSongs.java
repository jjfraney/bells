package org.flyboy.bells.ui.javafx.tower;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * @author John J. Franey
 */
@Dependent
public class BelltowerSongs {

    List<String> songs = List.of(
            "call-to-mass",
            "wedding-peal",
            "funeral-toll"
    );

    public List<String> getSongs() {
        return songs;
    }
}
