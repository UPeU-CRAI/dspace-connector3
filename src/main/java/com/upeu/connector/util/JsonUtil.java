package com.upeu.connector.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class for handling JSON operations and metadata extraction.
 */
public class JsonUtil {

    /**
     * Converts a JSON string to a JSONObject.
     *
     * @param jsonString The JSON string.
     * @return A JSONObject instance.
     * @throws Exception if the string is not a valid JSON object.
     */
    public static JSONObject toJsonObject(String jsonString) throws Exception {
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            throw new Exception("Failed to parse JSON string to JSONObject: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a JSON string to a JSONArray.
     *
     * @param jsonString The JSON string.
     * @return A JSONArray instance.
     * @throws Exception if the string is not a valid JSON array.
     */
    public static JSONArray toJsonArray(String jsonString) throws Exception {
        try {
            return new JSONArray(jsonString);
        } catch (Exception e) {
            throw new Exception("Failed to parse JSON string to JSONArray: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a JSONObject to a string.
     *
     * @param jsonObject The JSONObject instance.
     * @return The JSON string.
     */
    public static String toString(JSONObject jsonObject) {
        return jsonObject.toString();
    }

    /**
     * Converts a JSONArray to a string.
     *
     * @param jsonArray The JSONArray instance.
     * @return The JSON string.
     */
    public static String toString(JSONArray jsonArray) {
        return jsonArray.toString();
    }

    /**
     * Extracts a specific metadata value from a JSONObject.
     *
     * @param metadata The JSONObject containing metadata.
     * @param key      The key for the desired metadata value.
     * @return The extracted value, or null if not found.
     */
    public static String extractMetadataValue(JSONObject metadata, String key) {
        try {
            if (metadata == null || !metadata.has(key)) {
                return null;
            }
            JSONArray valuesArray = metadata.getJSONArray(key);
            return valuesArray.length() > 0 ? valuesArray.getJSONObject(0).optString("value", null) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract metadata value for key: " + key, e);
        }
    }

    /**
     * Validates that a JSONObject has all the required keys.
     *
     * @param jsonObject   The JSONObject to validate.
     * @param requiredKeys An array of required keys.
     * @throws IllegalArgumentException if any required key is missing.
     */
    public static void validateRequiredKeys(JSONObject jsonObject, String... requiredKeys) {
        for (String key : requiredKeys) {
            if (!jsonObject.has(key)) {
                throw new IllegalArgumentException("Missing required key: " + key);
            }
        }
    }

    /**
     * Creates a metadata JSON array for a given value.
     *
     * @param value The value to include in the metadata array.
     * @return A JSONArray containing the metadata structure.
     */
    public static JSONArray createMetadataArray(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }
        return new JSONArray().put(new JSONObject().put("value", value));
    }

}
