package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Handles CRUD operations for Item objects in DSpace-CRIS.
 * This class depends on AuthenticationHandler for managing authentication tokens.
 */
public class ItemHandler {

    private final AuthenticationHandler authenticationHandler;
    private String baseUrl; // Ahora configurable dinámicamente

    /**
     * Constructor para ItemHandler.
     *
     * @param authenticationHandler AuthenticationHandler instance for token management.
     */
    public ItemHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    /**
     * Setter para configurar el baseUrl desde la configuración de Midpoint.
     *
     * @param baseUrl El endpoint base del API REST.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Método para verificar que baseUrl fue inicializado antes de usarlo.
     */
    private void ensureBaseUrlInitialized() {
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalStateException("El baseUrl no está configurado. Por favor inicializa el conector correctamente.");
        }
    }

    /**
     * Crea un nuevo item en DSpace.
     *
     * @param itemData JSON con los detalles del item.
     * @throws IOException Si ocurre un error durante la petición HTTP.
     * @throws ParseException Si ocurre un error al procesar la respuesta.
     */
    public void createItem(JSONObject itemData) throws IOException, ParseException {
        ensureBaseUrlInitialized(); // Verifica que baseUrl esté configurado
        String endpoint = baseUrl + "/server/api/core/items";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(itemData.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            if (statusCode != 201) {
                throw new RuntimeException("Failed to create item. HTTP Status: " + statusCode + ", Response: " + parseResponseBody(response));
            }
        }
    }

    /**
     * Recupera un item por su UUID.
     *
     * @param itemId UUID del item.
     * @return JSON con los detalles del item.
     * @throws IOException Si ocurre un error durante la petición HTTP.
     * @throws ParseException Si ocurre un error al procesar la respuesta.
     */
    public JSONObject getItem(String itemId) throws IOException, ParseException {
        ensureBaseUrlInitialized(); // Verifica que baseUrl esté configurado
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) {
                return new JSONObject(parseResponseBody(response));
            } else {
                throw new RuntimeException("Failed to retrieve item. HTTP Status: " + statusCode + ", Response: " + parseResponseBody(response));
            }
        }
    }

    /**
     * Actualiza un item existente en DSpace.
     *
     * @param itemId   UUID del item.
     * @param itemData JSON con los detalles actualizados.
     * @throws IOException Si ocurre un error durante la petición HTTP.
     * @throws ParseException Si ocurre un error al procesar la respuesta.
     */
    public void updateItem(String itemId, JSONObject itemData) throws IOException, ParseException {
        ensureBaseUrlInitialized(); // Verifica que baseUrl esté configurado
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(itemData.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                throw new RuntimeException("Failed to update item. HTTP Status: " + statusCode + ", Response: " + parseResponseBody(response));
            }
        }
    }

    /**
     * Elimina un item en DSpace.
     *
     * @param itemId UUID del item.
     * @throws IOException Si ocurre un error durante la petición HTTP.
     * @throws ParseException Si ocurre un error al procesar la respuesta.
     */
    public void deleteItem(String itemId) throws IOException, ParseException {
        ensureBaseUrlInitialized(); // Verifica que baseUrl esté configurado
        String endpoint = baseUrl + "/server/api/core/items/" + itemId;
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getCode();
            if (statusCode != 204) {
                throw new RuntimeException("Failed to delete item. HTTP Status: " + statusCode + ", Response: " + parseResponseBody(response));
            }
        }
    }

    /**
     * Método auxiliar para parsear el cuerpo de la respuesta.
     *
     * @param response La respuesta HTTP.
     * @return El cuerpo de la respuesta como String.
     * @throws IOException Si ocurre un error durante el parsing.
     * @throws ParseException Si ocurre un error al procesar la respuesta.
     */
    private String parseResponseBody(CloseableHttpResponse response) throws IOException, ParseException {
        return new String(response.getEntity().getContent().readAllBytes());
    }
}
