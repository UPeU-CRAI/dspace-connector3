package com.upeu.connector.handler;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.upeu.connector.DSpaceClient;

/**
 * Clase base para manejar entidades en el sistema DSpace-CRIS.
 * Proporciona funcionalidades comunes que pueden ser reutilizadas por los controladores específicos de entidades.
 */
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
        this.dSpaceClient = dSpaceClient;
    }

    /**
     * Proporciona acceso al cliente de DSpace para realizar operaciones API.
     *
     * @return Instancia de DSpaceClient.
     */
    public DSpaceClient getClient() {
        return dSpaceClient;
    }

    /**
     * Valida la estructura de una respuesta JSON para asegurarse de que contiene los campos requeridos.
     *
     * @param jsonResponse Objeto JSON que se va a validar.
     * @param requiredFields Campos que deben estar presentes en el JSON.
     * @throws RuntimeException Si algún campo requerido falta en el JSON.
     */
    protected void validateJsonResponse(JSONObject jsonResponse, String... requiredFields) {
        for (String field : requiredFields) {
            if (!jsonResponse.has(field)) {
                String errorMessage = "Falta el campo requerido: " + field;
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        }
    }

    /**
     * Maneja excepciones de la API de manera uniforme.
     *
     * @param message Mensaje de error que se registrará.
     * @param e Excepción que causó el error.
     * @throws RuntimeException Envuelve y relanza la excepción como RuntimeException.
     */
    protected void handleApiException(String message, Exception e) {
        logger.error(message, e);
        throw new RuntimeException(message, e);
    }

    /**
     * Construye una URL de endpoint con parámetros de consulta opcionales.
     *
     * @param baseEndpoint La URL base del endpoint.
     * @param queryParams Parámetros de consulta que se añadirán a la URL.
     * @return URL completa con los parámetros de consulta incluidos.
     */
    protected String constructEndpointWithParams(String baseEndpoint, String queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return baseEndpoint;
        }
        return baseEndpoint + "?" + queryParams;
    }

    /**
     * Método utilitario para registrar errores de API en el logger.
     *
     * @param message Mensaje de error que se registrará.
     * @param e Excepción asociada con el error.
     */
    protected void logError(String message, Exception e) {
        logger.error(message, e);
    }

    /**
     * Método abstracto que debe ser implementado por las clases específicas de controlador
     * para validar las entidades gestionadas.
     *
     * @param entity Entidad que se va a validar.
     * @return true si la entidad es válida, false si no lo es.
     */
    protected abstract boolean validate(Object entity);
}
