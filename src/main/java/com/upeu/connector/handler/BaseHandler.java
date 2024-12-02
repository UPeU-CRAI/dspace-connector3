package com.upeu.connector.handler;

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

    /**
     * Constructor de BaseHandler.
     *
     * @param dSpaceClient Instancia de DSpaceClient para interactuar con la API.
     */
    protected BaseHandler(DSpaceClient dSpaceClient) {
        this.dSpaceClient = dSpaceClient;
    }

    /**
     * Realiza una operación genérica de creación.
     *
     * @param endpointKey Clave del endpoint.
     * @param payload     Datos en formato JSON.
     * @return Respuesta de la API en formato JSON.
     */
    public JSONObject create(String endpointKey, JSONObject payload) {
        try {
            String response = dSpaceClient.post(endpointKey, payload.toString());
            logger.info("Entidad creada exitosamente en: {}", endpointKey);
            return new JSONObject(response);
        } catch (Exception e) {
            logger.error("Error al crear en {}: {}", endpointKey, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Realiza una operación genérica de actualización.
     *
     * @param endpointKey Clave del endpoint.
     * @param id          ID de la entidad.
     * @param updates     Datos actualizados en formato JSON.
     * @return Respuesta de la API en formato JSON.
     */
    public JSONObject update(String endpointKey, String id, JSONObject updates) {
        try {
            String fullEndpoint = constructEndpointWithParams(endpointKey, "id=" + id);
            String response = dSpaceClient.put(fullEndpoint, updates.toString());
            logger.info("Entidad actualizada exitosamente en: {}", fullEndpoint);
            return new JSONObject(response);
        } catch (Exception e) {
            logger.error("Error al actualizar en {}: {}", endpointKey, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Realiza una operación genérica de eliminación.
     *
     * @param endpointKey Clave del endpoint.
     * @param id          ID de la entidad.
     */
    public void delete(String endpointKey, String id) {
        try {
            String fullEndpoint = constructEndpointWithParams(endpointKey, "id=" + id);
            dSpaceClient.delete(fullEndpoint);
            logger.info("Entidad eliminada en: {}", fullEndpoint);
        } catch (Exception e) {
            logger.error("Error al eliminar en {}: {}", endpointKey, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Construye un endpoint con parámetros de consulta opcionales.
     */
    protected String constructEndpointWithParams(String endpointKey, String... queryParams) {
        String fullEndpoint = dSpaceClient.getAuthManager().buildEndpoint(endpointKey);
        if (queryParams != null && queryParams.length > 0) {
            fullEndpoint += "?" + String.join("&", queryParams);
        }
        return fullEndpoint;
    }

    /**
     * Método abstracto para validar entidades.
     */
    protected abstract boolean validate(Object entity);
}
