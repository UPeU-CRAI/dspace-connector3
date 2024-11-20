package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.schemas.ItemSchema;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Handles CRUD operations for Item objects in DSpace-CRIS.
 * Uses ItemSchema for data transformation.
 */
public class ItemHandler {

    private final AuthenticationHandler authenticationHandler;
    private String baseUrl;

    public ItemHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private void ensureBaseUrlInitialized() {
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalStateException("baseUrl not set. Please configure the connector properly.");
        }
    }

    // =====================================
    // Create Item
    // =====================================
    public void createItem(ItemSchema item) throws IOException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/core/items";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(item.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 201) {
                throw new RuntimeException("Failed to create item: " + response.getCode());
            }
        }
    }

    // =====================================
    // Get Item
    // =====================================
    public ItemSchema getItem(String itemId) throws IOException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() == 200) {
                return ItemSchema.fromJson(new JSONObject(response.getEntity().getContent().readAllBytes()));
            } else {
                throw new RuntimeException("Failed to get item: " + response.getCode());
            }
        }
    }

    // =====================================
    // Update Item
    // =====================================
    public void updateItem(String itemId, ItemSchema item) throws IOException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(item.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                throw new RuntimeException("Failed to update item: " + response.getCode());
            }
        }
    }

    // =====================================
    // Delete Item
    // =====================================
    public void deleteItem(String itemId) throws IOException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 204) {
                throw new RuntimeException("Failed to delete item: " + response.getCode());
            }
        }
    }
}
