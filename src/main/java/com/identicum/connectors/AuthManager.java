package com.identicum.connectors;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.io.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.BasicCookieStore;
import org.apache.hc.core5.http.cookie.Cookie;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AuthManager {

    private String jwtToken;
    private long tokenExpirationTime;
    private final Object lock = new Object();
    private BasicCookieStore cookieStore;

    private final String serviceAddress;
    private final String username;
    private final String password;

    public AuthManager(String serviceAddress, String username, String password) {
        this.serviceAddress = serviceAddress;
        this.username = username;
        this.password = password;
    }

    public String obtainCsrfToken() {
        String endpoint = serviceAddress + "/server/api/authn/status";
        HttpGet request = new HttpGet(endpoint);

        cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        try (CloseableHttpResponse response = HttpClients.createDefault().execute(request, context)) {
            if (response.getCode() == 200) {
                for (Cookie cookie : cookieStore.getCookies()) {
                    if ("DSPACE-XSRF-COOKIE".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
                throw new RuntimeException("CSRF token not found in cookies");
            } else {
                throw new RuntimeException("Failed to obtain CSRF token. Status code: " + response.getCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining CSRF token", e);
        }
    }

    public String obtainJwtToken() {
        String csrfToken = obtainCsrfToken();
        String endpoint = serviceAddress + "/server/api/authn/login";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setHeader("X-XSRF-TOKEN", csrfToken);

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("user", username));
        params.add(new BasicNameValuePair("password", password));
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = HttpClients.createDefault().execute(request)) {
            if (response.getCode() == 200) {
                var authHeader = response.getFirstHeader("Authorization");
                if (authHeader != null && authHeader.getValue().startsWith("Bearer ")) {
                    jwtToken = authHeader.getValue().substring(7);
                    tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // 1 hora
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

    public String getJwtToken() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                jwtToken = obtainJwtToken();
            }
            return jwtToken;
        }
    }
}
