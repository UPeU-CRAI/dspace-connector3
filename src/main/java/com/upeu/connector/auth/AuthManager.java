package com.upeu.connector.auth;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthManager handles authentication and token management for DSpace API.
 */
public class AuthManager {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_BEARER_PREFIX = "Bearer ";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private String jwtToken;
    private long tokenExpirationTime;
    private final BasicCookieStore cookieStore;
    private final HttpClientContext httpClientContext;
    private final CloseableHttpClient httpClient;
    private final EndpointUtil endpointUtil;
    private final String username;
    private final String password;

    private final Object lock = new Object();

    public AuthManager(String baseUrl, String username, String password) {
        validateNonEmpty(baseUrl, "La URL base no puede ser nula o vacía.");
        validateNonEmpty(username, "El nombre de usuario no puede ser nulo o vacío.");
        validateNonEmpty(password, "La contraseña no puede ser nula o vacía.");

        this.endpointUtil = new EndpointUtil(baseUrl);
        this.username = username;
        this.password = password;
        this.cookieStore = new BasicCookieStore();
        this.httpClientContext = HttpClientContext.create();
        this.httpClientContext.setCookieStore(cookieStore);

        // Inicializar httpClient
        this.httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    public String getJwtToken() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() >= tokenExpirationTime) {
                jwtToken = obtainJwtToken();
            }
            return jwtToken;
        }
    }

    public void addAuthenticationHeaders(HttpUriRequestBase request) {
        request.addHeader(HEADER_AUTHORIZATION, HEADER_BEARER_PREFIX + getJwtToken());
        request.addHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON);
    }

    private String obtainCsrfToken() {
        HttpGet request = new HttpGet(endpointUtil.getAuthnStatusEndpoint());
        try (CloseableHttpResponse response = httpClient.execute(request, httpClientContext)) {
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
        HttpPost request = new HttpPost(endpointUtil.getAuthnLoginEndpoint());
        request.addHeader(HEADER_CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
        request.addHeader("X-XSRF-TOKEN", obtainCsrfToken());

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("user", username));
        params.add(new BasicNameValuePair("password", password));
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(request, httpClientContext)) {
            if (response.getCode() == 200) {
                jwtToken = extractJwtTokenFromResponse(response);
                tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // 1 hora
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

    private static void validateNonEmpty(String value, String errorMessage) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
