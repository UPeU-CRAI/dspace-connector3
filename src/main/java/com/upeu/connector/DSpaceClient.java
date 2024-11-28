package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.util.HttpUtil;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles API communication with DSpace-CRIS.
 */
public class DSpaceClient {

    private static final Logger LOG = LoggerFactory.getLogger(DSpaceClient.class);

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

        // Log the configuration details
        LOG.info("Initializing DSpaceClient with the following configuration:");
        LOG.info("Base URL: {}", config.getBaseUrl());
        LOG.info("Username: {}", config.getUsername());
        LOG.info("Password: [PROTECTED]"); // Nunca imprimas contraseñas en texto plano
        LOG.info("Connect Timeout: {} ms", config.getConnectTimeout());
        LOG.info("Read Timeout: {} ms", config.getReadTimeout());

        // Initialize the HTTP client with timeout settings
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                        .setResponseTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                        .build())
                .build();

        this.httpUtil = new HttpUtil(authManager, httpClient);
        LOG.info("DSpaceClient initialized successfully.");
    }

    /**
     * Searches for resources using a specific endpoint and query.
     *
     * @param endpoint The API endpoint to query.
     * @param query    The query string.
     * @return List of results as JSON objects.
     */
    public List<JSONObject> search(String endpoint, String query) {
        validateEndpoint(endpoint);
        try {
            // Construct the full URL
            String url = buildUrl(endpoint) + "?query=" + query;

            // Log the constructed URL for debugging
            LOG.debug("Executing search on URL: {}", url);

            // Execute the GET request
            String response = httpUtil.get(url);

            // Parse the response into a list of JSON objects
            return new JSONObject(response)
                    .getJSONArray("results")
                    .toList()
                    .stream()
                    .map(obj -> new JSONObject((Map<?, ?>) obj))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Error while performing search on endpoint: " + endpoint, e);
            throw new RuntimeException("Failed to execute search on endpoint: " + endpoint, e);
        }
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
        String baseUrl = config.getBaseUrl();

        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalStateException("Base URL is not configured.");
        }

        // Ensure baseUrl ends with "/"
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        // Remove leading "/" from endpoint if present
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }

        return baseUrl + endpoint;
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
