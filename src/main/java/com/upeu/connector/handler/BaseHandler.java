package com.upeu.connector.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.upeu.connector.DSpaceClient;

/**
 * Base class for handling entities in the DSpace-CRIS system.
 * Provides common functionality for all entity handlers.
 */
public abstract class BaseHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DSpaceClient dSpaceClient;

    /**
     * Constructor for BaseHandler.
     *
     * @param dSpaceClient The DSpaceClient instance for making API requests.
     */
    protected BaseHandler(DSpaceClient dSpaceClient) {
        this.dSpaceClient = dSpaceClient;
    }

    /**
     * Provides access to the underlying DSpaceClient.
     *
     * @return The DSpaceClient instance.
     */
    public DSpaceClient getClient() {
        return dSpaceClient;
    }

    /**
     * Utility method to log API errors.
     *
     * @param message The error message.
     * @param e The exception causing the error.
     */
    protected void logError(String message, Exception e) {
        logger.error(message, e);
    }

    /**
     * Abstract method to be implemented by specific handlers for validation.
     *
     * @param entity The entity to validate.
     * @return true if the entity is valid, false otherwise.
     */
    protected abstract boolean validate(Object entity);
}
