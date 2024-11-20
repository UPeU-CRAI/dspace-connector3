package com.identicum.connectors;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * AuthenticationHandler handles the authentication flow for DSpace-CRIS.
 * It manages CSRF and JWT tokens and provides methods for secured HTTP requests.
 */
public class AuthenticationHandler {

    private final String serviceAddress;
    private final String username;
    private final String password;

    private String jwtToken;
    private long tokenExpirationTime;
    private BasicCookieStore cookieStore;

    private final Object lock = new Object();

    // =====================================
    // Constructor
    // =====================================
    public AuthenticationHandler(String serviceAddress, String username, String password) {
        this.serviceAddress = serviceAddress;
        this.username = username;
        this.password = password;
    }

    // =====================================
    // Obtain CSRF Token
    // =====================================
    private String obtainCsrfToken() {
        String endpoint = serviceAddress + "/server/api/authn/status";
        HttpGet request = new HttpGet(endpoint);

        cookieStore = new BasicCookieStore();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    return cookieStore.getCookies().stream()
                            .filter(cookie -> "DSPACE-XSRF-COOKIE".equals(cookie.getName()))
                            .map(Cookie::getValue)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("CSRF token not found in cookies"));
                } else {
                    throw new RuntimeException("Failed to obtain CSRF token. Status code: " + response.getCode());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining CSRF token", e);
        }
    }

    // =====================================
    // Obtain JWT Token
    // =====================================
    private String obtainJwtToken() {
        String csrfToken = obtainCsrfToken();
        String endpoint = serviceAddress + "/server/api/authn/login";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setHeader("X-XSRF-TOKEN", csrfToken);

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("user", username));
        params.add(new BasicNameValuePair("password", password));
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() == 200) {
                var authHeader = response.getFirstHeader("Authorization");
                if (authHeader != null && authHeader.getValue().startsWith("Bearer ")) {
                    jwtToken = authHeader.getValue().substring(7);
                    tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // 1 hour
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

    // =====================================
    // Get JWT Token or Refresh if Expired
    // =====================================
    public String getJwtToken() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                jwtToken = obtainJwtToken();
            }
            return jwtToken;
        }
    }

    // =====================================
    // Test Connection
    // =====================================
    public void testConnection() {
        String endpoint = serviceAddress + "/server/api/authn/status";
        HttpGet request = new HttpGet(endpoint);

        try (CloseableHttpClient httpClient = HttpClients.custom().build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() != 200) {
                throw new RuntimeException("Test connection failed. Status code: " + response.getCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error testing connection", e);
        }
    }

    public Executor getHttpClient() {
        return null;
    }
}
