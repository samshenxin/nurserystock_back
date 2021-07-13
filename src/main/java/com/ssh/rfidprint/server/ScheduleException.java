package com.ssh.rfidprint.server;


/**
 * 自定义异常
 * 
 * Created by liyd on 12/19/14.
 */
public class ScheduleException extends Exception {

    /** serialVersionUID */
    private static final long serialVersionUID = -1921648378954132894L;

    /**
     * Constructor
     */
    public ScheduleException() {
        super();
    }

    /**
     * Instantiates a new ScheduleException.
     *
     * @param e the e
     */
    public ScheduleException(Throwable e) {
        super(e);
    }

    /**
     * Constructor
     *
     * @param message the message
     */
    public ScheduleException(String message) {
        super(message);
    }

}
