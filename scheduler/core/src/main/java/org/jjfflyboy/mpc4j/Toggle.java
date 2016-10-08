package org.jjfflyboy.mpc4j;

/**
 * @author jfraney
 */
public enum Toggle {
    ON, OFF;

    public static Toggle decode(String value) {
        if("0".equals(value)) {
            return OFF;
        } else if("1".equals(value)) {
            return ON;
        } else {
            throw new RuntimeException("Unknown value: " + value);
        }
    }
}
