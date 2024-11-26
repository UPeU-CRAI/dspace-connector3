package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * DSpaceClient handles API communication with DSpace-CRIS.
 */
public class DSpaceClient {

    private final DSpaceConfiguration config;
    private final CloseableHttpClient httpClient;
    private final AuthManager authManager;

    public DSpaceClient(DSpaceConfiguration config, AuthManager authManager) {
        this.config = config;
        this.authManager = authManager;
        this.httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.custom()
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                        .setResponseTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                        .build())
                .build();
    }

    /**
     * Execute an HTTP request, ensuring authentication is valid.
     *
     * @param request The HTTP request.
     * @return The HTTP response.
     * @throws IOException if the request fails.
     */
    private CloseableHttpResponse executeRequest(HttpUriRequestBase request) throws IOException {
        if (!authManager.isAuthenticated()) {
            authManager.renewAuthentication(); // Renueva autenticación si es necesario
        }
        authManager.addAuthenticationHeaders(request); // Agrega encabezados de autenticación
        return httpClient.execute(request, authManager.getContext());
    }

    /**
     * Perform a GET request.
     *
     * @param endpoint API endpoint.
     * @return The response body as a string.
     * @throws Exception if the request fails.
     */
    public String get(String endpoint) throws Exception {
        HttpGet request = new HttpGet(buildUrl(endpoint));
        CloseableHttpResponse response;

        try {
            response = executeRequest(request);
        } catch (IOException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("403")) {
                authManager.renewAuthentication();
                response = executeRequest(request);
            } else {
                throw e; // Otros errores se propagan
            }
        }

        validateResponse(response);
        return parseResponse(response.getEntity());
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
        HttpPost request = new HttpPost(buildUrl(endpoint));
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        CloseableHttpResponse response;

        try {
            response = executeRequest(request);
        } catch (IOException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("403")) {
                authManager.renewAuthentication();
                response = executeRequest(request);
            } else {
                throw e; // Otros errores se propagan
            }
        }

        validateResponse(response);
        return parseResponse(response.getEntity());
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
        HttpPut request = new HttpPut(buildUrl(endpoint));
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        CloseableHttpResponse response;

        try {
            response = executeRequest(request);
        } catch (IOException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("403")) {
                authManager.renewAuthentication();
                response = executeRequest(request);
            } else {
                throw e; // Otros errores se propagan
            }
        }

        validateResponse(response);
        return parseResponse(response.getEntity());
    }

    /**
     * Perform a DELETE request.
     *
     * @param endpoint API endpoint.
     * @throws Exception if the request fails.
     */
    public void delete(String endpoint) throws Exception {
        HttpDelete request = new HttpDelete(buildUrl(endpoint));
        CloseableHttpResponse response;

        try {
            response = executeRequest(request);
        } catch (IOException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("403")) {
                authManager.renewAuthentication();
                response = executeRequest(request);
            } else {
                throw e; // Otros errores se propagan
            }
        }

        validateResponse(response);
    }

    /**
     * Validates an HTTP response for errors.
     *
     * @param response The HTTP response.
     * @throws Exception if the response indicates an error.
     */
    private void validateResponse(CloseableHttpResponse response) throws Exception {
        if (response.getCode() >= 400) {
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
     * Builds the full URL for an API endpoint.
     *
     * @param endpoint The API endpoint.
     * @return The full URL as a string.
     */
    private String buildUrl(String endpoint) {
        return config.getBaseUrl().endsWith("/") ? config.getBaseUrl() + endpoint : config.getBaseUrl() + "/" + endpoint;
    }
}
