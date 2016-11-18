package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BellTower {
    private static final Logger LOGGER = LoggerFactory.getLogger(BellTower.class);

    public static void main(String[] args) throws IOException {
        LOGGER.info("getting calendar");
        Calendar calendar = new CalendarByGoogle();
        calendar.getEvents().stream()
                .forEach(e -> System.out.printf("%s (%s)\n\n", e.getTitle(), e.getTime()));
        return;
    }
}
