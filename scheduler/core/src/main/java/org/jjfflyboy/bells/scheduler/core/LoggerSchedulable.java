package org.jjfflyboy.bells.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author jfraney
 */
public class LoggerSchedulable extends AbstractOneShotSchedulable {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerSchedulable.class);

    public LoggerSchedulable() {
        super(LocalDateTime.now().plus(Duration.ofMinutes(5)));
    }

    @Override
    public Callable<Void> getCallable() {
        return () -> {
            LOGGER.info("running the callable.  firetime={}, now={}",
                    getFireTime(), LocalDateTime.now());
            return null;
        };
    }

}
