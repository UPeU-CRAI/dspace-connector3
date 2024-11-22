package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;
import com.identicum.schemas.EPersonSchema;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Handles CRUD operations for EPerson objects in DSpace-CRIS.
 * Uses EPersonSchema for data transformation.
 */
public class EPersonHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EPersonHandler.class);

    private final Endpoints endpoints;

    public EPersonHandler(AuthenticationHandler authenticationHandler, Endpoints endpoints) {
        super(authenticationHandler);
        this.endpoints = endpoints;
        LOG.info("EPersonHandler inicializado con base URL: {}", authenticationHandler.getBaseUrl());
    }

    // =====================================
    // Create EPerson
    // =====================================
    public String createEPerson(EPersonSchema ePersonSchema) throws IOException {
        LOG.info("Iniciando creación de EPerson...");
        validateSchema(ePersonSchema);

        String endpoint = endpoints.getEPersonsEndpoint();
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Enviando solicitud para crear EPerson en {}", endpoint);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                LOG.info("EPerson creado exitosamente. Código de estado: {}", statusCode);
                return parseResponseBody(response).getString("id"); // Usa parseResponseBody de AbstractHandler
            } else {
                handleErrorResponse(statusCode, "Error al crear EPerson");
                return null; // Unreachable, added for clarity.
            }
        }
    }

    // =====================================
    // Get EPerson
    // =====================================
    public EPersonSchema getEPerson(String ePersonId) throws IOException {
        LOG.info("Iniciando obtención de EPerson con ID: {}", ePersonId);
        validateId(ePersonId);

        String endpoint = endpoints.getEPersonByIdEndpoint(ePersonId);
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        LOG.info("Enviando solicitud para obtener EPerson con ID: {}", ePersonId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("EPerson obtenido exitosamente. Código de estado: {}", statusCode);
                return EPersonSchema.fromJson(parseResponseBody(response));
            } else {
                handleErrorResponse(statusCode, "Error al obtener EPerson con ID: " + ePersonId);
                return null; // Unreachable, added for clarity.
            }
        }
    }

    // =====================================
    // Update EPerson
    // =====================================
    public void updateEPerson(String ePersonId, EPersonSchema ePersonSchema) throws IOException {
        LOG.info("Iniciando actualización de EPerson con ID: {}", ePersonId);
        validateId(ePersonId);
        validateSchema(ePersonSchema);

        String endpoint = endpoints.getEPersonByIdEndpoint(ePersonId);
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        LOG.info("Enviando solicitud para actualizar EPerson con ID: {}", ePersonId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                LOG.info("EPerson actualizado exitosamente. Código de estado: {}", statusCode);
            } else {
                handleErrorResponse(statusCode, "Error al actualizar EPerson con ID: " + ePersonId);
            }
        }
    }

    // =====================================
    // Delete EPerson
    // =====================================
    public void deleteEPerson(String ePersonId) throws IOException {
        LOG.info("Iniciando eliminación de EPerson con ID: {}", ePersonId);
        validateId(ePersonId);

        String endpoint = endpoints.getEPersonByIdEndpoint(ePersonId);
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        LOG.info("Enviando solicitud para eliminar EPerson con ID: {}", ePersonId);
        try (CloseableHttpResponse response = authenticationHandler.getHttpClient().execute(request)) {
            int statusCode = response.getCode();
            if (statusCode == 204) { // No Content
                LOG.info("EPerson eliminado exitosamente. Código de estado: {}", statusCode);
            } else {
                handleErrorResponse(statusCode, "Error al eliminar EPerson con ID: " + ePersonId);
            }
        }
    }

    // ===============================
    // Helper Methods
    // ===============================
    private void validateId(String id) {
        LOG.debug("Validando ID: {}", id);
        if (id == null || id.isEmpty()) {
            LOG.error("Validación fallida: el ID no puede ser nulo o vacío.");
            throw new IllegalArgumentException("EPerson ID cannot be null or empty.");
        }
    }

    private void validateSchema(EPersonSchema schema) {
        LOG.debug("Validando esquema de EPerson...");
        if (schema == null) {
            LOG.error("Validación fallida: el esquema de EPerson no puede ser nulo.");
            throw new IllegalArgumentException("EPerson schema cannot be null.");
        }
    }

    private void handleErrorResponse(int statusCode, String errorMessage) {
        LOG.error("{} - Código HTTP: {}", errorMessage, statusCode);
        throw new RuntimeException(errorMessage + " - HTTP Status: " + statusCode);
    }
}
