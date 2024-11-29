package com.upeu.connector.auth;

import com.upeu.connector.util.EndpointUtil;
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
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthManager handles authentication and token management for DSpace API.
 */
public class AuthManager {

    private String jwtToken;
    private long tokenExpirationTime;
    private final BasicCookieStore cookieStore;
    private final HttpClientContext httpClientContext;
    private final EndpointUtil endpointUtil;
    private final String username;
    private final String password;

    private final Object lock = new Object();

    public AuthManager(String baseUrl, String username, String password) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("La URL base no puede ser nula o vacía.");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo o vacío.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía.");
        }

        this.endpointUtil = new EndpointUtil(baseUrl);
        this.username = username;
        this.password = password;
        this.cookieStore = new BasicCookieStore();
        this.httpClientContext = HttpClientContext.create();
        this.httpClientContext.setCookieStore(cookieStore);
    }

    public HttpClientContext getContext() {
        return httpClientContext;
    }

    public String getJwtToken() {
        validateAndRenewTokens();
        return jwtToken;
    }

    public void addAuthenticationHeaders(HttpUriRequestBase request) {
        validateAndRenewTokens();
        request.addHeader(new BasicHeader("Authorization", "Bearer " + jwtToken));
        request.addHeader(new BasicHeader("Content-Type", "application/json"));
    }

    public void renewAuthentication() {
        synchronized (lock) {
            jwtToken = null;
            validateAndRenewTokens();
        }
    }

    public boolean isAuthenticated() {
        return jwtToken != null && System.currentTimeMillis() < tokenExpirationTime;
    }

    private void validateAndRenewTokens() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() >= tokenExpirationTime) {
                jwtToken = obtainJwtToken();
            }
        }
    }

    private String obtainCsrfToken() {
        String endpoint = endpointUtil.getAuthnStatusEndpoint(); // Utilizando EndpointUtil
        HttpGet request = new HttpGet(endpoint);

        try (CloseableHttpClient httpClient = createHttpClient();
             CloseableHttpResponse response = httpClient.execute(request, httpClientContext)) {

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
        String csrfToken = obtainCsrfToken(); // Obtener el token CSRF
        String endpoint = endpointUtil.getAuthnLoginEndpoint(); // Utilizando EndpointUtil
        HttpPost request = new HttpPost(endpoint);

        try (CloseableHttpClient httpClient = createHttpClient()) {
            request.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
            request.addHeader(new BasicHeader("X-XSRF-TOKEN", csrfToken)); // Incluir el token CSRF en los encabezados

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
            }
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining JWT token", e);
        }
    }

    private String extractJwtTokenFromResponse(CloseableHttpResponse response) throws IOException {
        var authHeader = response.getFirstHeader("Authorization");
        if (authHeader != null && authHeader.getValue().startsWith("Bearer ")) {
            return authHeader.getValue().substring(7);
        }
        throw new RuntimeException("Authorization header is invalid or missing.");
    }

    private CloseableHttpClient createHttpClient() {
        return HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
    }
}
