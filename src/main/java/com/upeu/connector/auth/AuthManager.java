package com.upeu.connector.auth;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthManager centralizes authentication and HTTP request management for DSpace API.
 */
public class AuthManager {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_BEARER_PREFIX = "Bearer ";
    private static final String HEADER_X_XSRF_TOKEN = "X-XSRF-TOKEN";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private final BasicCookieStore cookieStore;
    private final HttpClientContext httpClientContext;
    private final CloseableHttpClient httpClient;
    private final String baseUrl;
    private final String username;
    private final String password;

    private String jwtToken;
    private long tokenExpirationTime;

    private final Object lock = new Object();

    public AuthManager(String baseUrl, String username, String password) {
        validateNonEmpty(baseUrl, "La URL base no puede ser nula o vacía.");
        validateNonEmpty(username, "El nombre de usuario no puede ser nulo o vacío.");
        validateNonEmpty(password, "La contraseña no puede ser nula o vacía.");

        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.username = username;
        this.password = password;
        this.cookieStore = new BasicCookieStore();
        this.httpClientContext = HttpClientContext.create();
        this.httpClientContext.setCookieStore(cookieStore);

        this.httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    // ==============================
    // Autenticación
    // ==============================

    /**
     * Verifica si el token JWT está autenticado y no ha expirado.
     * @return true si está autenticado, false de lo contrario.
     */
    public boolean isAuthenticated() {
        return jwtToken != null && System.currentTimeMillis() < tokenExpirationTime;
    }

    /**
     * Renueva la autenticación obteniendo un nuevo token JWT.
     */
    public void renewAuthentication() {
        synchronized (lock) {
            jwtToken = obtainJwtToken();
        }
    }

    public String getJwtToken() {
        synchronized (lock) {
            if (!isAuthenticated()) {
                jwtToken = obtainJwtToken();
            }
            return jwtToken;
        }
    }

    public void addAuthenticationHeaders(HttpUriRequestBase request) {
        request.addHeader(HEADER_AUTHORIZATION, HEADER_BEARER_PREFIX + getJwtToken());
        request.addHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON);
    }

    public void validateConnection() {
        HttpGet request = new HttpGet(buildEndpoint("server/api/authn/status"));
        try (var response = httpClient.execute(request, httpClientContext)) {
            if (response.getCode() != 200) {
                throw new IllegalStateException("Failed to validate connection. Status code: " + response.getCode());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error during connection validation: " + e.getMessage(), e);
        }
    }

    private String obtainCsrfToken() {
        HttpGet request = new HttpGet(buildEndpoint("server/api/authn/status"));
        try (var response = httpClient.execute(request, httpClientContext)) {
            if (response.getCode() == 200) {
                return cookieStore.getCookies().stream()
                        .filter(cookie -> "DSPACE-XSRF-COOKIE".equals(cookie.getName()))
                        .map(cookie -> cookie.getValue())
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("CSRF token not found in cookies."));
            } else {
                throw new RuntimeException("Failed to obtain CSRF token. Status code: " + response.getCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining CSRF token", e);
        }
    }

    private String obtainJwtToken() {
        HttpPost request = new HttpPost(buildEndpoint("server/api/authn/login"));
        request.addHeader(HEADER_CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
        request.addHeader(HEADER_X_XSRF_TOKEN, obtainCsrfToken());

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("user", username));
        params.add(new BasicNameValuePair("password", password));
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (var response = httpClient.execute(request, httpClientContext)) {
            if (response.getCode() == 200) {
                jwtToken = extractJwtTokenFromResponse(response);
                tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // 1 hour
                return jwtToken;
            } else {
                throw new RuntimeException("Error obtaining JWT token. Status code: " + response.getCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining JWT token", e);
        }
    }

    private String extractJwtTokenFromResponse(CloseableHttpResponse response) throws IOException {
        var authHeader = response.getFirstHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.getValue().startsWith(HEADER_BEARER_PREFIX)) {
            return authHeader.getValue().substring(HEADER_BEARER_PREFIX.length());
        }
        throw new RuntimeException("Authorization header is invalid or missing.");
    }

    public String buildEndpoint(String endpointKey) {
        validateNonEmpty(endpointKey, "El endpoint relativo no puede ser nulo ni vacío.");

        // Si `endpointKey` ya es una URL completa, devolverla directamente
        if (endpointKey.startsWith("http://") || endpointKey.startsWith("https://")) {
            return endpointKey;
        }

        // Asegurarse de que `endpointKey` no tenga una barra inicial redundante
        String normalizedEndpoint = endpointKey.startsWith("/") ? endpointKey.substring(1) : endpointKey;

        // Construir la URL completa
        return baseUrl.endsWith("/") ? baseUrl + normalizedEndpoint : baseUrl + "/" + normalizedEndpoint;
    }

    // ==============================
    // Métodos HTTP
    // ==============================

    public String get(String url) throws Exception {
        validateNonEmpty(url, "La URL no puede ser nula ni vacía.");
        HttpGet request = new HttpGet(url);
        return executeWithAuth(request);
    }

    public String post(String url, String payload) throws Exception {
        validateNonEmpty(url, "La URL no puede ser nula ni vacía.");
        validateNonEmpty(payload, "El cuerpo de la solicitud no puede ser nulo ni vacío.");
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        return executeWithAuth(request);
    }

    public String put(String url, String payload) throws Exception {
        validateNonEmpty(url, "La URL no puede ser nula ni vacía.");
        validateNonEmpty(payload, "El cuerpo de la solicitud no puede ser nulo ni vacío.");
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        return executeWithAuth(request);
    }

    public void delete(String url) throws Exception {
        validateNonEmpty(url, "La URL no puede ser nula ni vacía.");
        HttpDelete request = new HttpDelete(url);
        executeWithAuthWithoutResponse(request);
    }

    private String executeWithAuth(HttpUriRequestBase request) throws Exception {
        addAuthenticationHeaders(request);
        try (var response = httpClient.execute(request, httpClientContext)) {
            validateResponse(response);
            return parseResponse(response.getEntity());
        }
    }

    private void executeWithAuthWithoutResponse(HttpUriRequestBase request) throws Exception {
        addAuthenticationHeaders(request);
        try (var response = httpClient.execute(request, httpClientContext)) {
            validateResponse(response);
        }
    }

    private void validateResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("HTTP request failed with status code: " + statusCode);
        }
    }

    private String parseResponse(HttpEntity entity) throws IOException {
        return entity != null ? new String(entity.getContent().readAllBytes()) : "";
    }

    private void validateNonEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
