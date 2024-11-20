package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.ItemSchema;
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
 * Handles CRUD operations for Item objects in DSpace-CRIS.
 * Uses ItemSchema for data transformation.
 */
public class ItemHandler extends AbstractHandler {

    public ItemHandler(AuthenticationHandler authenticationHandler) {
        super(authenticationHandler);
    }

    // =====================================
    // Create Item
    // =====================================
    public void createItem(ItemSchema item) throws IOException {
        String endpoint = baseUrl + Endpoints.ITEMS;
        HttpPost request = new HttpPost(endpoint);
        request.setEntity(new StringEntity(item.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode != 201) { // Created
                throw new RuntimeException("Failed to create item. HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Get Item
    // =====================================
    public ItemSchema getItem(String itemId) throws IOException, ParseException {
        String endpoint = baseUrl + String.format(Endpoints.ITEM_BY_ID, itemId);
        HttpGet request = new HttpGet(endpoint);

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                return ItemSchema.fromJson(parseResponseBody(response));
            } else {
                throw new RuntimeException("Failed to retrieve item with ID: " + itemId + ". HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Update Item
    // =====================================
    public void updateItem(String itemId, ItemSchema item) throws IOException {
        String endpoint = baseUrl + String.format(Endpoints.ITEM_BY_ID, itemId);
        HttpPut request = new HttpPut(endpoint);
        request.setEntity(new StringEntity(item.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode != 200) { // OK
                throw new RuntimeException("Failed to update item with ID: " + itemId + ". HTTP Status: " + statusCode);
            }
        }
    }

    // =====================================
    // Delete Item
    // =====================================
    public void deleteItem(String itemId) throws IOException {
        String endpoint = baseUrl + String.format(Endpoints.ITEM_BY_ID, itemId);
        HttpDelete request = new HttpDelete(endpoint);

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode != 204) { // No Content
                throw new RuntimeException("Failed to delete item with ID: " + itemId + ". HTTP Status: " + statusCode);
            }
        }
    }
}
