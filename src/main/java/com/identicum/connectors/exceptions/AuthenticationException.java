package com.identicum.connectors.exceptions;

/**
 * Custom exception for handling authentication errors in the DSpace-CRIS connector.
 * This exception is used when authentication fails, such as invalid credentials,
 * token expiration, or other authentication-related issues.
 */
public class AuthenticationException extends RuntimeException {

    /**
     * Constructs a new AuthenticationException with the specified detail message.
     *
     * @param message The detail message to describe the exception.
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthenticationException with the specified detail message and cause.
     *
     * @param message The detail message to describe the exception.
     * @param cause   The cause of the exception (a throwable that caused this exception).
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AuthenticationException with the specified cause.
     *
     * @param cause The cause of the exception (a throwable that caused this exception).
     */
    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
