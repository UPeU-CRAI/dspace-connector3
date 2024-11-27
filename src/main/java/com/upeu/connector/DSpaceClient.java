package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.util.HttpUtil;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;

/**
 * DSpaceClient handles API communication with DSpace-CRIS.
 */
public class DSpaceClient {

    private final DSpaceConfiguration config; // Configuración del cliente
    private final HttpUtil httpUtil;         // Utilidad HTTP para manejar solicitudes

    public DSpaceClient(DSpaceConfiguration config, AuthManager authManager) {
        // Validar los parámetros de entrada
        if (config == null) {
            throw new IllegalArgumentException("La configuración no puede ser nula.");
        }
        if (authManager == null) {
            throw new IllegalArgumentException("AuthManager no puede ser nulo.");
        }

        this.config = config;

        // Configurar el cliente HTTP
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                        .setResponseTimeout(Timeout.ofMilliseconds(config.getReadTimeout()))
                        .build())
                .build();

        this.httpUtil = new HttpUtil(authManager, httpClient);
    }

    /**
     * Realiza una solicitud GET al endpoint especificado.
     * @param endpoint El endpoint relativo (ejemplo: "/epersons").
     * @return Respuesta en formato JSON como String.
     * @throws Exception Si ocurre algún error durante la solicitud.
     */
    public String get(String endpoint) throws Exception {
        validateEndpoint(endpoint);
        return httpUtil.get(buildUrl(endpoint));
    }

    /**
     * Realiza una solicitud POST al endpoint especificado.
     * @param endpoint El endpoint relativo (ejemplo: "/epersons").
     * @param body El cuerpo de la solicitud en formato JSON.
     * @return Respuesta en formato JSON como String.
     * @throws Exception Si ocurre algún error durante la solicitud.
     */
    public String post(String endpoint, String body) throws Exception {
        validateEndpoint(endpoint);
        validateBody(body);
        return httpUtil.post(buildUrl(endpoint), body);
    }

    /**
     * Realiza una solicitud PUT al endpoint especificado.
     * @param endpoint El endpoint relativo (ejemplo: "/epersons/{id}").
     * @param body El cuerpo de la solicitud en formato JSON.
     * @return Respuesta en formato JSON como String.
     * @throws Exception Si ocurre algún error durante la solicitud.
     */
    public String put(String endpoint, String body) throws Exception {
        validateEndpoint(endpoint);
        validateBody(body);
        return httpUtil.put(buildUrl(endpoint), body);
    }

    /**
     * Realiza una solicitud DELETE al endpoint especificado.
     * @param endpoint El endpoint relativo (ejemplo: "/epersons/{id}").
     * @throws Exception Si ocurre algún error durante la solicitud.
     */
    public void delete(String endpoint) throws Exception {
        validateEndpoint(endpoint);
        httpUtil.delete(buildUrl(endpoint));
    }

    /**
     * Construye la URL completa combinando la URL base con el endpoint relativo.
     * @param endpoint El endpoint relativo.
     * @return URL completa como String.
     */
    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("/")) {
            return config.getBaseUrl().endsWith("/") ?
                    config.getBaseUrl() + endpoint.substring(1) :
                    config.getBaseUrl() + endpoint;
        }
        return config.getBaseUrl().endsWith("/") ?
                config.getBaseUrl() + endpoint :
                config.getBaseUrl() + "/" + endpoint;
    }

    /**
     * Valida que el endpoint no sea nulo ni vacío.
     * @param endpoint El endpoint a validar.
     */
    private void validateEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("El endpoint no puede ser nulo ni vacío.");
        }
    }

    /**
     * Valida que el cuerpo de la solicitud no sea nulo ni vacío.
     * @param body El cuerpo de la solicitud a validar.
     */
    private void validateBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("El cuerpo de la solicitud no puede ser nulo ni vacío.");
        }
    }
}
