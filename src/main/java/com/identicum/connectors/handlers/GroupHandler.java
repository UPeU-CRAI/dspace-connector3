package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.GroupSchema;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Handler for managing Group operations in DSpace.
 * Supports creation, retrieval, updating, and deletion of groups using GroupSchema.
 */
public class GroupHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GroupHandler.class);

    private final Endpoints endpoints;

    public GroupHandler(AuthenticationHandler authenticationHandler, Endpoints endpoints) {
        super(authenticationHandler);
        this.endpoints = endpoints;
    }

    public String createGroup(GroupSchema groupSchema) throws IOException {
        validateSchema(groupSchema);

        String endpoint = endpoints.getGroupsUrl();
        HttpPost request = new HttpPost(endpoint);
        request.setEntity(new StringEntity(groupSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to create group at {}", endpoint);
        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("Group created successfully.");
                return parseResponseBody(response).getString("id");
            } else {
                LOG.error("Failed to create group. HTTP Status: {}", statusCode);
                throw new RuntimeException("Failed to create group. HTTP Status: " + statusCode);
            }
        } catch (ParseException e) {
            LOG.error("Error parsing response during Group creation", e);
            throw new RuntimeException("Error parsing response during Group creation: " + e.getMessage(), e);
        }
    }

    public GroupSchema getGroup(String groupId) throws IOException {
        validateId(groupId);

        String endpoint = endpoints.getGroupByIdUrl(groupId);
        HttpGet request = new HttpGet(endpoint);

        LOG.info("Sending request to get group with ID: {}", groupId);
        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Group retrieved successfully.");
                return GroupSchema.fromJson(parseResponseBody(response));
            } else {
                LOG.error("Failed to retrieve group with ID: {}. HTTP Status: {}", groupId, statusCode);
                throw new RuntimeException("Failed to retrieve group with ID: " + groupId + ". HTTP Status: " + statusCode);
            }
        } catch (ParseException e) {
            LOG.error("Error parsing response during Group retrieval", e);
            throw new RuntimeException("Error parsing response during Group retrieval: " + e.getMessage(), e);
        }
    }

    public void updateGroup(String groupId, GroupSchema groupSchema) throws IOException {
        validateId(groupId);
        validateSchema(groupSchema);

        String endpoint = endpoints.getGroupByIdUrl(groupId);
        HttpPut request = new HttpPut(endpoint);
        request.setEntity(new StringEntity(groupSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to update group with ID: {}", groupId);
        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Group updated successfully.");
            } else {
                LOG.error("Failed to update group with ID: {}. HTTP Status: {}", groupId, statusCode);
                throw new RuntimeException("Failed to update group with ID: " + groupId + ". HTTP Status: " + statusCode);
            }
        }
    }

    public void deleteGroup(String groupId) throws IOException {
        validateId(groupId);

        String endpoint = endpoints.getGroupByIdUrl(groupId);
        HttpDelete request = new HttpDelete(endpoint);

        LOG.info("Sending request to delete group with ID: {}", groupId);
        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("Group deleted successfully.");
            } else {
                LOG.error("Failed to delete group with ID: {}. HTTP Status: {}", groupId, statusCode);
                throw new RuntimeException("Failed to delete group with ID: " + groupId + ". HTTP Status: " + statusCode);
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
}
