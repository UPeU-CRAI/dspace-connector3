package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.io.IOException;

/**
 * Abstract base handler for DSpace operations.
 * Provides common functionality for all handlers.
 */
public abstract class AbstractHandler {

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
