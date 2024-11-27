package com.upeu.connector.util;

import com.upeu.connector.DSpaceConfiguration;
import com.upeu.connector.auth.AuthManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class ValidationUtil {

    // ==============================
    // Validaciones de cadenas e IDs
    // ==============================

    /**
     * Valida que un ID no sea nulo o vacío.
     *
     * @param id      ID a validar.
     * @param message Mensaje de error si falla la validación.
     */
    public static void validateId(String id, String message) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Valida que los campos requeridos no sean nulos ni vacíos.
     *
     * @param fields Campos a validar.
     */
    public static void validateRequiredFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.isEmpty()) {
                throw new IllegalArgumentException("El campo requerido no puede ser nulo o vacío: " + field);
            }
        }
    }

    /**
     * Valida que un objeto no sea nulo o vacío.
     *
     * @param object  Objeto a validar.
     * @param message Mensaje de error si falla la validación.
     */
    public static void validateNotEmpty(Object object, String message) {
        if (object == null || (object instanceof String && ((String) object).isEmpty())) {
            throw new IllegalArgumentException(message);
        }
        if (object instanceof JSONObject && ((JSONObject) object).isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        if (object instanceof JSONArray && ((JSONArray) object).isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    // ==============================
    // Validaciones de configuración y autenticación
    // ==============================

    /**
     * Valida que la configuración de DSpace sea válida.
     *
     * @param configuration Configuración a validar.
     */
    public static void validateConfiguration(DSpaceConfiguration configuration) {
        validateNotEmpty(configuration, "La configuración no puede ser nula.");
        if (!configuration.isInitialized()) {
            throw new IllegalStateException("La configuración no está inicializada correctamente.");
        }
    }

    /**
     * Valida que el AuthManager esté autenticado.
     *
     * @param authManager AuthManager a validar.
     */
    public static void validateAuthentication(AuthManager authManager) {
        validateNotEmpty(authManager, "El AuthManager no puede ser nulo.");
        if (!authManager.isAuthenticated()) {
            throw new IllegalStateException("El AuthManager no está autenticado.");
        }
    }

    // ==============================
    // Validaciones específicas de negocio
    // ==============================

    /**
     * Valida que un correo electrónico tenga un formato válido.
     *
     * @param email Correo electrónico a validar.
     */
    public static void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El correo electrónico no es válido: " + email);
        }
    }

    /**
     * Valida que un objeto JSON contenga los campos requeridos.
     *
     * @param json           Objeto JSON a validar.
     * @param requiredFields Campos requeridos que deben estar presentes.
     */
    public static void validateJsonFields(JSONObject json, String... requiredFields) {
        for (String field : requiredFields) {
            if (!json.has(field)) {
                throw new IllegalArgumentException("El campo requerido falta en el JSON: " + field);
            }
        }
    }
}
