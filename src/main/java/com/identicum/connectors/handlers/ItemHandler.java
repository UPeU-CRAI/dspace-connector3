package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.ItemSchema;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
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
    public String createItem(ItemSchema itemSchema) throws IOException {
        validateSchema(itemSchema);

        String endpoint = endpoints.getItemsEndpoint();
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setEntity(new StringEntity(itemSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to create item at {}", endpoint);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("Item created successfully.");
                return parseResponseBody(response).getString("id"); // Usando método heredado
            } else {
                handleErrorResponse(statusCode, "Failed to create item");
                return null; // No alcanzable, agregado para claridad.
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
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        LOG.info("Sending request to get item with ID: {}", itemId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Item retrieved successfully.");
                return ItemSchema.fromJson(parseResponseBody(response));
            } else {
                handleErrorResponse(statusCode, "Failed to retrieve item with ID: " + itemId);
                return null; // No alcanzable, agregado para claridad.
            }
        }
    }

    // =====================================
    // Update Item
    // =====================================
    public void updateItem(String itemId, ItemSchema itemSchema) throws IOException {
        validateId(itemId);
        validateSchema(itemSchema);

        String endpoint = endpoints.getItemByIdEndpoint(itemId);
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setEntity(new StringEntity(itemSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to update item with ID: {}", itemId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Item updated successfully.");
            } else {
                handleErrorResponse(statusCode, "Failed to update item with ID: " + itemId);
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
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        LOG.info("Sending request to delete item with ID: {}", itemId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("Item deleted successfully.");
            } else {
                handleErrorResponse(statusCode, "Failed to delete item with ID: " + itemId);
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

    private void handleErrorResponse(int statusCode, String errorMessage) {
        LOG.error("{} - HTTP Status: {}", errorMessage, statusCode);
        throw new RuntimeException(errorMessage + " - HTTP Status: " + statusCode);
    }
}
