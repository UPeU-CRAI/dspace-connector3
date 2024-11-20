package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Clase base abstracta para los handlers.
 * Centraliza lógica común como la validación de baseUrl, ejecución de solicitudes HTTP,
 * y el parseo de respuestas.
 */
public abstract class AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractHandler.class);

    protected final AuthenticationHandler authenticationHandler;
    protected final String baseUrl;

    /**
     * Constructor del handler abstracto.
     *
     * @param authenticationHandler Handler de autenticación configurado.
     */
    public AbstractHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
        this.baseUrl = authenticationHandler.getBaseUrl();

        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            LOG.error("El baseUrl no está configurado. Por favor inicializa el conector correctamente.");
            throw new IllegalStateException("El baseUrl no está configurado. Por favor inicializa el conector correctamente.");
        }
    }

    /**
     * Ejecuta una solicitud HTTP y devuelve la respuesta.
     *
     * @param request Solicitud HTTP a ejecutar.
     * @return Respuesta de la solicitud.
     * @throws IOException Si ocurre un error de red.
     */
    protected CloseableHttpResponse executeRequest(HttpUriRequest request) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient()) {
            // Agregar el token de autenticación a la solicitud
            request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
            LOG.info("Ejecutando solicitud HTTP: {} {}", request.getMethod(), request.getUri());

            CloseableHttpResponse response = httpClient.execute(request);
            LOG.info("Respuesta recibida: {} {}", response.getCode(), response.getReasonPhrase());
            return response;
        } catch (IOException | URISyntaxException e) {
            LOG.error("Error al ejecutar la solicitud HTTP: {} {}", request.getMethod(), request.getUri(), e);
            throw e;
        }
    }

    /**
     * Parsea el cuerpo de la respuesta HTTP a un JSONObject.
     *
     * @param response Respuesta HTTP a parsear.
     * @return JSONObject representando el cuerpo de la respuesta.
     * @throws IOException    Si ocurre un error al leer la respuesta.
     * @throws ParseException Si ocurre un error al parsear la respuesta.
     */
    protected JSONObject parseResponseBody(CloseableHttpResponse response) throws IOException, ParseException {
        try {
            String responseBody = EntityUtils.toString(response.getEntity());
            LOG.debug("Cuerpo de la respuesta: {}", responseBody);
            return new JSONObject(responseBody);
        } catch (IOException | ParseException e) {
            LOG.error("Error al parsear el cuerpo de la respuesta", e);
            throw e;
        }
    }

    /**
     * Verifica si el estado HTTP de la respuesta está dentro del rango esperado.
     *
     * @param response   Respuesta HTTP.
     * @param expectedStatus Código de estado esperado.
     * @throws RuntimeException Si el estado HTTP no coincide con el esperado.
     */
    protected void validateHttpResponseStatus(CloseableHttpResponse response, int expectedStatus) {
        int actualStatus = response.getCode();
        if (actualStatus != expectedStatus) {
            LOG.error("Estado HTTP inesperado: esperado {}, recibido {}", expectedStatus, actualStatus);
            throw new RuntimeException("HTTP Status inesperado: esperado " + expectedStatus + ", recibido " + actualStatus);
        }
    }
}
