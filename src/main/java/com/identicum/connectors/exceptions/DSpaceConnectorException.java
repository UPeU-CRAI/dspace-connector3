package com.identicum.connectors.exceptions;

/**
 * Custom exception for handling errors specific to the DSpace-CRIS connector.
 * This exception is used to encapsulate any issues encountered during operations
 * such as authentication, schema handling, or resource synchronization.
 */
public class DSpaceConnectorException extends RuntimeException {

    /**
     * Constructs a new DSpaceConnectorException with the specified detail message.
     *
     * @param message The detail message to describe the exception.
     */
    public DSpaceConnectorException(String message) {
        super(message);
    }

    /**
     * Constructs a new DSpaceConnectorException with the specified detail message and cause.
     *
     * @param message The detail message to describe the exception.
     * @param cause   The cause of the exception (a throwable that caused this exception).
     */
    public DSpaceConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DSpaceConnectorException with the specified cause.
     *
     * @param cause The cause of the exception (a throwable that caused this exception).
     */
    public DSpaceConnectorException(Throwable cause) {
        super(cause);
    }
}
