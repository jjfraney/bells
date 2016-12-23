package org.jjfflyboy.bells.scheduler.core;

import java.time.Duration;

/**
 * @author jfraney
 */
public interface Settings {
    String getMpdHost();
    Integer getMpdPort();

    /**
     * @return time between calendar queries
     */
    Duration getCalendarQueryPeriod();

    /**
     * @return time into future to query events
     */
    Duration getCalendarQueryLookAhead();

    /**
     * call-to-mass bells will ring some time before mass starts.
     * @return time before mass to ring call-to-mass bells.
     */
    Duration getCallToMassDuration();

    String getCalendarId();

    Duration getDebugPlayPeriod();

    Boolean isDebug();
}
