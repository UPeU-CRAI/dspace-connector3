package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.ItemSchema;
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
 * Handles CRUD operations for Item objects in DSpace-CRIS.
 * Uses ItemSchema for data transformation.
 */
public class ItemHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ItemHandler.class);

    private final Endpoints endpoints;

    public ItemHandler(AuthenticationHandler authenticationHandler, Endpoints endpoints) {
        super(authenticationHandler);
        this.endpoints = endpoints;
    }

    // =====================================
    // Create Item
    // =====================================
    public String createItem(ItemSchema item) throws IOException {
        validateSchema(item);

        String endpoint = endpoints.getItemsEndpoint();
        HttpPost request = new HttpPost(endpoint);
        request.setEntity(new StringEntity(item.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to create item at {}", endpoint);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("Item created successfully.");
                return parseResponseBody(response).getString("id");
            } else {
                LOG.error("Failed to create item. HTTP Status: {}", statusCode);
                throw new RuntimeException("Failed to create item. HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Get Item
    // =====================================
    public ItemSchema getItem(String itemId) throws IOException {
        validateId(itemId);

        String endpoint = endpoints.getItemByIdEndpoint(itemId);
        HttpGet request = new HttpGet(endpoint);

        LOG.info("Sending request to get item with ID: {}", itemId);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Item retrieved successfully.");
                return ItemSchema.fromJson(parseResponseBody(response));
            } else {
                LOG.error("Failed to retrieve item with ID: {}. HTTP Status: {}", itemId, statusCode);
                throw new RuntimeException("Failed to retrieve item with ID: " + itemId + ". HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Update Item
    // =====================================
    public void updateItem(String itemId, ItemSchema item) throws IOException {
        validateId(itemId);
        validateSchema(item);

        String endpoint = endpoints.getItemByIdEndpoint(itemId);
        HttpPut request = new HttpPut(endpoint);
        request.setEntity(new StringEntity(item.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to update item with ID: {}", itemId);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Item updated successfully.");
            } else {
                LOG.error("Failed to update item with ID: {}. HTTP Status: {}", itemId, statusCode);
                throw new RuntimeException("Failed to update item with ID: " + itemId + ". HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Delete Item
    // =====================================
    public void deleteItem(String itemId) throws IOException {
        validateId(itemId);

        String endpoint = endpoints.getItemByIdEndpoint(itemId);
        HttpDelete request = new HttpDelete(endpoint);

        LOG.info("Sending request to delete item with ID: {}", itemId);
        try (CloseableHttpResponse response = sendRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("Item deleted successfully.");
            } else {
                LOG.error("Failed to delete item with ID: {}. HTTP Status: {}", itemId, statusCode);
                throw new RuntimeException("Failed to delete item with ID: " + itemId + ". HTTP Status: " + statusCode);
            }
        }
    }

    // ===============================
    // Helper Methods
    // ===============================
    private void validateId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty.");
        }
    }

    private void validateSchema(ItemSchema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("Item schema cannot be null.");
        }
    }

    private JSONObject parseResponseBody(CloseableHttpResponse response) throws IOException {
        String responseBody = new String(response.getEntity().getContent().readAllBytes());
        return new JSONObject(responseBody);
    }
}
