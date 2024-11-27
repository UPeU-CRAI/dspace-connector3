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

public abstract class BaseHandler {

    // Logger para registrar mensajes de depuración y errores
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // Cliente para interactuar con la API de DSpace
    protected final DSpaceClient dSpaceClient;

    /**
     * Constructor de la clase BaseHandler.
     *
     * @param dSpaceClient Instancia de DSpaceClient para realizar solicitudes a la API.
     */
    protected BaseHandler(DSpaceClient dSpaceClient) {
        if (dSpaceClient == null) {
            throw new IllegalArgumentException("El DSpaceClient no puede ser nulo.");
        }
        this.dSpaceClient = dSpaceClient;
    }

    protected String buildEndpoint(String baseEndpoint, String... queryParams) {
        StringBuilder fullEndpoint = new StringBuilder(baseEndpoint);

        if (queryParams != null && queryParams.length > 0) {
            fullEndpoint.append("?");
            for (String param : queryParams) {
                fullEndpoint.append(param).append("&");
            }
            fullEndpoint.setLength(fullEndpoint.length() - 1); // Remover el último "&"
        }

        return fullEndpoint.toString();
    }

    // Proporciona acceso al cliente de DSpace para realizar operaciones API.
    public DSpaceClient getClient() {
        return dSpaceClient;
    }

    //Valida la estructura de una respuesta JSON para asegurarse de que contiene los campos requeridos.
    protected void validateJsonResponse(JSONObject jsonResponse, String... requiredFields) {
        for (String field : requiredFields) {
            if (!jsonResponse.has(field)) {
                String errorMessage = "Falta el campo requerido: " + field;
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        }
    }

    // Maneja excepciones de la API de manera uniforme.
    protected void handleApiException(String message, Exception e) {
        logger.error(message, e);
        throw new RuntimeException(message, e);
    }

    // Construye una URL de endpoint con parámetros de consulta opcionales.
    protected String constructEndpointWithParams(String baseEndpoint, String queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return baseEndpoint;
        }
        return baseEndpoint + "?" + queryParams;
    }

    // Método utilitario para registrar errores de API en el logger.
    protected void logError(String message, Exception e) {
        logger.error(message, e);
    }

    // Método abstracto que debe ser implementado por las clases específicas de controlador
    protected abstract boolean validate(Object entity);

    /**
     * Realiza una actualización de una entidad en el sistema DSpace-CRIS.
     *
     * @param endpoint La URL base del endpoint (por ejemplo, "/epersons").
     * @param id El ID de la entidad a actualizar.
     * @param updates Objeto JSON con los datos actualizados.
     * @return Objeto JSON con la respuesta del servidor.
     */
    protected JSONObject update(String endpoint, String id, JSONObject updates) {
        // Validar que el ID no sea nulo o vacío
        ValidationUtil.validateId(id, "El ID no puede ser nulo o vacío.");

        try {
            // Construir el endpoint usando buildEndpoint
            String fullEndpoint = buildEndpoint(endpoint, "id=" + id);

            // Enviar la solicitud PUT y convertir la respuesta en JSONObject
            String response = dSpaceClient.put(fullEndpoint, updates.toString());
            return new JSONObject(response);
        } catch (Exception e) {
            // Manejar excepciones de forma uniforme
            handleApiException("Error al actualizar la entidad en el endpoint: " + endpoint, e);
            return null; // Este punto no se alcanza debido al throw
        }
    }

    /**
     * Realiza la eliminación de una entidad en el sistema DSpace-CRIS.
     *
     * @param endpoint La URL base del endpoint (por ejemplo, "/epersons").
     * @param id El ID de la entidad a eliminar.
     */
    protected void delete(String endpoint, String id) {
        // Validar que el ID no sea nulo o vacío
        ValidationUtil.validateId(id, "El ID no puede ser nulo o vacío.");

        try {
            // Construir el endpoint usando buildEndpoint
            String fullEndpoint = buildEndpoint(endpoint, "id=" + id);

            // Enviar la solicitud DELETE
            dSpaceClient.delete(fullEndpoint);

            // Registro de éxito
            logger.info("Entidad eliminada exitosamente en el endpoint: " + fullEndpoint);
        } catch (Exception e) {
            // Manejar excepciones de forma uniforme
            handleApiException("Error al eliminar la entidad en el endpoint: " + endpoint, e);
        }
    }

    protected JSONObject create(String endpoint, JSONObject payload) {
        // Validar que el payload no sea nulo o vacío
        ValidationUtil.validateNotEmpty(payload, "El payload no puede ser nulo o vacío.");

        try {
            // Enviar la solicitud POST al endpoint
            String response = dSpaceClient.post(endpoint, payload.toString());

            // Log de éxito
            logger.info("Entidad creada exitosamente en el endpoint: " + endpoint);

            // Retornar la respuesta como JSONObject
            return new JSONObject(response);
        } catch (Exception e) {
            // Manejar excepciones uniformemente
            handleApiException("Error al crear la entidad en el endpoint: " + endpoint, e);
            return null; // Este punto no se alcanza debido al throw
        }
    }


    /**
     * Realiza una búsqueda de entidades en el sistema DSpace-CRIS.
     *
     * @param endpoint La URL base del endpoint (por ejemplo, "/epersons").
     * @param queryParams Parámetros de consulta opcionales (puede ser nulo).
     * @return Lista de resultados en formato JSON.
     */
    public List<JSONObject> search(String endpoint, String queryParams) {
        try {
            // Construir la URL completa del endpoint con los parámetros de consulta
            String fullEndpoint = constructEndpointWithParams(endpoint, queryParams);

            // Enviar la solicitud GET
            String response = dSpaceClient.get(fullEndpoint);

            // Convertir la respuesta en una lista de objetos JSON
            return new JSONObject(response).getJSONArray("results")
                    .toList()
                    .stream()
                    .map(obj -> new JSONObject((Map<?, ?>) obj))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            handleApiException("Error al buscar entidades en el endpoint: " + endpoint, e);
            return Collections.emptyList(); // En caso de error, retornar una lista vacía
        }
    }

}
