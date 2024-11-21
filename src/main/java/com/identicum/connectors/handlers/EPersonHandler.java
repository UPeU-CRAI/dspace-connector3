package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.EPersonSchema;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
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
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to create EPerson at {}", endpoint);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("EPerson created successfully.");
                return parseResponseBody(response).getString("id");
            } else {
                LOG.error("Failed to create EPerson. HTTP Status: {}", statusCode);
                throw new RuntimeException("Failed to create EPerson. HTTP Status: " + statusCode);
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

        LOG.info("Sending request to get EPerson with ID: {}", ePersonId);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("EPerson retrieved successfully.");
                return EPersonSchema.fromJson(parseResponseBody(response));
            } else {
                LOG.error("Failed to retrieve EPerson with ID: {}. HTTP Status: {}", ePersonId, statusCode);
                throw new RuntimeException("Failed to retrieve EPerson with ID: " + ePersonId + ". HTTP Status: " + statusCode);
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
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to update EPerson with ID: {}", ePersonId);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("EPerson updated successfully.");
            } else {
                LOG.error("Failed to update EPerson with ID: {}. HTTP Status: {}", ePersonId, statusCode);
                throw new RuntimeException("Failed to update EPerson with ID: " + ePersonId + ". HTTP Status: " + statusCode);
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

        LOG.info("Sending request to delete EPerson with ID: {}", ePersonId);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("EPerson deleted successfully.");
            } else {
                LOG.error("Failed to delete EPerson with ID: {}. HTTP Status: {}", ePersonId, statusCode);
                throw new RuntimeException("Failed to delete EPerson with ID: " + ePersonId + ". HTTP Status: " + statusCode);
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

    private JSONObject parseResponseBody(CloseableHttpResponse response) throws IOException {
        String responseBody = new String(response.getEntity().getContent().readAllBytes());
        return new JSONObject(responseBody);
    }
}
