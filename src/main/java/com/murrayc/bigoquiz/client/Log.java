package com.murrayc.bigoquiz.client;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by murrayc on 1/20/16.
 */
public class Log {
    private static final Logger logger = Logger.getLogger("");

    public static void error(final String message) {
        logger.log(Level.WARNING, message);
    }

    public static void error(final String message, final Throwable e) {
        logger.log(Level.WARNING, message, e);
    }

    public static void fatal(final String message) {
        logger.log(Level.SEVERE, message);
    }

    public static void fatal(final String message, final Throwable e) {
        logger.log(Level.SEVERE, message, e);
    }
}
