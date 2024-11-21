package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Abstract base handler for DSpace operations.
 * Provides common functionality for all handlers.
 */
public abstract class AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractHandler.class);

    protected final AuthenticationHandler authenticationHandler;
    protected final Endpoints endpoints; // Add an Endpoints field

    /**
     * Constructor for AbstractHandler.
     *
     * @param authenticationHandler The AuthenticationHandler instance.
     */
    public AbstractHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
        this.endpoints = new Endpoints(authenticationHandler.getBaseUrl()); // Initialize Endpoints with baseUrl
    }

    /**
     * Sends an HTTP request.
     *
     * @param request The HTTP request to execute.
     * @return The HTTP response.
     * @throws IOException in case of communication errors.
     */
    protected CloseableHttpResponse sendRequest(HttpUriRequestBase request) throws IOException {
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        return authenticationHandler.getHttpClient().execute(request);
    }

    /**
     * Sends a GET request to a given endpoint.
     *
     * @param endpoint the full endpoint URL
     * @return the HTTP response
     * @throws IOException in case of communication errors
     */
    protected CloseableHttpResponse sendGetRequest(String endpoint) throws IOException {
        HttpGet request = new HttpGet(endpoint);
        return sendRequest(request);
    }

    /**
     * Parses the response body from an HTTP response into a JSONObject.
     *
     * @param response The HTTP response.
     * @return A JSONObject representing the response body.
     * @throws IOException If the response body cannot be read.
     */
    protected JSONObject parseResponseBody(CloseableHttpResponse response) throws IOException {
        String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        LOG.debug("Response body: {}", responseBody);
        return new JSONObject(responseBody); // Convert the response body to JSONObject
    }

    /**
     * Retrieves the full base URL from AuthenticationHandler.
     *
     * @return the base URL as a String
     */
    protected String getBaseUrl() {
        return authenticationHandler.getBaseUrl(); // Ensure AuthenticationHandler has this method.
    }

    /**
     * Builds the full URL for a given path using the base URL and an endpoint path.
     *
     * @param path the specific API path (e.g., "/eperson/epersons")
     * @return the full URL as a String
     */
    protected String buildUrl(String path) {
        return endpoints.buildEndpoint(path); // Use the Endpoints instance to build the URL
    }
}
