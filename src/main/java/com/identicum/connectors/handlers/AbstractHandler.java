package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Clase base abstracta para los handlers.
 * Centraliza lógica común como la validación de baseUrl, ejecución de solicitudes HTTP,
 * y el parseo de respuestas.
 */
public abstract class AbstractHandler {

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
    protected CloseableHttpResponse executeRequest(HttpUriRequest request) throws IOException {
        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient()) {
            request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
            return httpClient.execute(request);
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
        String responseBody = EntityUtils.toString(response.getEntity());
        return new JSONObject(responseBody);
    }
}
