package org.flyboy.bells.tower;

/**
 * @author John J. Franey
 */
public class BelltowerSampleNotFoundException extends BelltowerException {
    private final String sampleName;

    BelltowerSampleNotFoundException(String sampleName) {
        this.sampleName = sampleName;
    }

    @Override
    public String getMessage() {
        return "\"" + sampleName + "\"" + " not found.";
    }
}
