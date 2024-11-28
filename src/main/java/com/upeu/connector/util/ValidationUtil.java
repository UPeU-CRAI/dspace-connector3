package com.upeu.connector.util;

import com.upeu.connector.DSpaceConfiguration;
import com.upeu.connector.auth.AuthManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

/**
 * Utility class for performing various validation tasks.
 */
public class ValidationUtil {

    // ==============================
    // Validaciones de cadenas e IDs
    // ==============================

    /**
     * Validates that an ID is not null or empty.
     *
     * @param id      ID to validate.
     * @param message Error message if validation fails.
     */
    public static void validateId(String id, String message) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(message + " (ID was null or empty)");
        }
    }

    /**
     * Validates that required fields are not null or empty.
     *
     * @param fields Fields to validate.
     */
    public static void validateRequiredFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                throw new IllegalArgumentException("Required field cannot be null or empty: " + field);
            }
        }
    }

    /**
     * Validates that an object is not null or empty.
     *
     * @param object  Object to validate.
     * @param message Error message if validation fails.
     */
    public static void validateNotEmpty(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message + " (Object was null)");
        }
        if (object instanceof String && ((String) object).trim().isEmpty()) {
            throw new IllegalArgumentException(message + " (String was empty)");
        }
        if (object instanceof Collection && ((Collection<?>) object).isEmpty()) {
            throw new IllegalArgumentException(message + " (Collection was empty)");
        }
        if (object instanceof Map && ((Map<?, ?>) object).isEmpty()) {
            throw new IllegalArgumentException(message + " (Map was empty)");
        }
        if (object instanceof JSONObject && ((JSONObject) object).isEmpty()) {
            throw new IllegalArgumentException(message + " (JSON Object was empty)");
        }
        if (object instanceof JSONArray && ((JSONArray) object).isEmpty()) {
            throw new IllegalArgumentException(message + " (JSON Array was empty)");
        }
    }

    // ==============================
    // Validaciones de configuración y autenticación
    // ==============================

    /**
     * Validates that the DSpace configuration is valid.
     *
     * @param configuration Configuration to validate.
     */
    public static void validateConfiguration(DSpaceConfiguration configuration) {
        validateNotEmpty(configuration, "Configuration cannot be null.");
        if (!configuration.isInitialized()) {
            throw new IllegalStateException("Configuration is not properly initialized.");
        }
    }

    /**
     * Validates that the AuthManager is authenticated.
     *
     * @param authManager AuthManager to validate.
     */
    public static void validateAuthentication(AuthManager authManager) {
        validateNotEmpty(authManager, "AuthManager cannot be null.");
        if (!authManager.isAuthenticated()) {
            throw new IllegalStateException("AuthManager is not authenticated.");
        }
    }

    // ==============================
    // Validaciones específicas de negocio
    // ==============================

    /**
     * Validates that an email address is in a valid format.
     *
     * @param email Email address to validate.
     */
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    /**
     * Validates that a JSONObject contains the required fields.
     *
     * @param json           JSONObject to validate.
     * @param requiredFields Required fields that must be present.
     */
    public static void validateJsonFields(JSONObject json, String... requiredFields) {
        for (String field : requiredFields) {
            if (!json.has(field)) {
                throw new IllegalArgumentException("Required field missing in JSON: " + field);
            }
        }
    }

    // ==============================
    // Validaciones de números
    // ==============================

    /**
     * Validates that a number is positive.
     *
     * @param number Number to validate.
     * @param message Error message if validation fails.
     */
    public static void validatePositiveNumber(Number number, String message) {
        if (number == null || number.doubleValue() <= 0) {
            throw new IllegalArgumentException(message + " (Number was not positive: " + number + ")");
        }
    }

    /**
     * Validates that a number falls within a specified range.
     *
     * @param number Number to validate.
     * @param min Minimum acceptable value (inclusive).
     * @param max Maximum acceptable value (inclusive).
     * @param message Error message if validation fails.
     */
    public static void validateNumberInRange(Number number, double min, double max, String message) {
        if (number == null || number.doubleValue() < min || number.doubleValue() > max) {
            throw new IllegalArgumentException(
                    message + " (Number out of range: " + number + ", expected: [" + min + ", " + max + "])"
            );
        }
    }
}
