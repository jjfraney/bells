package org.jjfflyboy.bells.scheduler.core;

/**
 * @author jfraney
 */
public class SchedulerException extends RuntimeException {
    public SchedulerException() {
    }

    public SchedulerException(String s) {
        super(s);
    }

    public SchedulerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SchedulerException(Throwable throwable) {
        super(throwable);
    }

    public SchedulerException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
