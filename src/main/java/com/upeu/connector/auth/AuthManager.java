package com.upeu.connector.auth;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.protocol.HttpClientContext;

public class AuthManager {

    private String jwtToken;
    private long tokenExpirationTime;
    private String csrfToken;
    private final Object lock = new Object();
    private final BasicCookieStore cookieStore;
    private final HttpClientContext httpClientContext;
    private final String baseUrl;
    private final String username;
    private final String password;

    public AuthManager(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.cookieStore = new BasicCookieStore();
        this.httpClientContext = HttpClientContext.create();
        this.httpClientContext.setCookieStore(cookieStore);
    }

    public HttpClientContext getContext() {
        return httpClientContext;
    }

    private String obtainCsrfToken() {
        try {
            // Lógica real para obtener el token CSRF
            // Realiza la solicitud al servidor y extrae el token CSRF de las cookies
            throw new UnsupportedOperationException("Implementación pendiente para obtener el token CSRF");
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el token CSRF", e);
        }
    }

    private String obtainJwtToken() {
        try {
            this.csrfToken = obtainCsrfToken();
            // Lógica real para obtener el token JWT utilizando el CSRF token
            throw new UnsupportedOperationException("Implementación pendiente para obtener el token JWT");
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el token JWT", e);
        }
    }

    private void validateAndRenewTokens() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                jwtToken = obtainJwtToken();
                tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // 1 hora
            }
        }
    }

    public String getJwtToken() {
        validateAndRenewTokens();
        return jwtToken;
    }

    public void addAuthenticationHeaders(HttpUriRequestBase request) {
        validateAndRenewTokens();
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("X-XSRF-TOKEN", csrfToken);
        request.addHeader("Content-Type", "application/json");
    }

    public void renewAuthentication() {
        synchronized (lock) {
            jwtToken = null;
            csrfToken = null;
            validateAndRenewTokens();
        }
    }

    public boolean isAuthenticated() {
        return jwtToken != null && System.currentTimeMillis() < tokenExpirationTime;
    }
}
