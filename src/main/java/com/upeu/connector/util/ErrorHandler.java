package com.upeu.connector.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for centralized error handling.
 */
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    /**
     * Logs an error with a custom message and exception.
     *
     * @param message The custom error message.
     * @param exception The exception to log.
     */
    public static void logError(String message, Exception exception) {
        logger.error(message, exception);
    }

    /**
     * Throws a runtime exception with a custom message and logs the error.
     *
     * @param message The error message.
     * @param exception The exception to include in the log.
     * @throws RuntimeException always thrown.
     */
    public static void handleCriticalError(String message, Exception exception) {
        logError(message, exception);
        throw new RuntimeException(message, exception);
    }

    /**
     * Logs a warning with a custom message.
     *
     * @param message The warning message.
     */
    public static void logWarning(String message) {
        logger.warn(message);
    }

    /**
     * Logs an informational message.
     *
     * @param message The informational message.
     */
    public static void logInfo(String message) {
        logger.info(message);
    }
}
