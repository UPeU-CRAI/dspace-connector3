package com.upeu.connector.util;

import com.upeu.connector.DSpaceConfiguration;
import com.upeu.connector.auth.AuthManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

/**
 * Utility class for performing various validation tasks and handling JSON operations.
 */
public class ValidationJsonUtil {

    // ==============================
    // Validaciones generales
    // ==============================

    public static void validateId(String id, String message) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(message + " (ID was null or empty)");
        }
    }

    public static void validateRequiredFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                throw new IllegalArgumentException("Required field cannot be null or empty: " + field);
            }
        }
    }

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

    public static <T> T validateNotNull(T object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    // ==============================
    // Validaciones de configuración y autenticación
    // ==============================

    public static void validateConfiguration(DSpaceConfiguration configuration) {
        validateNotEmpty(configuration, "Configuration cannot be null.");
        if (!configuration.isInitialized()) {
            throw new IllegalStateException("Configuration is not properly initialized. Ensure all fields are set.");
        }
    }

    public static void validateAuthentication(AuthManager authManager) {
        validateNotEmpty(authManager, "AuthManager cannot be null.");
        if (!authManager.isAuthenticated()) {
            throw new IllegalStateException("AuthManager is not authenticated. Ensure valid credentials.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    // ==============================
    // Operaciones de JSON
    // ==============================

    public static JSONObject toJsonObject(String jsonString) {
        validateNotNull(jsonString, "JSON string cannot be null.");
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON string: " + e.getMessage(), e);
        }
    }

    public static JSONArray toJsonArray(String jsonString) {
        validateNotNull(jsonString, "JSON string cannot be null.");
        try {
            return new JSONArray(jsonString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON string: " + e.getMessage(), e);
        }
    }

    public static String toString(JSONObject jsonObject) {
        return jsonObject != null ? jsonObject.toString() : null;
    }

    public static String toString(JSONArray jsonArray) {
        return jsonArray != null ? jsonArray.toString() : null;
    }

    public static String extractMetadataValue(JSONObject metadata, String key) {
        validateNotNull(metadata, "Metadata cannot be null.");
        try {
            if (!metadata.has(key)) {
                return null;
            }
            JSONArray valuesArray = metadata.getJSONArray(key);
            return valuesArray.length() > 0 ? valuesArray.getJSONObject(0).optString("value", null) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract metadata value for key: " + key, e);
        }
    }

    public static JSONArray createMetadataArray(String value) {
        validateNotNull(value, "Metadata value cannot be null.");
        JSONArray metadataArray = new JSONArray();
        metadataArray.put(new JSONObject().put("value", value));
        return metadataArray;
    }
}
