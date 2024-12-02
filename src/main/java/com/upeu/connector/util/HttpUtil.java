package com.upeu.connector.util;

import com.upeu.connector.auth.AuthManager;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;

import java.io.IOException;

/**
 * Utility class for executing HTTP requests with authentication.
 */
public class HttpUtil {

    private final AuthManager authManager;
    private final CloseableHttpClient httpClient;

    /**
     * Constructor for HttpUtil.
     *
     * @param authManager The authentication manager.
     * @param httpClient  The HTTP client instance.
     */
    public HttpUtil(AuthManager authManager, CloseableHttpClient httpClient) {
        if (authManager == null) {
            throw new IllegalArgumentException("AuthManager no puede ser nulo.");
        }
        if (httpClient == null) {
            throw new IllegalArgumentException("HttpClient no puede ser nulo.");
        }
        this.authManager = authManager;
        this.httpClient = httpClient;
    }

    /**
     * Executes a GET request.
     *
     * @param url The URL to send the request to.
     * @return The response as a string.
     * @throws Exception If an error occurs.
     */
    public String get(String url) throws Exception {
        validateUrl(url);
        HttpGet request = new HttpGet(url);
        executeWithAuth(request);
        return executeRequest(request);
    }

    /**
     * Executes a POST request.
     *
     * @param url     The URL to send the request to.
     * @param payload The payload as a JSON string.
     * @return The response as a string.
     * @throws Exception If an error occurs.
     */
    public String post(String url, String payload) throws Exception {
        validateUrl(url);
        validatePayload(payload);
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        executeWithAuth(request);
        return executeRequest(request);
    }

    /**
     * Executes a PUT request.
     *
     * @param url     The URL to send the request to.
     * @param payload The payload as a JSON string.
     * @return The response as a string.
     * @throws Exception If an error occurs.
     */
    public String put(String url, String payload) throws Exception {
        validateUrl(url);
        validatePayload(payload);
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        executeWithAuth(request);
        return executeRequest(request);
    }

    /**
     * Executes a DELETE request.
     *
     * @param url The URL to send the request to.
     * @throws Exception If an error occurs.
     */
    public void delete(String url) throws Exception {
        validateUrl(url);
        HttpDelete request = new HttpDelete(url);
        executeWithAuth(request);
        executeRequestWithoutResponse(request);
    }

    /**
     * Adds authentication headers and ensures token validity.
     *
     * @param request The HTTP request.
     * @throws Exception If the authentication fails.
     */
    private void executeWithAuth(HttpUriRequestBase request) throws Exception {
        authManager.addAuthenticationHeaders(request);
    }

    /**
     * Executes the HTTP request and validates the response.
     *
     * @param request The HTTP request.
     * @return The response as a string.
     * @throws Exception If an error occurs.
     */
    private String executeRequest(HttpUriRequestBase request) throws Exception {
        try (CloseableHttpResponse response = httpClient.execute(request, authManager.getContext())) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
    }

    /**
     * Executes the HTTP request without expecting a response body.
     *
     * @param request The HTTP request.
     * @throws Exception If an error occurs.
     */
    private void executeRequestWithoutResponse(HttpUriRequestBase request) throws Exception {
        try (CloseableHttpResponse response = httpClient.execute(request, authManager.getContext())) {
            validateResponse(response);
        }
    }

    /**
     * Validates the response from the server.
     *
     * @param response The HTTP response.
     * @throws IOException If the response status is not successful.
     */
    private void validateResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getCode();
        if (statusCode < 200 || statusCode >= 300) {
            String responseBody = parseResponse(response.getEntity());
            throw new IOException("HTTP request failed with status code: " + statusCode + ". Response: " + responseBody);
        }
    }

    /**
     * Parses the response entity into a string.
     *
     * @param entity The HTTP entity.
     * @return The entity content as a string.
     * @throws IOException If an error occurs while reading the entity.
     */
    private String parseResponse(HttpEntity entity) throws IOException {
        return entity != null ? new String(entity.getContent().readAllBytes()) : "";
    }

    /**
     * Validates the URL.
     *
     * @param url The URL to validate.
     */
    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL no puede ser nula ni vacía.");
        }
    }

    /**
     * Validates the payload.
     *
     * @param payload The payload to validate.
     */
    private void validatePayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("El cuerpo de la solicitud no puede ser nulo ni vacío.");
        }
    }
}
