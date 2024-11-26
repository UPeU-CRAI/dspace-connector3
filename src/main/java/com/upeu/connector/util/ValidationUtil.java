package com.upeu.connector.util;

import org.json.JSONObject;

public class ValidationUtil {

    public static void validateId(String id, String message) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validateRequiredFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.isEmpty()) {
                throw new IllegalArgumentException("Field cannot be null or empty: " + field);
            }
        }
    }

    /**
     * Validates that a given object is not null or empty.
     *
     * @param object  The object to validate.
     * @param message The error message to throw if validation fails.
     */
    public static void validateNotEmpty(Object object, String message) {
        if (object == null || (object instanceof String && ((String) object).isEmpty())) {
            throw new IllegalArgumentException(message);
        }

        if (object instanceof JSONObject && ((JSONObject) object).isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        // Puedes extender para otros tipos según lo necesario.
    }

}
