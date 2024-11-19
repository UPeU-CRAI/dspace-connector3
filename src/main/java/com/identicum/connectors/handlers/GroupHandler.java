package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Handles CRUD operations for Group objects in DSpace-CRIS.
 * This class depends on AuthenticationHandler for managing authentication tokens.
 */
public class GroupHandler {

    private final AuthenticationHandler authenticationHandler;
    private final String baseUrl;

    /**
     * Constructor for GroupHandler.
     *
     * @param authenticationHandler AuthenticationHandler instance for token management.
     * @param baseUrl               Base URL of the DSpace-CRIS API.
     */
    public GroupHandler(AuthenticationHandler authenticationHandler, String baseUrl) {
        this.authenticationHandler = authenticationHandler;
        this.baseUrl = baseUrl;
    }

    /**
     * Creates a new group in DSpace.
     *
     * @param groupData JSON object with group details.
     * @throws IOException If an error occurs during the HTTP request.
     */
    public void createGroup(JSONObject groupData) throws IOException {
        String endpoint = baseUrl + "/server/api/eperson/groups";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(groupData.toString(), ContentType.APPLICATION_JSON));

        try (var response = authenticationHandler.getHttpClient().execute(request)) {
            if (response.getCode() != 201) {
                throw new RuntimeException("Failed to create group. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Retrieves a group by its UUID.
     *
     * @param groupId UUID of the group to retrieve.
     * @return JSON object with group details.
     * @throws IOException If an error occurs during the HTTP request.
     */
    public JSONObject getGroup(String groupId) throws IOException {
        String endpoint = baseUrl + "/server/api/eperson/groups/" + groupId;
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (var response = authenticationHandler.getHttpClient().execute(request)) {
            if (response.getCode() == 200) {
                return new JSONObject(new String(response.getEntity().getContent().readAllBytes()));
            } else {
                throw new RuntimeException("Failed to retrieve group. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Updates an existing group in DSpace.
     *
     * @param groupId   UUID of the group to update.
     * @param groupData JSON object with updated group details.
     * @throws IOException If an error occurs during the HTTP request.
     */
    public void updateGroup(String groupId, JSONObject groupData) throws IOException {
        String endpoint = baseUrl + "/server/api/eperson/groups/" + groupId;
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(groupData.toString(), ContentType.APPLICATION_JSON));

        try (var response = authenticationHandler.getHttpClient().execute(request)) {
            if (response.getCode() != 200) {
                throw new RuntimeException("Failed to update group. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Deletes a group in DSpace.
     *
     * @param groupId UUID of the group to delete.
     * @throws IOException If an error occurs during the HTTP request.
     */
    public void deleteGroup(String groupId) throws IOException {
        String endpoint = baseUrl + "/server/api/eperson/groups/" + groupId;
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (var response = authenticationHandler.getHttpClient().execute(request)) {
            if (response.getCode() != 204) {
                throw new RuntimeException("Failed to delete group. HTTP Status: " + response.getCode());
            }
        }
    }
}
