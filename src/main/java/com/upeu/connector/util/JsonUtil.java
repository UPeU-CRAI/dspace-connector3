package com.upeu.connector.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class for handling JSON operations.
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
}
