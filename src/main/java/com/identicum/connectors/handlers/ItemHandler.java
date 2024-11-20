package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.ItemSchema;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Handles CRUD operations for Item objects in DSpace-CRIS.
 * Uses ItemSchema for data transformation.
 */
public class ItemHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ItemHandler.class);

    public ItemHandler(AuthenticationHandler authenticationHandler) {
        super(authenticationHandler);
    }

    // =====================================
    // Create Item
    // =====================================
    public String createItem(ItemSchema item) throws IOException {
        validateSchema(item);

        String endpoint = buildEndpoint(Endpoints.ITEMS);
        HttpPost request = new HttpPost(endpoint);
        request.setEntity(new StringEntity(item.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to create item at {}", endpoint);
        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("Item created successfully.");
                return parseResponseBody(response).getString("id");
            } else {
                LOG.error("Failed to create item. HTTP Status: {}", statusCode);
                throw new RuntimeException("Failed to create item. HTTP Status: " + statusCode);
            }
        } catch (ParseException | URISyntaxException e) {
            LOG.error("Error parsing response during item creation", e);
            throw new RuntimeException("Error parsing response during item creation: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Get Item
    // =====================================
    public ItemSchema getItem(String itemId) throws IOException {
        validateId(itemId);

        String endpoint = buildEndpoint(String.format(Endpoints.ITEM_BY_ID, itemId));
        HttpGet request = new HttpGet(endpoint);

        LOG.info("Sending request to get item with ID: {}", itemId);
        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("Item retrieved successfully.");
                return ItemSchema.fromJson(parseResponseBody(response));
            } else {
                LOG.error("Failed to retrieve item with ID: {}. HTTP Status: {}", itemId, statusCode);
                throw new RuntimeException("Failed to retrieve item with ID: " + itemId + ". HTTP Status: " + statusCode);
            }
        } catch (ParseException e) {
            LOG.error("Error parsing response during item retrieval", e);
            throw new RuntimeException("Error parsing response during item retrieval: " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // =====================================
    // Update Item
    // =====================================
    public void updateItem(String itemId, ItemSchema item) throws IOException, URISyntaxException {
        validateId(itemId);
        validateSchema(item);

        String endpoint = buildEndpoint(String.format(Endpoints.ITEM_BY_ID, itemId));
        HttpPut request = new HttpPut(endpoint);
        request.setEntity(new StringEntity(item.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Sending request to update item with ID: {}", itemId);
        try (CloseableHttpResponse response = executeRequest(request)) {
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

        String endpoint = buildEndpoint(String.format(Endpoints.ITEM_BY_ID, itemId));
        HttpDelete request = new HttpDelete(endpoint);

        LOG.info("Sending request to delete item with ID: {}", itemId);
        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("Item deleted successfully.");
            } else {
                LOG.error("Failed to delete item with ID: {}. HTTP Status: {}", itemId, statusCode);
                throw new RuntimeException("Failed to delete item with ID: " + itemId + ". HTTP Status: " + statusCode);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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

    private String buildEndpoint(String relativePath) {
        return baseUrl + relativePath;
    }
}
