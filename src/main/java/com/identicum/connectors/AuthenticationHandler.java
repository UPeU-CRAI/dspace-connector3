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
import org.identityconnectors.common.security.GuardedString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * AuthenticationHandler handles the authentication flow for DSpace-CRIS.
 * It manages CSRF and JWT tokens and provides methods for secured HTTP requests.
 */
public class AuthenticationHandler {

    private final DSpaceConnectorConfiguration config; // Configuration object
    private final BasicCookieStore cookieStore; // Cookie store for CSRF token management
    private final CloseableHttpClient httpClient; // Reusable HTTP client

    private String jwtToken; // Cached JWT token
    private long tokenExpirationTime; // Expiration time for the JWT token
    private final Object lock = new Object(); // Lock for thread-safe JWT refresh

    /**
     * Constructor for AuthenticationHandler.
     *
     * @param config The configuration object providing validated values.
     */
    public AuthenticationHandler(DSpaceConnectorConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null.");
        }
        this.config = config;
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    }

    /**
     * Obtain CSRF Token from the server.
     *
     * @return CSRF token as a string.
     * @throws RuntimeException if unable to fetch the CSRF token
     */
    private String obtainCsrfToken() {
        String endpoint = config.getBaseUrl() + "/server/api/authn/status";
        HttpGet request = new HttpGet(endpoint);

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
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining CSRF token", e);
        }
    }

    /**
     * Obtain JWT Token using CSRF token and user credentials.
     *
     * @return JWT token as a string.
     * @throws RuntimeException if unable to fetch the JWT token
     */
    private String obtainJwtToken() {
        String csrfToken = obtainCsrfToken(); // Get CSRF token first
        String endpoint = config.getBaseUrl() + "/server/api/authn/login";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setHeader("X-XSRF-TOKEN", csrfToken);

        // Prepare login credentials as URL-encoded parameters
        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("user", config.getUsername()));
        params.add(new BasicNameValuePair("password", extractPassword(config.getPassword())));
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() == 200) {
                var authHeader = response.getFirstHeader("Authorization");
                if (authHeader != null && authHeader.getValue().startsWith("Bearer ")) {
                    jwtToken = authHeader.getValue().substring(7); // Extract token from "Bearer <token>"
                    tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // Token valid for 1 hour
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
     * Get JWT Token or refresh it if expired.
     *
     * @return JWT token as a string.
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
     * Extract the password from GuardedString.
     *
     * @param guardedPassword The password object.
     * @return Decrypted password as string.
     */
    private String extractPassword(GuardedString guardedPassword) {
        final StringBuilder password = new StringBuilder();
        guardedPassword.access(chars -> password.append(new String(chars)));
        return password.toString();
    }

    /**
     * Test the connection to the DSpace server.
     */
    public void testConnection() {
        String endpoint = config.getBaseUrl() + "/server/api/authn/status";
        HttpGet request = new HttpGet(endpoint);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 200) {
                throw new RuntimeException("Test connection failed. Status code: " + response.getCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error testing connection", e);
        }
    }

    /**
     * Get the reusable HTTP client.
     *
     * @return CloseableHttpClient instance.
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Get the Base URL.
     *
     * @return Base URL as a string.
     */
    public String getBaseUrl() {
        return config.getBaseUrl();
    }
}