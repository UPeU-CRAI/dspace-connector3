package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for managing ePerson operations in DSpace-CRIS.
 */
public class EPersonHandler {

    private final DSpaceClient client;

    public EPersonHandler(DSpaceClient client) {
        this.client = client;
    }

    /**
     * Fetch all ePersons from the DSpace-CRIS API with pagination.
     *
     * @param page  Page number for pagination.
     * @param size  Number of records per page.
     * @return A list of ePersons with their attributes.
     * @throws Exception if the request fails.
     */
    public List<EPerson> getAllEPersons(int page, int size) throws Exception {
        String endpoint = "/server/api/eperson/epersons?page=" + page + "&size=" + size;
        return fetchEPersons(endpoint);
    }

    /**
     * Fetch ePersons from a specific endpoint.
     *
     * @param endpoint API endpoint to fetch ePersons.
     * @return A list of ePersons parsed from the API response.
     * @throws Exception if the request fails.
     */
    private List<EPerson> fetchEPersons(String endpoint) throws Exception {
        try {
            String response = client.get(endpoint);
            JSONObject jsonResponse = new JSONObject(response);

            if (!jsonResponse.has("_embedded")) {
                throw new RuntimeException("Response is missing '_embedded' field");
            }

            JSONArray ePersonsArray = jsonResponse.getJSONObject("_embedded").optJSONArray("epersons");
            if (ePersonsArray == null) {
                throw new RuntimeException("Response is missing 'epersons' array");
            }

            List<EPerson> ePersons = new ArrayList<>();
            for (int i = 0; i < ePersonsArray.length(); i++) {
                JSONObject ePersonJson = ePersonsArray.getJSONObject(i);
                ePersons.add(parseEPerson(ePersonJson));
            }

            return ePersons;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch ePersons: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch details of a specific ePerson by their ID.
     *
     * @param personId The ID of the ePerson.
     * @return The ePerson object with its details.
     * @throws Exception if the request fails.
     */
    public EPerson getEPersonById(String personId) throws Exception {
        if (personId == null || personId.isEmpty()) {
            throw new IllegalArgumentException("Person ID cannot be null or empty");
        }

        try {
            String response = client.get("/server/api/eperson/epersons/" + personId);
            JSONObject ePersonJson = new JSONObject(response);
            return parseEPerson(ePersonJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch ePerson by ID: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a JSON object representing an ePerson into a Java object.
     *
     * @param ePersonJson The JSON object of the ePerson.
     * @return The parsed ePerson object.
     * @throws RuntimeException if the JSON structure is invalid.
     */
    private EPerson parseEPerson(JSONObject ePersonJson) {
        try {
            if (!ePersonJson.has("id") || !ePersonJson.has("metadata")) {
                throw new RuntimeException("Invalid ePerson JSON structure: missing 'id' or 'metadata'");
            }

            String id = ePersonJson.getString("id");
            String email = ePersonJson.optString("email", null);
            boolean canLogIn = ePersonJson.optBoolean("canLogIn", false);

            JSONObject metadata = ePersonJson.getJSONObject("metadata");
            String firstName = extractMetadataValue(metadata, "eperson.firstname");
            String lastName = extractMetadataValue(metadata, "eperson.lastname");

            return new EPerson(id, email, firstName, lastName, canLogIn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ePerson JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts a single metadata value from a metadata JSON object.
     *
     * @param metadata The metadata JSON object.
     * @param key      The key to extract the value for.
     * @return The extracted value, or null if not found.
     */
    private String extractMetadataValue(JSONObject metadata, String key) {
        try {
            if (!metadata.has(key)) {
                return null;
            }

            JSONArray valuesArray = metadata.getJSONArray(key);
            if (valuesArray.isEmpty()) {
                return null;
            }

            return valuesArray.getJSONObject(0).optString("value", null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract metadata value for key '" + key + "': " + e.getMessage(), e);
        }
    }

    /**
     * Utility method to create a metadata JSON array.
     *
     * @param value The value to add to the metadata.
     * @return A JSON array with the metadata value.
     */
    private JSONArray createMetadataArray(Object value) {
        return new JSONArray().put(new JSONObject().put("value", value));
    }
}
