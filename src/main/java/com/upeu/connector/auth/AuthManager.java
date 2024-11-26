package com.upeu.connector.auth;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AuthManager {

    private String jwtToken;
    private long tokenExpirationTime;
    private String csrfToken;
    private final Object lock = new Object();
    private final BasicCookieStore cookieStore;
    private final HttpClientContext httpClientContext; // Declarar aquí el atributo
    private boolean testMode = false;

    private final String baseUrl;
    private final String username;
    private final String password;

    public AuthManager(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.cookieStore = new BasicCookieStore();
        this.httpClientContext = HttpClientContext.create(); // Inicializar aquí
        this.httpClientContext.setCookieStore(cookieStore); // Asignar el almacén de cookies
    }

    public HttpClientContext getContext() {
        return httpClientContext; // Método para obtener el contexto
    }

    /**
     * Obtain CSRF Token.
     *
     * @return The CSRF token.
     */
    private String obtainCsrfToken() {
        String endpoint = baseUrl + "/server/api/authn/status";
        HttpGet request = new HttpGet(endpoint);

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() == 200) {
                return cookieStore.getCookies().stream()
                        .filter(cookie -> "DSPACE-XSRF-COOKIE".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("CSRF token not found in cookies"));
            } else {
                throw new RuntimeException("Failed to obtain CSRF token. Status code: " + response.getCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining CSRF token", e);
        }
    }

    /**
     * Obtain JWT Token.
     *
     * @return The JWT token.
     */
    private String obtainJwtToken() {
        String csrfToken = obtainCsrfToken();
        String endpoint = baseUrl + "/server/api/authn/login";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setHeader("X-XSRF-TOKEN", csrfToken);

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("user", username));
        params.add(new BasicNameValuePair("password", password));
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() == 200) {
                var authHeader = response.getFirstHeader("Authorization");
                if (authHeader != null && authHeader.getValue().startsWith("Bearer ")) {
                    jwtToken = authHeader.getValue().substring(7);
                    tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // 1 hora
                    this.csrfToken = csrfToken; // Actualiza CSRF para solicitudes futuras
                    return jwtToken;
                } else {
                    throw new RuntimeException("Authorization header missing or invalid");
                }
            } else {
                throw new RuntimeException("Failed to obtain JWT token. Status code: " + response.getCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining JWT token", e);
        }
    }

    /**
     * Get or renew JWT Token if expired.
     *
     * @return The valid JWT token.
     */
    public String getJwtToken() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                jwtToken = obtainJwtToken();
            }
            return jwtToken;
        }
    }

    /**
     * Add authentication headers to a request.
     *
     * @param request The HTTP request.
     */
    public void addAuthenticationHeaders(HttpPost request) {
        getJwtToken(); // Ensure token is valid
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("X-XSRF-TOKEN", csrfToken);
        request.addHeader("Content-Type", "application/json");
    }

    /**
     * Add authentication headers to a request.
     *
     * @param request The HTTP request.
     */
    public void addAuthenticationHeaders(HttpGet request) {
        getJwtToken(); // Ensure token is valid
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("X-XSRF-TOKEN", csrfToken);
        request.addHeader("Content-Type", "application/json");
    }

    // Este método reutiliza la lógica existente para agregar los encabezados de autenticación a cualquier tipo de solicitud.
    public void addAuthenticationHeaders(HttpUriRequestBase request) {
        getJwtToken(); // Ensure token is valid
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("X-XSRF-TOKEN", csrfToken);
        request.addHeader("Content-Type", "application/json");
    }

    /**
     * Renew authentication explicitly.
     */
    public void renewAuthentication() {
        synchronized (lock) {
            jwtToken = null;
            csrfToken = null;
            obtainJwtToken();
        }
    }

    /**
     * Validate if authenticated.
     *
     * @return True if authenticated, false otherwise.
     */
    public boolean isAuthenticated() {
        return jwtToken != null && System.currentTimeMillis() < tokenExpirationTime;
    }

    public void enableTestMode() {
        this.testMode = true;
    }


}
