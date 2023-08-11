package org.flyboy.belltower.belfry;

/**
 * @author John J. Franey
 */
public class BellPatternNotFoundException extends BelfryException {
    private final String pattern;

    BellPatternNotFoundException(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getMessage() {
        return "\"" + pattern + "\"" + " not found.";
    }
}
