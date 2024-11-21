package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Handles operations for Metadata Schemas and Fields in DSpace.
 */
public class MetadataSchemaHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataSchemaHandler.class);

    private final Endpoints endpoints;

    public MetadataSchemaHandler(AuthenticationHandler authenticationHandler, Endpoints endpoints) {
        super(authenticationHandler);
        this.endpoints = endpoints;
    }

    // =====================================
    // List Metadata Schemas
    // =====================================
    public JSONArray listMetadataSchemas() throws IOException {
        String endpoint = endpoints.getMetadataSchemasEndpoint();
        HttpGet request = new HttpGet(endpoint);

        LOG.info("Sending request to list metadata schemas.");
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Metadata schemas retrieved successfully.");
                return new JSONArray(parseResponseBody(response).toString());
            } else {
                LOG.error("Failed to retrieve metadata schemas. HTTP Status: {}", statusCode);
                throw new RuntimeException("Failed to retrieve metadata schemas. HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // List Metadata Fields
    // =====================================
    public JSONArray listMetadataFields() throws IOException {
        String endpoint = endpoints.getMetadataFieldsEndpoint();
        HttpGet request = new HttpGet(endpoint);

        LOG.info("Sending request to list metadata fields.");
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Metadata fields retrieved successfully.");
                return new JSONArray(parseResponseBody(response).toString());
            } else {
                LOG.error("Failed to retrieve metadata fields. HTTP Status: {}", statusCode);
                throw new RuntimeException("Failed to retrieve metadata fields. HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Create Metadata Schema
    // =====================================
    public String createMetadataSchema(JSONObject schema) throws IOException {
        validateSchema(schema);

        String endpoint = endpoints.getMetadataSchemasEndpoint();
        HttpPost request = new HttpPost(endpoint);
        request.setEntity(new StringEntity(schema.toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to create metadata schema.");
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("Metadata schema created successfully.");
                return parseResponseBody(response).getString("id");
            } else {
                LOG.error("Failed to create metadata schema. HTTP Status: {}", statusCode);
                throw new RuntimeException("Failed to create metadata schema. HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Update Metadata Schema
    // =====================================
    public void updateMetadataSchema(String schemaId, JSONObject schema) throws IOException {
        validateId(schemaId);
        validateSchema(schema);

        String endpoint = endpoints.getMetadataSchemaByIdEndpoint(schemaId);
        HttpPut request = new HttpPut(endpoint);
        request.setEntity(new StringEntity(schema.toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to update metadata schema with ID: {}", schemaId);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Metadata schema updated successfully.");
            } else {
                LOG.error("Failed to update metadata schema with ID: {}. HTTP Status: {}", schemaId, statusCode);
                throw new RuntimeException("Failed to update metadata schema with ID: " + schemaId + ". HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Delete Metadata Schema
    // =====================================
    public void deleteMetadataSchema(String schemaId) throws IOException {
        validateId(schemaId);

        String endpoint = endpoints.getMetadataSchemaByIdEndpoint(schemaId);
        HttpDelete request = new HttpDelete(endpoint);

        LOG.info("Sending request to delete metadata schema with ID: {}", schemaId);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("Metadata schema deleted successfully.");
            } else {
                LOG.error("Failed to delete metadata schema with ID: {}. HTTP Status: {}", schemaId, statusCode);
                throw new RuntimeException("Failed to delete metadata schema with ID: " + schemaId + ". HTTP Status: " + statusCode);
            }
        }
    }

    // ===============================
    // Helper Methods
    // ===============================
    private void validateId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }
    }

    private void validateSchema(JSONObject schema) {
        if (schema == null || schema.isEmpty()) {
            throw new IllegalArgumentException("Schema cannot be null or empty.");
        }
    }

    @Override
    protected JSONObject parseResponseBody(CloseableHttpResponse response) throws IOException {
        String responseBody = new String(response.getEntity().getContent().readAllBytes());
        return new JSONObject(responseBody); // Convertimos la respuesta en un JSONObject
    }
}
