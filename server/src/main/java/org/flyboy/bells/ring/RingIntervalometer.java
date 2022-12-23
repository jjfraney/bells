package org.flyboy.bells.ring;

import io.quarkus.scheduler.Scheduled;
import io.vertx.mutiny.core.Vertx;
import org.flyboy.bells.tower.Belltower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

/**
 * Drives the belltower to ring at scheduled times.
 *
 * @author John J. Franey
 */
@ApplicationScoped
public class RingIntervalometer {

    private static final Logger logger = LoggerFactory.getLogger(RingIntervalometer.class);

    @Inject
    RingRequestMultiRepository ringRequestMultiRepository;

    @Inject
    RingEventFactory ringEventFactory;

    @Inject
    Belltower belltower;

    @Inject
    Vertx vertx;

    /**
     * currently scheduled timers
     */
    Map<Long, RingEvent> currentTimers = emptyMap();

    /**
     * timer and handler to refresh the ring requests.
     */
    @Scheduled(every = "${belltower.intervalometer.schedule.refresh.period:2h}")
    void scheduleRingRequests() {
        ringRequestMultiRepository.getRequests().subscribe().with(
                this::scheduleRingRequests,
                failure -> logger.warn("Unable to obtain ring requests " + failure));
    }

    /**
     * cancel current timers and rebuild them using the freshest
     * ring requests
     *
     * @param ringRequests list of {@link RingRequest} for the rebuilt schedule
     */
    void scheduleRingRequests(List<RingRequest> ringRequests) {
        List<RingEvent> events = ringRequests.stream().map(this::asEvent).toList();
        scheduleRingEvents(events);
        logger.info("updated timers={}", currentTimers);
    }

    /**
     * cancel current timers and rebuild them using the
     * these ring events.
     *
     * @param events list of {@link RingEvent} to handle
     */
    void scheduleRingEvents(List<RingEvent> events) {

        // cancel existing timers
        currentTimers.keySet().forEach(id -> vertx.cancelTimer(id));
        currentTimers = emptyMap();

        ZonedDateTime now = ZonedDateTime.now();
        currentTimers = events.stream()

                // ignore requests of the past, if any
                .filter(e -> e.dateTime().isAfter(now))

                // schedule event
                .map(this::schedule)

                // collect a map of timer id -> bell event
                .collect(Collectors.toMap(t -> t.timerId, t -> t.event));
    }

    private RingEvent asEvent(RingRequest request) {
        return ringEventFactory.createFrom(request);
    }

    private Ticket schedule(RingEvent event) {
        long timerId = scheduleRingRequests(event.dateTime());
        return new Ticket(timerId, event);
    }

    /**
     * set a vertx timer to ring the bell at some offset from reference dateTime.
     * The reference dateTime can be equal to now.
     *
     * @param ringTime of to run the ring handler.
     * @return the id of the scheduled timer
     */
    private long scheduleRingRequests(ZonedDateTime ringTime) {
        // wait dateTime in milliseconds
        long waitTime = ZonedDateTime.now().until(ringTime, ChronoUnit.MILLIS);

        // set timer to fire after the wait dateTime, handler to ring.
        return vertx.setTimer(waitTime, this::ring);
    }

    /**
     * request the belltower to ring the bells.
     *
     * @param timerId of the expired timer
     */
    void ring(Long timerId) {


        if (currentTimers.containsKey(timerId)) {

            RingEvent ringEvent = currentTimers.get(timerId);

            logger.debug("timer fired: timerId={}, ringEvent={}", timerId, ringEvent);

            belltower.ring(ringEvent.sampleName())
                    .subscribe().with(
                            r -> logger.info(r.toString()),
                            f -> logger.error(f.getMessage())
                    );

            currentTimers.remove(timerId);
        } else {
            logger.debug("timer fired: {}, no timer found", timerId);
        }
    }

    /**
     * tuple to hold the timerId of the timer
     * scheduled to fire for to handle a {@link RingEvent}
     *
     * @param timerId of a timer
     * @param event handled by this ticket's timer
     */
    private record Ticket(long timerId, RingEvent event) {
        public Ticket {
            Objects.requireNonNull(event);
        }
    }

}
