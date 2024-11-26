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
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;

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

    /**
     * Obtiene el token CSRF desde el endpoint `/server/api/authn/status`.
     *
     * @return Token CSRF.
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
                        .map(cookie -> cookie.getValue())
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No se encontró el token CSRF en las cookies"));
            } else {
                throw new RuntimeException("Error al obtener el token CSRF. Código de estado: " + response.getCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al obtener el token CSRF", e);
        }
    }

    /**
     * Obtiene el token JWT desde el endpoint `/server/api/authn/login`.
     *
     * @return Token JWT.
     */
    private String obtainJwtToken() {
        String endpoint = baseUrl + "/server/api/authn/login";
        HttpPost request = new HttpPost(endpoint);

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {

            // Configura los encabezados y parámetros de la solicitud
            request.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
            request.addHeader(new BasicHeader("X-XSRF-TOKEN", obtainCsrfToken()));

            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("user", username));
            params.add(new BasicNameValuePair("password", password));
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    var authHeader = response.getFirstHeader("Authorization");
                    if (authHeader != null && authHeader.getValue().startsWith("Bearer ")) {
                        jwtToken = authHeader.getValue().substring(7);
                        tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // 1 hora
                        return jwtToken;
                    } else {
                        throw new RuntimeException("El encabezado de autorización es inválido o falta");
                    }
                } else {
                    throw new RuntimeException("Error al obtener el token JWT. Código de estado: " + response.getCode());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al obtener el token JWT", e);
        }
    }

    /**
     * Valida y renueva los tokens si es necesario.
     */
    private void validateAndRenewTokens() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                jwtToken = obtainJwtToken();
            }
        }
    }

    /**
     * Devuelve el token JWT válido.
     *
     * @return Token JWT.
     */
    public String getJwtToken() {
        validateAndRenewTokens();
        return jwtToken;
    }

    /**
     * Agrega encabezados de autenticación a cualquier solicitud HTTP.
     *
     * @param request Solicitud HTTP a la que se agregarán los encabezados.
     */
    public void addAuthenticationHeaders(HttpUriRequestBase request) {
        validateAndRenewTokens();
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("X-XSRF-TOKEN", csrfToken);
        request.addHeader("Content-Type", "application/json");
    }

    /**
     * Renueva manualmente la autenticación.
     */
    public void renewAuthentication() {
        synchronized (lock) {
            jwtToken = null;
            csrfToken = null;
            validateAndRenewTokens();
        }
    }

    /**
     * Verifica si el usuario está autenticado.
     *
     * @return True si el usuario está autenticado, False si no.
     */
    public boolean isAuthenticated() {
        return jwtToken != null && System.currentTimeMillis() < tokenExpirationTime;
    }
}
