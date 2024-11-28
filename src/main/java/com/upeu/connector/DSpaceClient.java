package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.util.HttpUtil;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles API communication with DSpace-CRIS.
 */
public class DSpaceClient {

    private final DSpaceConfiguration config; // Client configuration
    private final HttpUtil httpUtil;         // Utility for handling HTTP requests

    /**
     * Constructor for DSpaceClient.
     *
     * @param config      Configuration for the DSpace client.
     * @param authManager AuthManager instance for handling authentication.
     */
    public DSpaceClient(DSpaceConfiguration config, AuthManager authManager) {
        // Validate inputs
        if (config == null) {
            throw new IllegalArgumentException("La configuración no puede ser nula.");
        }
        if (authManager == null) {
            throw new IllegalArgumentException("AuthManager no puede ser nulo.");
        }

        this.config = config;

        // Initialize the HTTP client with timeout settings
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                        .setResponseTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                        .build())
                .build();

        this.httpUtil = new HttpUtil(authManager, httpClient);
    }

    public List<JSONObject> search(String endpoint, String query) {
        // Construye la URL con el endpoint y el query
        String url = endpoint + "?query=" + query;

        // Realiza la solicitud GET
        String response = httpUtil.get(url);

        // Convierte la respuesta JSON a una lista de objetos
        return new JSONObject(response).getJSONArray("results").toList()
                .stream()
                .map(obj -> new JSONObject((Map<?, ?>) obj))
                .collect(Collectors.toList());
    }

    /**
     * Performs a GET request to the specified endpoint.
     *
     * @param endpoint The relative endpoint (e.g., "/epersons").
     * @return Response as a JSON-formatted string.
     * @throws Exception If an error occurs during the request.
     */
    public String get(String endpoint) throws Exception {
        validateEndpoint(endpoint);
        return httpUtil.get(buildUrl(endpoint));
    }

    /**
     * Performs a POST request to the specified endpoint.
     *
     * @param endpoint The relative endpoint (e.g., "/epersons").
     * @param body     The JSON body of the request.
     * @return Response as a JSON-formatted string.
     * @throws Exception If an error occurs during the request.
     */
    public String post(String endpoint, String body) throws Exception {
        validateEndpoint(endpoint);
        validateBody(body);
        return httpUtil.post(buildUrl(endpoint), body);
    }

    /**
     * Performs a PUT request to the specified endpoint.
     *
     * @param endpoint The relative endpoint (e.g., "/epersons/{id}").
     * @param body     The JSON body of the request.
     * @return Response as a JSON-formatted string.
     * @throws Exception If an error occurs during the request.
     */
    public String put(String endpoint, String body) throws Exception {
        validateEndpoint(endpoint);
        validateBody(body);
        return httpUtil.put(buildUrl(endpoint), body);
    }

    /**
     * Performs a DELETE request to the specified endpoint.
     *
     * @param endpoint The relative endpoint (e.g., "/epersons/{id}").
     * @throws Exception If an error occurs during the request.
     */
    public void delete(String endpoint) throws Exception {
        validateEndpoint(endpoint);
        httpUtil.delete(buildUrl(endpoint));
    }

    /**
     * Constructs the full URL by combining the base URL and the relative endpoint.
     *
     * @param endpoint The relative endpoint.
     * @return The full URL as a string.
     */
    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("/")) {
            return config.getBaseUrl().endsWith("/") ?
                    config.getBaseUrl() + endpoint.substring(1) :
                    config.getBaseUrl() + endpoint;
        }
        return config.getBaseUrl().endsWith("/") ?
                config.getBaseUrl() + endpoint :
                config.getBaseUrl() + "/" + endpoint;
    }

    /**
     * Validates that the endpoint is not null or empty.
     *
     * @param endpoint The endpoint to validate.
     */
    private void validateEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("El endpoint no puede ser nulo ni vacío.");
        }
    }

    /**
     * Validates that the request body is not null or empty.
     *
     * @param body The request body to validate.
     */
    private void validateBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("El cuerpo de la solicitud no puede ser nulo ni vacío.");
        }
    }
}
