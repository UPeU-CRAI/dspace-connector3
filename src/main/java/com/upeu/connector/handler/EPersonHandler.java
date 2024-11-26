package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import com.upeu.connector.filter.EPersonFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for managing ePerson operations in DSpace-CRIS.
 */
public class EPersonHandler {

    private static final Logger LOGGER = Logger.getLogger(EPersonHandler.class.getName());
    private final DSpaceClient client;
    private final EPersonFilterTranslator filterTranslator;

    public EPersonHandler(DSpaceClient client) {
        this.client = client;
        this.filterTranslator = new EPersonFilterTranslator();
    }

    /**
     * Fetch details of a specific ePerson by their ID.
     */
    public EPerson getEPersonById(String personId) throws Exception {
        validateId(personId, "Person ID cannot be null or empty");
        try {
            String response = client.get("/server/api/eperson/epersons/" + personId);
            return parseEPerson(new JSONObject(response));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch ePerson by ID {0}: {1}", new Object[]{personId, e.getMessage()});
            throw e;
        }
    }

    /**
     * Create a new ePerson in the DSpace-CRIS system.
     */
    public EPerson createEPerson(String firstname, String lastname, String email, boolean canLogIn) throws Exception {
        validateRequiredFields(firstname, lastname, email);
        try {
            JSONObject metadata = new JSONObject();
            metadata.put("eperson.firstname", createMetadataArray(firstname));
            metadata.put("eperson.lastname", createMetadataArray(lastname));
            metadata.put("eperson.email", createMetadataArray(email));

            JSONObject requestBody = new JSONObject();
            requestBody.put("metadata", metadata);
            requestBody.put("canLogIn", canLogIn);

            String response = client.post("/server/api/eperson/epersons", requestBody.toString());
            return parseEPerson(new JSONObject(response));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create ePerson: {0}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing ePerson's information.
     */
    public EPerson updateEPerson(String id, JSONObject updates) throws Exception {
        validateId(id, "ePerson ID is required for update");
        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException("Updates cannot be null or empty");
        }
        try {
            String response = client.put("/server/api/eperson/epersons/" + id, updates.toString());
            return parseEPerson(new JSONObject(response));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update ePerson with ID {0}: {1}", new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Delete an ePerson by their ID.
     */
    public void deleteEPerson(String id) throws Exception {
        validateId(id, "ePerson ID is required for deletion");
        try {
            client.delete("/server/api/eperson/epersons/" + id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete ePerson with ID {0}: {1}", new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Fetch ePersons with optional filtering from Midpoint.
     */
    public List<EPerson> getEPersons(Filter filter) throws Exception {
        StringBuilder endpoint = new StringBuilder("/server/api/eperson/epersons");

        if (filter != null) {
            List<String> queryParams = filterTranslator.translate(filter);
            if (!queryParams.isEmpty()) {
                endpoint.append(queryParams.get(0));
            }
        }

        return fetchEPersons(endpoint.toString());
    }

    /**
     * Fetch ePersons from a specific endpoint.
     */
    private List<EPerson> fetchEPersons(String endpoint) throws Exception {
        List<EPerson> ePersons = new ArrayList<>();
        try {
            String response = client.get(endpoint);
            JSONObject jsonResponse = new JSONObject(response);

            if (!jsonResponse.has("_embedded") || !jsonResponse.getJSONObject("_embedded").has("epersons")) {
                LOGGER.warning("Response is missing '_embedded.epersons' field");
                return ePersons;
            }

            JSONArray ePersonsArray = jsonResponse.getJSONObject("_embedded").getJSONArray("epersons");
            for (int i = 0; i < ePersonsArray.length(); i++) {
                JSONObject ePersonJson = ePersonsArray.getJSONObject(i);
                ePersons.add(parseEPerson(ePersonJson));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch ePersons from endpoint {0}: {1}", new Object[]{endpoint, e.getMessage()});
            throw e;
        }
        return ePersons;
    }

    /**
     * Parses a JSON object representing an ePerson into a Java object.
     */
    private EPerson parseEPerson(JSONObject ePersonJson) {
        try {
            String id = ePersonJson.optString("id", null);
            String email = ePersonJson.optString("email", null);
            boolean canLogIn = ePersonJson.optBoolean("canLogIn", false);

            JSONObject metadata = ePersonJson.optJSONObject("metadata");
            String firstName = extractMetadataValue(metadata, "eperson.firstname");
            String lastName = extractMetadataValue(metadata, "eperson.lastname");

            return new EPerson(id, email, firstName, lastName, canLogIn);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse ePerson JSON: {0}", e.getMessage());
            throw new RuntimeException("Invalid ePerson JSON structure", e);
        }
    }

    /**
     * Utility to validate required fields.
     */
    private void validateRequiredFields(String firstname, String lastname, String email) {
        if (firstname == null || firstname.isEmpty() || lastname == null || lastname.isEmpty() || email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Firstname, lastname, and email are mandatory");
        }
    }

    /**
     * Utility to validate ID.
     */
    private void validateId(String id, String message) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Utility method to create a metadata JSON array.
     */
    private JSONArray createMetadataArray(String value) {
        return new JSONArray().put(new JSONObject().put("value", value));
    }

    /**
     * Extracts a single metadata value from a metadata JSON object.
     */
    private String extractMetadataValue(JSONObject metadata, String key) {
        try {
            if (metadata == null || !metadata.has(key)) {
                return null;
            }
            JSONArray valuesArray = metadata.getJSONArray(key);
            return valuesArray.length() > 0 ? valuesArray.getJSONObject(0).optString("value", null) : null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to extract metadata value for key {0}: {1}", new Object[]{key, e.getMessage()});
            throw new RuntimeException(e);
        }
    }
}
