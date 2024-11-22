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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationHandler {

    private final DSpaceConnectorConfiguration config;
    private final BasicCookieStore cookieStore;
    private final CloseableHttpClient httpClient;

    private String jwtToken;
    private long tokenExpirationTime;
    private final Object lock = new Object();

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationHandler.class);

    public AuthenticationHandler(DSpaceConnectorConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("La configuración no puede ser nula.");
        }
        this.config = config;
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        LOG.info("AuthenticationHandler inicializado correctamente.");
    }

    private String obtainCsrfToken() {
        String endpoint = config.getBaseUrl() + "/server/api/authn/status";
        HttpGet request = new HttpGet(endpoint);
        LOG.info("Intentando obtener token CSRF desde el endpoint: {}", endpoint);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            LOG.info("Respuesta recibida al intentar obtener token CSRF. Código de estado: {}", response.getCode());

            if (response.getCode() == 200) {
                String csrfToken = cookieStore.getCookies().stream()
                        .filter(cookie -> "DSPACE-XSRF-COOKIE".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElseThrow(() -> {
                            LOG.error("No se encontró el token CSRF en las cookies de la respuesta.");
                            return new RuntimeException("Token CSRF no encontrado en cookies.");
                        });

                LOG.info("Token CSRF obtenido exitosamente: {}", csrfToken);
                return csrfToken;
            } else {
                LOG.error("Fallo al obtener token CSRF. Código de estado: {}", response.getCode());
                throw new RuntimeException("Fallo al obtener token CSRF. Código de estado: " + response.getCode());
            }
        } catch (IOException e) {
            LOG.error("Error al intentar obtener el token CSRF: {}", e.getMessage(), e);
            throw new RuntimeException("Error obteniendo token CSRF", e);
        }
    }

    private String obtainJwtToken() {
        String csrfToken = obtainCsrfToken();
        String endpoint = config.getBaseUrl() + "/server/api/authn/login";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setHeader("X-XSRF-TOKEN", csrfToken);

        LOG.info("Intentando obtener token JWT desde el endpoint: {}", endpoint);

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("user", config.getUsername()));
        params.add(new BasicNameValuePair("password", extractPassword(config.getPassword())));
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            LOG.info("Respuesta recibida al intentar obtener token JWT. Código de estado: {}", response.getCode());

            if (response.getCode() == 200) {
                var authHeader = response.getFirstHeader("Authorization");
                if (authHeader != null && authHeader.getValue().startsWith("Bearer ")) {
                    jwtToken = authHeader.getValue().substring(7);
                    tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000;
                    LOG.info("Token JWT obtenido exitosamente. Expira en 1 hora.");
                    return jwtToken;
                } else {
                    LOG.error("El encabezado de autorización está ausente o no es válido.");
                    throw new RuntimeException("Encabezado de autorización ausente o inválido.");
                }
            } else {
                LOG.error("Fallo al obtener token JWT. Código de estado: {}", response.getCode());
                throw new RuntimeException("Fallo al obtener token JWT. Código de estado: " + response.getCode());
            }
        } catch (IOException e) {
            LOG.error("Error al intentar obtener el token JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Error obteniendo token JWT", e);
        }
    }

    public String getJwtToken() {
        synchronized (lock) {
            if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                LOG.info("Token JWT no encontrado o expirado. Intentando obtener un nuevo token.");
                jwtToken = obtainJwtToken();
            } else {
                LOG.debug("Token JWT válido encontrado en caché.");
            }
            return jwtToken;
        }
    }

    private String extractPassword(GuardedString guardedPassword) {
        final StringBuilder password = new StringBuilder();
        guardedPassword.access(chars -> password.append(new String(chars)));
        LOG.debug("Password extraído de GuardedString.");
        return password.toString();
    }

    public void testConnection() {
        String endpoint = config.getBaseUrl() + "/server/api/authn/status";
        HttpGet request = new HttpGet(endpoint);
        LOG.info("Probando conexión al servidor DSpace en el endpoint: {}", endpoint);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            LOG.info("Respuesta recibida al probar conexión. Código de estado: {}", response.getCode());
            if (response.getCode() != 200) {
                LOG.error("Conexión de prueba fallida. Código de estado: {}", response.getCode());
                throw new RuntimeException("Fallo en la conexión de prueba. Código de estado: " + response.getCode());
            }
            LOG.info("Conexión de prueba exitosa.");
        } catch (IOException e) {
            LOG.error("Error al probar la conexión al servidor DSpace: {}", e.getMessage(), e);
            throw new RuntimeException("Error probando conexión al servidor DSpace", e);
        }
    }

    public CloseableHttpClient getHttpClient() {
        LOG.debug("Obteniendo instancia del cliente HTTP.");
        return httpClient;
    }

    public String getBaseUrl() {
        LOG.debug("Obteniendo la URL base: {}", config.getBaseUrl());
        return config.getBaseUrl();
    }
}
