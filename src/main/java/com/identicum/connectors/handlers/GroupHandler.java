package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.GroupSchema;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Handles CRUD operations for Group objects in DSpace-CRIS.
 * Uses GroupSchema for data transformation.
 */
public class GroupHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GroupHandler.class);

    private final Endpoints endpoints;

    public GroupHandler(AuthenticationHandler authenticationHandler, Endpoints endpoints) {
        super(authenticationHandler);
        this.endpoints = endpoints;
    }

    // =====================================
    // Create Group
    // =====================================
    public String createGroup(GroupSchema groupSchema) throws IOException {
        validateSchema(groupSchema);

        String endpoint = endpoints.getGroupsEndpoint();
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setEntity(new StringEntity(groupSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to create group at {}", endpoint);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("Group created successfully.");
                return parseResponseBody(response).getString("id"); // Usa el método heredado de AbstractHandler
            } else {
                handleErrorResponse(statusCode, "Failed to create group");
                return null; // Unreachable, added for clarity.
            }
        }
    }

    // =====================================
    // Get Group
    // =====================================
    public GroupSchema getGroup(String groupId) throws IOException {
        validateId(groupId);

        String endpoint = endpoints.getGroupByIdEndpoint(groupId);
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        LOG.info("Sending request to get group with ID: {}", groupId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Group retrieved successfully.");
                return GroupSchema.fromJson(parseResponseBody(response));
            } else {
                handleErrorResponse(statusCode, "Failed to retrieve group with ID: " + groupId);
                return null; // Unreachable, added for clarity.
            }
        }
    }

    // =====================================
    // Update Group
    // =====================================
    public void updateGroup(String groupId, GroupSchema groupSchema) throws IOException {
        validateId(groupId);
        validateSchema(groupSchema);

        String endpoint = endpoints.getGroupByIdEndpoint(groupId);
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setEntity(new StringEntity(groupSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to update group with ID: {}", groupId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Group updated successfully.");
            } else {
                handleErrorResponse(statusCode, "Failed to update group with ID: " + groupId);
            }
        }
    }

    // =====================================
    // Delete Group
    // =====================================
    public void deleteGroup(String groupId) throws IOException {
        validateId(groupId);

        String endpoint = endpoints.getGroupByIdEndpoint(groupId);
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        LOG.info("Sending request to delete group with ID: {}", groupId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("Group deleted successfully.");
            } else {
                handleErrorResponse(statusCode, "Failed to delete group with ID: " + groupId);
            }
        }
    }

    // ===============================
    // Helper Methods
    // ===============================
    private void validateId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Group ID cannot be null or empty.");
        }
    }

    private void validateSchema(GroupSchema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("Group schema cannot be null.");
        }
    }

    private void handleErrorResponse(int statusCode, String errorMessage) {
        LOG.error("{} - HTTP Status: {}", errorMessage, statusCode);
        throw new RuntimeException(errorMessage + " - HTTP Status: " + statusCode);
    }
}
