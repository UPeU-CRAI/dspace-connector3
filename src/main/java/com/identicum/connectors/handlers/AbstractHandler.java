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
        LOG.info("Inicializando AbstractHandler...");
        this.authenticationHandler = authenticationHandler;
        this.endpoints = new Endpoints(authenticationHandler.getBaseUrl()); // Initialize Endpoints with baseUrl
        LOG.info("AbstractHandler inicializado con base URL: {}", authenticationHandler.getBaseUrl());
    }

    /**
     * Sends an HTTP request.
     *
     * @param request The HTTP request to execute.
     * @return The HTTP response.
     * @throws IOException in case of communication errors.
     */
    protected CloseableHttpResponse sendRequest(HttpUriRequestBase request) throws IOException {
        String jwtToken = authenticationHandler.getJwtToken();
        LOG.debug("Configurando encabezado Authorization con JWT token.");
        request.setHeader("Authorization", "Bearer " + jwtToken);

        LOG.info("Enviando solicitud HTTP al endpoint: {}", request.getRequestUri());
        try {
            CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request);
            LOG.info("Solicitud enviada exitosamente. Código de estado: {}", response.getCode());
            return response;
        } catch (IOException e) {
            LOG.error("Error al enviar la solicitud HTTP: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sends a GET request to a given endpoint.
     *
     * @param endpoint the full endpoint URL
     * @return the HTTP response
     * @throws IOException in case of communication errors
     */
    protected CloseableHttpResponse sendGetRequest(String endpoint) throws IOException {
        LOG.info("Preparando solicitud GET para el endpoint: {}", endpoint);
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
        LOG.info("Parseando el cuerpo de la respuesta...");
        try {
            String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            LOG.debug("Cuerpo de la respuesta recibido: {}", responseBody);
            return new JSONObject(responseBody); // Convert the response body to JSONObject
        } catch (IOException e) {
            LOG.error("Error al parsear el cuerpo de la respuesta: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves the full base URL from AuthenticationHandler.
     *
     * @return the base URL as a String
     */
    protected String getBaseUrl() {
        String baseUrl = authenticationHandler.getBaseUrl();
        LOG.debug("Obteniendo la base URL desde AuthenticationHandler: {}", baseUrl);
        return baseUrl;
    }

    /**
     * Builds the full URL for a given path using the base URL and an endpoint path.
     *
     * @param path the specific API path (e.g., "/eperson/epersons")
     * @return the full URL as a String
     */
    protected String buildUrl(String path) {
        LOG.info("Construyendo la URL completa para el path: {}", path);
        String fullUrl = endpoints.buildEndpoint(path);
        LOG.info("URL construida: {}", fullUrl);
        return fullUrl;
    }
}
