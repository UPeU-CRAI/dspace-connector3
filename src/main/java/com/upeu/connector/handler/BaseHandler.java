package com.upeu.connector.handler;

import com.upeu.connector.util.ValidationUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.upeu.connector.DSpaceClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase base para los handlers de operaciones en el conector DSpace.
 * Proporciona métodos comunes para CRUD y búsqueda en la API.
 */
public abstract class BaseHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final DSpaceClient dSpaceClient;
    protected final EndpointUtil endpointUtil;

    /**
     * Constructor de BaseHandler.
     *
     * @param dSpaceClient Instancia de DSpaceClient para interactuar con la API.
     */
    protected BaseHandler(DSpaceClient dSpaceClient, EndpointUtil endpointUtil) {
        this.dSpaceClient = ValidationUtil.validateNotNull(dSpaceClient, "El DSpaceClient no puede ser nulo.");
        this.endpointUtil = ValidationUtil.validateNotNull(endpointUtil, "EndpointUtil no puede ser nulo.");
    }

    /**
     * Construye un endpoint con parámetros de consulta opcionales.
     *
     * @param baseEndpoint Relativo al endpoint base.
     * @param queryParams  Parámetros de consulta (opcional).
     * @return URL completa con los parámetros.
     */
    protected String constructEndpointWithParams(String baseEndpoint, String... queryParams) {
        String fullEndpoint = endpointUtil.buildEndpoint(baseEndpoint);

        if (queryParams != null && queryParams.length > 0) {
            fullEndpoint += "?" + String.join("&", queryParams);
        }

        return fullEndpoint;
    }

    /**
     * Valida una respuesta JSON asegurando que contenga campos obligatorios.
     *
     * @param jsonResponse Respuesta JSON.
     * @param requiredFields Campos requeridos.
     */
    protected void validateJsonResponse(JSONObject jsonResponse, String... requiredFields) {
        ValidationUtil.validateJsonFields(jsonResponse, requiredFields);
    }

    /**
     * Maneja excepciones generadas durante operaciones de API.
     *
     * @param message Mensaje descriptivo del error.
     * @param e       Excepción capturada.
     */
    protected void handleApiException(String message, Exception e) {
        logger.error(message, e);
        throw new RuntimeException(message, e);
    }

    /**
     * Realiza una operación de creación en el endpoint especificado.
     *
     * @param relativePath Relativo al endpoint base.
     * @param payload      Datos en formato JSON.
     * @return Respuesta de la API en formato JSON.
     */
    protected JSONObject create(String relativePath, JSONObject payload) {
        ValidationUtil.validateNotEmpty(payload, "El payload no puede ser nulo o vacío.");

        try {
            String response = dSpaceClient.post(relativePath, payload.toString());
            logger.info("Entidad creada exitosamente en el endpoint: {}", relativePath);
            return new JSONObject(response);
        } catch (Exception e) {
            handleApiException("Error al crear entidad en el endpoint: " + relativePath, e);
            return null;
        }
    }

    /**
     * Realiza una operación de actualización en el endpoint especificado.
     *
     * @param relativePath Relativo al endpoint base.
     * @param id           ID de la entidad a actualizar.
     * @param updates      Datos actualizados en formato JSON.
     * @return Respuesta de la API en formato JSON.
     */
    protected JSONObject update(String relativePath, String id, JSONObject updates) {
        ValidationUtil.validateId(id, "El ID no puede ser nulo o vacío.");
        ValidationUtil.validateNotEmpty(updates, "Los datos de actualización no pueden ser nulos o vacíos.");

        try {
            String fullEndpoint = constructEndpointWithParams(relativePath, "id=" + id);
            String response = dSpaceClient.put(fullEndpoint, updates.toString());
            logger.info("Entidad actualizada exitosamente en el endpoint: {}", fullEndpoint);
            return new JSONObject(response);
        } catch (Exception e) {
            handleApiException("Error al actualizar entidad en el endpoint: " + relativePath, e);
            return null;
        }
    }

    /**
     * Realiza una operación de eliminación en el endpoint especificado.
     *
     * @param relativePath Relativo al endpoint base.
     * @param id           ID de la entidad a eliminar.
     */
    protected void delete(String relativePath, String id) {
        ValidationUtil.validateId(id, "El ID no puede ser nulo o vacío.");

        try {
            String fullEndpoint = constructEndpointWithParams(relativePath, "id=" + id);
            dSpaceClient.delete(fullEndpoint);
            logger.info("Entidad eliminada exitosamente en el endpoint: {}", fullEndpoint);
        } catch (Exception e) {
            handleApiException("Error al eliminar entidad en el endpoint: " + relativePath, e);
        }
    }

    /**
     * Realiza una operación de búsqueda en el endpoint especificado.
     *
     * @param relativePath Relativo al endpoint base.
     * @param queryParams  Parámetros de consulta (opcional).
     * @return Lista de resultados en formato JSON.
     */
    public List<JSONObject> search(String relativePath, String queryParams) {
        try {
            String fullEndpoint = constructEndpointWithParams(relativePath, queryParams);
            String response = dSpaceClient.get(fullEndpoint);

            return new JSONObject(response).getJSONArray("results").toList().stream()
                    .map(obj -> new JSONObject((Map<?, ?>) obj))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            handleApiException("Error al buscar entidades en el endpoint: " + relativePath, e);
            return Collections.emptyList();
        }
    }

    /**
     * Método abstracto para validar la entidad manejada por clases específicas.
     *
     * @param entity Entidad a validar.
     * @return `true` si la entidad es válida; `false` de lo contrario.
     */
    protected abstract boolean validate(Object entity);
}
