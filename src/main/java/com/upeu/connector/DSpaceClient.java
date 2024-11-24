package com.upeu.connector;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
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

    // Initialize authentication and retrieve CSRF + JWT tokens
    public void authenticate() throws Exception {
        HttpPost loginRequest = new HttpPost(config.getBaseUrl() + "/rest/login");
        loginRequest.addHeader("Content-Type", "application/json");
        JSONObject credentials = new JSONObject();
        credentials.put("user", config.getUsername());
        credentials.put("password", config.getPassword());
        loginRequest.setEntity(new StringEntity(credentials.toString(), StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(loginRequest)) {
            if (response.getCode() != 200) {
                throw new Exception("Authentication failed: " + response.getReasonPhrase());
            }

            // Extract CSRF token and JWT token
            this.csrfToken = response.getFirstHeader("dspace-xsrf-token").getValue();
            this.jwtToken = response.getFirstHeader("Authorization").getValue();
        }
    }

    // Perform a GET request
    public String get(String endpoint) throws Exception {
        HttpGet request = new HttpGet(config.getBaseUrl() + endpoint);
        addAuthHeaders(request);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
    }

    // Perform a POST request
    public String post(String endpoint, String body) throws Exception {
        HttpPost request = new HttpPost(config.getBaseUrl() + endpoint);
        addAuthHeaders(request);
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
    }

    // Perform a PUT request
    public String put(String endpoint, String body) throws Exception {
        HttpPut request = new HttpPut(config.getBaseUrl() + endpoint);
        addAuthHeaders(request);
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
    }

    // Perform a DELETE request
    public void delete(String endpoint) throws Exception {
        HttpDelete request = new HttpDelete(config.getBaseUrl() + endpoint);
        addAuthHeaders(request);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            validateResponse(response);
        }
    }

    // Helper methods
    private void addAuthHeaders(HttpUriRequestBase request) {
        if (csrfToken != null) {
            request.addHeader("dspace-xsrf-token", csrfToken);
        }
        if (jwtToken != null) {
            request.addHeader("Authorization", jwtToken);
        }
        request.addHeader("Content-Type", "application/json");
    }

    private void validateResponse(CloseableHttpResponse response) throws Exception {
        if (response.getCode() >= 400) {
            throw new Exception("HTTP Error: " + response.getCode() + " - " + response.getReasonPhrase());
        }
    }

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
}
