package com.upeu.connector.util;

import com.upeu.connector.auth.AuthManager;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.json.JSONObject;

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
        ensureAuthentication(); // Ensure authentication is configured
        HttpGet request = new HttpGet(url); // Correctly define the HttpGet instance
        authManager.addAuthenticationHeaders(request); // Add authentication headers

        try (CloseableHttpResponse response = httpClient.execute(request, authManager.getContext())) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
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
        ensureAuthentication();
        HttpPost request = new HttpPost(url); // Correctly define the HttpPost instance
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        authManager.addAuthenticationHeaders(request);

        try (CloseableHttpResponse response = httpClient.execute(request, authManager.getContext())) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
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
        ensureAuthentication();
        HttpPut request = new HttpPut(url); // Correctly define the HttpPut instance
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        authManager.addAuthenticationHeaders(request);

        try (CloseableHttpResponse response = httpClient.execute(request, authManager.getContext())) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
    }

    /**
     * Executes a DELETE request.
     *
     * @param url The URL to send the request to.
     * @throws Exception If an error occurs.
     */
    public void delete(String url) throws Exception {
        ensureAuthentication();
        HttpDelete request = new HttpDelete(url); // Correctly define the HttpDelete instance
        authManager.addAuthenticationHeaders(request);

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
            throw new IOException("HTTP request failed with status code: " + statusCode);
        }
    }

    /**
     * Parses the response entity into a string.
     *
     * @param entity The HTTP entity.
     * @return The entity content as a string.
     * @throws IOException If an error occurs while reading the entity.
     */
    private String parseResponse(org.apache.hc.core5.http.HttpEntity entity) throws IOException {
        return entity != null ? new String(entity.getContent().readAllBytes()) : "";
    }

    /**
     * Ensures the authentication is valid.
     *
     * @throws Exception If the authentication fails.
     */
    private void ensureAuthentication() throws Exception {
        if (!authManager.isAuthenticated()) {
            authManager.renewAuthentication();
        }
    }
}
