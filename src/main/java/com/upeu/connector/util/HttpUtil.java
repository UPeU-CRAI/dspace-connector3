package com.upeu.connector.util;

import com.upeu.connector.auth.AuthManager;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for handling HTTP requests with authentication support.
 */
public class HttpUtil {

    private static final String ERROR_MESSAGE = "HTTP Error %d: %s | Response Body: %s";

    private final AuthManager authManager;
    private final CloseableHttpClient httpClient;

    public HttpUtil(AuthManager authManager, CloseableHttpClient httpClient) {
        this.authManager = authManager;
        this.httpClient = httpClient;
    }

    public String get(String url) throws Exception {
        return executeAuthenticatedRequest(new HttpGet(url));
    }

    public String post(String url, String payload) throws Exception {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
        return executeAuthenticatedRequest(request);
    }

    public String put(String url, String payload) throws Exception {
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
        return executeAuthenticatedRequest(request);
    }

    public String delete(String url) throws Exception {
        return executeAuthenticatedRequest(new HttpDelete(url));
    }

    private String executeAuthenticatedRequest(HttpUriRequestBase request) throws Exception {
        ensureAuthentication();
        authManager.addAuthenticationHeaders(request);

        try (CloseableHttpResponse response = httpClient.execute(request, authManager.getContext())) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
    }

    private void ensureAuthentication() throws Exception {
        if (!authManager.isAuthenticated()) {
            authManager.renewAuthentication();
        }
    }

    private void validateResponse(CloseableHttpResponse response) throws Exception {
        if (response.getCode() >= 400) {
            String responseBody = parseResponse(response.getEntity());
            throw new Exception(String.format(ERROR_MESSAGE, response.getCode(), response.getReasonPhrase(), responseBody));
        }
    }

    private String parseResponse(HttpEntity entity) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }
}
