package com.upeu.connector;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * DSpaceClient handles API communication with DSpace-CRIS.
 */
public class DSpaceClient {

    private final DSpaceConfiguration config;
    private final CloseableHttpClient httpClient;

    private String csrfToken;
    private String jwtToken;

    public DSpaceClient(DSpaceConfiguration config) {
        this.config = config;
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                        .setResponseTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                        .build())
                .build();
    }

    /**
     * Authenticate with the DSpace API and retrieve CSRF and JWT tokens.
     */
    public void authenticate() throws Exception {
        HttpPost loginRequest = new HttpPost(config.getBaseUrl() + "/rest/login");
        loginRequest.addHeader("Content-Type", "application/json");

        JSONObject credentials = new JSONObject()
                .put("user", config.getUsername())
                .put("password", config.getPassword());

        loginRequest.setEntity(new StringEntity(credentials.toString(), StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(loginRequest)) {
            validateResponse(response);

            // Extract tokens
            this.csrfToken = getHeader(response, "dspace-xsrf-token");
            this.jwtToken = getHeader(response, "Authorization");

            if (csrfToken == null || jwtToken == null) {
                throw new Exception("Authentication failed: Missing CSRF or JWT token in the response.");
            }
        }
    }

    /**
     * Perform a GET request.
     *
     * @param endpoint API endpoint.
     * @return The response body as a string.
     * @throws Exception if the request fails.
     */
    public String get(String endpoint) throws Exception {
        try {
            HttpGet request = new HttpGet(buildUrl(endpoint));
            addAuthHeaders(request);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                validateResponse(response);
                return parseResponse(response.getEntity());
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Token renovado")) {
                return get(endpoint); // Reintenta tras renovar el token
            }
            throw e; // Propaga otros errores
        }
    }

    /**
     * Perform a POST request.
     *
     * @param endpoint API endpoint.
     * @param body     Request body as a JSON string.
     * @return The response body as a string.
     * @throws Exception if the request fails.
     */
    public String post(String endpoint, String body) throws Exception {
        try {
            HttpPost request = new HttpPost(buildUrl(endpoint));
            addAuthHeaders(request);
            request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                validateResponse(response);
                return parseResponse(response.getEntity());
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Token renovado")) {
                return post(endpoint, body); // Reintenta tras renovar el token
            }
            throw e; // Propaga otros errores
        }
    }


    /**
     * Perform a PUT request.
     *
     * @param endpoint API endpoint.
     * @param body     Request body as a JSON string.
     * @return The response body as a string.
     * @throws Exception if the request fails.
     */
    public String put(String endpoint, String body) throws Exception {
        try {
            HttpPut request = new HttpPut(buildUrl(endpoint));
            addAuthHeaders(request);
            request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                validateResponse(response);
                return parseResponse(response.getEntity());
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Token renovado")) {
                return put(endpoint, body); // Reintenta tras renovar el token
            }
            throw e; // Propaga otros errores
        }
    }

    /**
     * Perform a DELETE request.
     *
     * @param endpoint API endpoint.
     * @throws Exception if the request fails.
     */
    public void delete(String endpoint) throws Exception {
        try {
            HttpDelete request = new HttpDelete(buildUrl(endpoint));
            addAuthHeaders(request);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                validateResponse(response);
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Token renovado")) {
                delete(endpoint); // Reintenta tras renovar el token
            } else {
                throw e; // Propaga otros errores
            }
        }
    }

    /**
     * Adds authentication headers to a request.
     *
     * @param request The HTTP request.
     */
    private void addAuthHeaders(HttpUriRequestBase request) throws Exception {
        if (csrfToken == null || jwtToken == null) {
            authenticate(); // Renueva los tokens si están ausentes
        }
        request.addHeader("dspace-xsrf-token", csrfToken);
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("Content-Type", "application/json");
    }

    /**
     * Validates an HTTP response for errors.
     *
     * @param response The HTTP response.
     * @throws Exception if the response indicates an error.
     */
    private void validateResponse(CloseableHttpResponse response) throws Exception {
        if (response.getCode() == 401 || response.getCode() == 403) {
            // Si hay un error de autorización, renueva el token y reintenta
            authenticate();
            throw new RuntimeException("Token renovado, repite la solicitud.");
        } else if (response.getCode() >= 400) {
            String responseBody = parseResponse(response.getEntity());
            throw new Exception("HTTP Error " + response.getCode() + ": " + response.getReasonPhrase() +
                    " | Response Body: " + responseBody);
        }
    }

    /**
     * Parses the response entity into a string.
     *
     * @param entity The HTTP entity.
     * @return The response body as a string.
     * @throws Exception if parsing fails.
     */
    private String parseResponse(HttpEntity entity) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }

    /**
     * Retrieves a specific header value from the response.
     *
     * @param response The HTTP response.
     * @param header   The name of the header.
     * @return The header value or null if not found.
     */
    private String getHeader(CloseableHttpResponse response, String header) {
        return response.getFirstHeader(header) != null ? response.getFirstHeader(header).getValue() : null;
    }

    /**
     * Builds the full URL for an API endpoint.
     *
     * @param endpoint The API endpoint.
     * @return The full URL as a string.
     */
    private String buildUrl(String endpoint) {
        return config.getBaseUrl().endsWith("/") ? config.getBaseUrl() + endpoint : config.getBaseUrl() + "/" + endpoint;
    }
}
