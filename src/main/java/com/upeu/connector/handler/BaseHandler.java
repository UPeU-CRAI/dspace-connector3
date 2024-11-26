package com.upeu.connector.handler;

import com.upeu.connector.util.ValidationUtil;
import org.json.JSONObject;
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
     * Validates the structure of a JSON response.
     *
     * @param jsonResponse The JSON object to validate.
     * @param requiredFields The required fields that must exist in the JSON.
     * @throws RuntimeException if any required field is missing.
     */
    protected void validateJsonResponse(JSONObject jsonResponse, String... requiredFields) {
        for (String field : requiredFields) {
            if (!jsonResponse.has(field)) {
                String errorMessage = "Missing required field: " + field;
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        }
    }

    protected void validateId(String id, String message) {
        ValidationUtil.validateId(id, message);
    }

    /**
     * Handles API exceptions in a consistent manner.
     *
     * @param message The error message to log.
     * @param e The exception causing the error.
     * @throws RuntimeException with the provided message and exception details.
     */
    protected void handleApiException(String message, Exception e) {
        logger.error(message, e);
        throw new RuntimeException(message, e);
    }

    /**
     * Constructs an endpoint URL with optional query parameters.
     *
     * @param baseEndpoint The base endpoint URL.
     * @param queryParams  The query parameters to append.
     * @return The constructed endpoint URL.
     */
    protected String constructEndpointWithParams(String baseEndpoint, String queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return baseEndpoint;
        }
        return baseEndpoint + "?" + queryParams;
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
