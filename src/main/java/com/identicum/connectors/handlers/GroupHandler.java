package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.GroupSchema;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.IOException;

/**
 * Handler for managing Group operations in DSpace.
 * Supports creation, retrieval, updating, and deletion of groups using GroupSchema.
 */
public class GroupHandler extends AbstractHandler {

    public GroupHandler(AuthenticationHandler authenticationHandler) {
        super(authenticationHandler);
    }

    public String createGroup(GroupSchema groupSchema) throws IOException {
        String endpoint = baseUrl + Endpoints.GROUPS;
        HttpPost request = new HttpPost(endpoint);
        request.setEntity(new StringEntity(groupSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                return parseResponseBody(response).getString("id");
            } else {
                throw new RuntimeException("Failed to create group. HTTP Status: " + statusCode);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing response during Group creation: " + e.getMessage(), e);
        }
    }

    public GroupSchema getGroup(String groupId) throws IOException {
        String endpoint = baseUrl + String.format(Endpoints.GROUP_BY_ID, groupId);
        HttpGet request = new HttpGet(endpoint);

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                return GroupSchema.fromJson(parseResponseBody(response));
            } else {
                throw new RuntimeException("Failed to retrieve group with ID: " + groupId + ". HTTP Status: " + statusCode);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing response during Group retrieval: " + e.getMessage(), e);
        }
    }

    public void updateGroup(String groupId, GroupSchema groupSchema) throws IOException {
        String endpoint = baseUrl + String.format(Endpoints.GROUP_BY_ID, groupId);
        HttpPut request = new HttpPut(endpoint);
        request.setEntity(new StringEntity(groupSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode != 200) { // OK
                throw new RuntimeException("Failed to update group with ID: " + groupId + ". HTTP Status: " + statusCode);
            }
        }
    }

    public void deleteGroup(String groupId) throws IOException {
        String endpoint = baseUrl + String.format(Endpoints.GROUP_BY_ID, groupId);
        HttpDelete request = new HttpDelete(endpoint);

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode != 204) { // No Content
                throw new RuntimeException("Failed to delete group with ID: " + groupId + ". HTTP Status: " + statusCode);
            }
        }
    }
}
