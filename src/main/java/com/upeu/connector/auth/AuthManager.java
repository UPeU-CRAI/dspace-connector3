package com.upeu.connector.auth;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.cookie.BasicCookieStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AuthManager {

    private final String baseUrl;
    private final String username;
    private final String password;
    private String jwtToken;
    private String csrfToken;
    private final CookieStore cookieStore;
    private final HttpClientContext context;

    public AuthManager(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.cookieStore = new BasicCookieStore();
        this.context = HttpClientContext.create();
        this.context.setCookieStore(cookieStore);
    }

    /**
     * Authenticate and obtain JWT and CSRF tokens.
     */
    public void authenticate() throws IOException {
        try (CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()) {
            // Get CSRF Token
            HttpGet csrfRequest = new HttpGet(baseUrl + "/rest/login");
            HttpResponse csrfResponse = client.execute(csrfRequest, context);
            if (csrfResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed to get CSRF token. Response code: " + csrfResponse.getStatusLine().getStatusCode());
            }
            csrfToken = extractCsrfToken(csrfResponse);

            // Authenticate and get JWT Token
            HttpPost authRequest = new HttpPost(baseUrl + "/rest/login");
            authRequest.addHeader("Content-Type", "application/json");
            authRequest.addHeader("X-CSRF-Token", csrfToken);

            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", username);
            credentials.put("password", password);
            StringEntity authPayload = new StringEntity(new JSONObject(credentials).toString());
            authRequest.setEntity(authPayload);

            HttpResponse authResponse = client.execute(authRequest, context);
            if (authResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Authentication failed. Response code: " + authResponse.getStatusLine().getStatusCode());
            }

            jwtToken = extractJwtToken(authResponse);
        }
    }

    /**
     * Renew authentication if the token is invalid or expired.
     */
    public void renewAuthentication() throws IOException {
        jwtToken = null;
        csrfToken = null;
        authenticate();
    }

    /**
     * Add authentication headers to a request.
     */
    public void addAuthenticationHeaders(HttpPost request) {
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("X-CSRF-Token", csrfToken);
    }

    private String extractJwtToken(HttpResponse response) throws IOException {
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONObject jsonResponse = new JSONObject(responseBody);
        return jsonResponse.getString("token");
    }

    private String extractCsrfToken(HttpResponse response) {
        return context.getCookieStore().getCookies().stream()
                .filter(cookie -> "CSRF-TOKEN".equals(cookie.getName()))
                .findFirst()
                .map(org.apache.http.cookie.Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("CSRF token not found in cookies"));
    }

    public boolean isAuthenticated() {
        return jwtToken != null && csrfToken != null;
    }

    public HttpClientContext getContext() {
        return context;
    }
}
