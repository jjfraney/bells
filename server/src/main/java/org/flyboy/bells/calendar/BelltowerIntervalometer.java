package org.flyboy.bells.calendar;

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
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

/**
 * Drives the belltower to ring at scheduled times.
 *
 * @author John J. Franey
 */
@ApplicationScoped
public class BelltowerIntervalometer {

    private static final Logger logger = LoggerFactory.getLogger(BelltowerIntervalometer.class);

    @Inject
    BellCalendar bellCalendar;

    @Inject
    Belltower belltower;

    @Inject
    Vertx vertx;

    /**
     * currently scheduled timers
     */
    Map<Long, BellEvent> currentTimers = emptyMap();

    /**
     * timer and handler to refresh the events.
     */
    @Scheduled(every = "${belltower.intervalometer.schedule.refresh.period:2h}")
    void scheduleRing() {
        logger.debug("refreshing events");
        bellCalendar.getEvents().subscribe().with(
                this::scheduleRing,
                failure -> logger.warn("Unable to obtain events " + failure));
    }

    /**
     * cancel current timers and rebuild them using the freshest
     * scheduled bell events
     *
     * @param bellEvents list of bell events for the rebuilt schedule
     */
    void scheduleRing(List<BellEvent> bellEvents) {
        logger.debug("scheduling events, current={}", currentTimers);

        // cancel existing timers
        currentTimers.keySet().forEach(id -> vertx.cancelTimer(id));
        currentTimers = emptyMap();

        ZonedDateTime now = ZonedDateTime.now();
        currentTimers = bellEvents.stream()
                // ignore events of the past, if any
                .filter(e -> e.getTime().isAfter(now))

                // schedule event
                .map(e -> new Ticket(scheduleRing(now, e), e))

                // collect a map of timer id -> bell event
                .collect(Collectors.toMap(t -> t.timerId, t -> t.bellEvent));
        logger.info("current Timers={}", currentTimers);
    }

    /**
     * set a vertx timer to ring the bell at some offset from reference time.
     * The reference time can be equal to now.
     *
     * @param reference zoned date time
     * @param bellEvent names the bell sample to ring
     * @return the id of the scheduled timer
     */
    private long scheduleRing(ZonedDateTime reference, BellEvent bellEvent) {
        logger.debug("scheduling ring: bellEvent={}", bellEvent);
        return vertx.setTimer(reference.until(bellEvent.getTime(), ChronoUnit.MILLIS), this::ring);
    }

    /**
     * request the belltower to ring the bells.
     *
     * @param timerId of the expired timer
     */
    void ring(Long timerId) {

        logger.debug("timer fired: {}", timerId);

        if (currentTimers.containsKey(timerId)) {

            BellEvent bellEvent = currentTimers.get(timerId);

            logger.debug("timer fired: timerId={}, bellEvent={}", timerId, bellEvent);

            belltower.ring(bellEvent.getTitle())
                    .subscribe().with(
                            r -> logger.info(r.toString()),
                            f -> logger.error(f.getMessage())
                    );

            currentTimers.remove(timerId);
        }
    }

    /**
     * tuple to hold the timerId of the timer
     * scheduled to fire for a given bellEvent
     *
     * @param timerId   of a timer
     * @param bellEvent the timer handler will play
     */
    private record Ticket(long timerId, BellEvent bellEvent) {
    }

}
