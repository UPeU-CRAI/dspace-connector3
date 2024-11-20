package com.identicum.connectors.exceptions;

/**
 * Custom exception for handling errors during the creation of handlers.
 */
public class HandlerCreationException extends RuntimeException {

    /**
     * Constructs a new HandlerCreationException with the specified detail message.
     *
     * @param message the detail message
     */
    public HandlerCreationException(String message) {
        super(message);
    }

    /**
     * Constructs a new HandlerCreationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public HandlerCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
