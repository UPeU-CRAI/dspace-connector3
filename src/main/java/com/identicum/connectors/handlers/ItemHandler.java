package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Handles CRUD operations for Item objects in DSpace-CRIS.
 * This class depends on AuthenticationHandler for managing authentication tokens.
 */
public class ItemHandler {

    private final AuthenticationHandler authenticationHandler;
    private final String baseUrl;

    /**
     * Constructor for ItemHandler.
     *
     * @param authenticationHandler AuthenticationHandler instance for token management.
     * @param baseUrl               Base URL of the DSpace-CRIS API.
     */
    public ItemHandler(AuthenticationHandler authenticationHandler, String baseUrl) {
        this.authenticationHandler = authenticationHandler;
        this.baseUrl = baseUrl;
    }

    /**
     * Creates a new item in DSpace.
     *
     * @param itemData JSON object with item details.
     * @throws IOException If an error occurs during the HTTP request.
     */
    public void createItem(JSONObject itemData) throws IOException {
        String endpoint = baseUrl + "/server/api/core/items";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(itemData.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode != 201) {
                throw new RuntimeException("Failed to create item. HTTP Status: " + statusCode + ", Response: " + parseResponseBody(response));
            }
        }
    }

    /**
     * Retrieves an item by its UUID.
     *
     * @param itemId UUID of the item to retrieve.
     * @return JSON object with item details.
     * @throws IOException If an error occurs during the HTTP request.
     */
    public JSONObject getItem(String itemId) throws IOException {
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) {
                return new JSONObject(parseResponseBody(response));
            } else {
                throw new RuntimeException("Failed to retrieve item. HTTP Status: " + statusCode + ", Response: " + parseResponseBody(response));
            }
        }
    }

    /**
     * Updates an existing item in DSpace.
     *
     * @param itemId   UUID of the item to update.
     * @param itemData JSON object with updated item details.
     * @throws IOException If an error occurs during the HTTP request.
     */
    public void updateItem(String itemId, JSONObject itemData) throws IOException {
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(itemData.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed to update item. HTTP Status: " + statusCode + ", Response: " + parseResponseBody(response));
            }
        }
    }

    /**
     * Deletes an item in DSpace.
     *
     * @param itemId UUID of the item to delete.
     * @throws IOException If an error occurs during the HTTP request.
     */
    public void deleteItem(String itemId) throws IOException {
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode != 204) {
                throw new RuntimeException("Failed to delete item. HTTP Status: " + statusCode + ", Response: " + parseResponseBody(response));
            }
        }
    }

    /**
     * Helper Method: Parse Response Body
     *
     * @param response The HTTP response.
     * @return Response body as a String.
     * @throws IOException If an error occurs during response parsing.
     */
    private String parseResponseBody(CloseableHttpResponse response) throws IOException {
        return new String(response.getEntity().getContent().readAllBytes());
    }
}
