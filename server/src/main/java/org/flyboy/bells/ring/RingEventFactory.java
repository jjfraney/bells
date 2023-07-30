package org.flyboy.bells.ring;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * @author John J. Franey
 */
@ApplicationScoped
public class RingEventFactory {

    @ConfigProperty(name = "belltower.ring.offset.mass", defaultValue = "-PT1M")
    String massOffset;

    private Duration duration;

    private Duration getMassOffset() {
        if (duration == null) {
            duration = Duration.parse(massOffset);
        }
        return duration;
    }


    public RingEvent createFrom(RingRequest request) {


        ZonedDateTime time;
        String title;

        boolean isMass = "mass".equalsIgnoreCase(request.title());
        if (isMass) {
            time = request.dateTime().plus(getMassOffset());
            title = "call-to-mass.ogg";
        } else {
            time = request.dateTime();
            title = request.title();
        }

        return new RingEvent(time, title, request);
    }
}
