package org.flyboy.bells.tower;

/**
 * @author John J. Franey
 */
public class BelltowerPatternNotFoundException extends BelltowerException {
    private final String pattern;

    BelltowerPatternNotFoundException(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getMessage() {
        return "\"" + pattern + "\"" + " not found.";
    }
}
