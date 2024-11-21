package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.EPersonSchema;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Handles CRUD operations for EPerson objects in DSpace-CRIS.
 * Uses EPersonSchema for data transformation.
 */
public class EPersonHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EPersonHandler.class);

    private final Endpoints endpoints;

    public EPersonHandler(AuthenticationHandler authenticationHandler, Endpoints endpoints) {
        super(authenticationHandler);
        this.endpoints = endpoints;
    }

    // =====================================
    // Create EPerson
    // =====================================
    public String createEPerson(EPersonSchema ePersonSchema) throws IOException {
        validateSchema(ePersonSchema);

        String endpoint = endpoints.getEPersonsEndpoint();
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to create EPerson at {}", endpoint);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("EPerson created successfully.");
                return parseResponseBody(response).getString("id"); // Usa parseResponseBody de AbstractHandler
            } else {
                handleErrorResponse(statusCode, "Failed to create EPerson");
                return null; // Unreachable, added for clarity.
            }
        }
    }

    // =====================================
    // Get EPerson
    // =====================================
    public EPersonSchema getEPerson(String ePersonId) throws IOException {
        validateId(ePersonId);

        String endpoint = endpoints.getEPersonByIdEndpoint(ePersonId);
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        LOG.info("Sending request to get EPerson with ID: {}", ePersonId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("EPerson retrieved successfully.");
                return EPersonSchema.fromJson(parseResponseBody(response));
            } else {
                handleErrorResponse(statusCode, "Failed to retrieve EPerson with ID: " + ePersonId);
                return null; // Unreachable, added for clarity.
            }
        }
    }

    // =====================================
    // Update EPerson
    // =====================================
    public void updateEPerson(String ePersonId, EPersonSchema ePersonSchema) throws IOException {
        validateId(ePersonId);
        validateSchema(ePersonSchema);

        String endpoint = endpoints.getEPersonByIdEndpoint(ePersonId);
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to update EPerson with ID: {}", ePersonId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("EPerson updated successfully.");
            } else {
                handleErrorResponse(statusCode, "Failed to update EPerson with ID: " + ePersonId);
            }
        }
    }

    // =====================================
    // Delete EPerson
    // =====================================
    public void deleteEPerson(String ePersonId) throws IOException {
        validateId(ePersonId);

        String endpoint = endpoints.getEPersonByIdEndpoint(ePersonId);
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        LOG.info("Sending request to delete EPerson with ID: {}", ePersonId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("EPerson deleted successfully.");
            } else {
                handleErrorResponse(statusCode, "Failed to delete EPerson with ID: " + ePersonId);
            }
        }
    }

    // ===============================
    // Helper Methods
    // ===============================
    private void validateId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("EPerson ID cannot be null or empty.");
        }
    }

    private void validateSchema(EPersonSchema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("EPerson schema cannot be null.");
        }
    }

    private void handleErrorResponse(int statusCode, String errorMessage) {
        LOG.error("{} - HTTP Status: {}", errorMessage, statusCode);
        throw new RuntimeException(errorMessage + " - HTTP Status: " + statusCode);
    }
}
