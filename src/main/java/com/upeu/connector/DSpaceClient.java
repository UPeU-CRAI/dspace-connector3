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

    private final DSpaceConfiguration config;
    private final HttpUtil httpUtil;

    /**
     * Constructor for DSpaceClient.
     *
     * @param config      Configuration for the DSpace client.
     * @param authManager AuthManager instance for handling authentication.
     */
    public DSpaceClient(DSpaceConfiguration config, AuthManager authManager) {
        if (config == null) {
            throw new IllegalArgumentException("La configuración no puede ser nula.");
        }
        if (authManager == null) {
            throw new IllegalArgumentException("AuthManager no puede ser nulo.");
        }

        this.config = config;

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                        .setResponseTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                        .build())
                .build();

        this.httpUtil = new HttpUtil(authManager, httpClient);

        LOG.info("Initializing DSpaceClient with configuration:");
        LOG.info("Base URL: {}", config.getBaseUrl());
    }

    /**
     * Searches for resources using a specific endpoint and query.
     *
     * @param endpointKey The endpoint key (e.g., "epersons").
     * @param query       The query string.
     * @return List of results as JSON objects.
     */
    public List<JSONObject> search(String endpointKey, String query) {
        if (endpointKey == null || endpointKey.trim().isEmpty()) {
            throw new IllegalArgumentException("El endpointKey no puede ser nulo ni vacío.");
        }

        String url = config.getBaseUrl() + "/server/api/" + endpointKey;
        if (query != null && !query.isEmpty()) {
            url += "?query=" + query;
        }

        LOG.debug("Executing search on URL: {}", url);

        try {
            String response = httpUtil.get(url);
            return new JSONObject(response)
                    .getJSONArray("results")
                    .toList()
                    .stream()
                    .map(obj -> new JSONObject((Map<?, ?>) obj))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Error while performing search on endpoint: {}", url, e);
            throw new RuntimeException("Failed to execute search on endpoint: " + url, e);
        }
    }

    /**
     * Performs a GET request to the specified endpoint.
     *
     * @param relativePath The relative endpoint path.
     * @return Response as a JSON-formatted string.
     * @throws Exception If an error occurs during the request.
     */
    public String get(String relativePath) throws Exception {
        validateEndpoint(relativePath);
        String url = config.getBaseUrl() + "/server/api/" + relativePath;
        return httpUtil.get(url);
    }

    /**
     * Performs a POST request to the specified endpoint.
     *
     * @param relativePath The relative endpoint path.
     * @param body         The JSON body of the request.
     * @return Response as a JSON-formatted string.
     * @throws Exception If an error occurs during the request.
     */
    public String post(String relativePath, String body) throws Exception {
        validateEndpoint(relativePath);
        validateBody(body);
        String url = config.getBaseUrl() + "/server/api/" + relativePath;
        return httpUtil.post(url, body);
    }

    /**
     * Performs a PUT request to the specified endpoint.
     *
     * @param relativePath The relative endpoint path.
     * @param body         The JSON body of the request.
     * @return Response as a JSON-formatted string.
     * @throws Exception If an error occurs during the request.
     */
    public String put(String relativePath, String body) throws Exception {
        validateEndpoint(relativePath);
        validateBody(body);
        String url = config.getBaseUrl() + "/server/api/" + relativePath;
        return httpUtil.put(url, body);
    }

    /**
     * Performs a DELETE request to the specified endpoint.
     *
     * @param relativePath The relative endpoint path.
     * @throws Exception If an error occurs during the request.
     */
    public void delete(String relativePath) throws Exception {
        validateEndpoint(relativePath);
        String url = config.getBaseUrl() + "/server/api/" + relativePath;
        httpUtil.delete(url);
    }

    /**
     * Validates that the endpoint is not null or empty.
     *
     * @param relativePath The endpoint to validate.
     */
    private void validateEndpoint(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
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
