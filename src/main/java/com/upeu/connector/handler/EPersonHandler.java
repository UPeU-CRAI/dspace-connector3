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
     * Fetch all ePersons from the DSpace-CRIS API.
     *
     * @return A list of ePersons with their key attributes.
     * @throws Exception if the request fails.
     */
    public List<EPerson> getAllEPersons() throws Exception {
        try {
            String response = client.get("/server/api/eperson/epersons");
            JSONObject jsonResponse = new JSONObject(response);

            JSONArray ePersonsArray = jsonResponse.getJSONObject("_embedded").getJSONArray("epersons");
            List<EPerson> ePersons = new ArrayList<>();

            for (int i = 0; i < ePersonsArray.length(); i++) {
                JSONObject ePersonJson = ePersonsArray.getJSONObject(i);
                ePersons.add(parseEPerson(ePersonJson));
            }

            return ePersons;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all ePersons: " + e.getMessage(), e);
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
        try {
            String response = client.get("/server/api/eperson/epersons/" + personId);
            JSONObject ePersonJson = new JSONObject(response);
            return parseEPerson(ePersonJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch ePerson by ID: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new ePerson in the DSpace-CRIS system.
     *
     * @param firstname The first name of the ePerson.
     * @param lastname  The last name of the ePerson.
     * @param email     The email address of the ePerson.
     * @param active    Whether the ePerson is active.
     * @return The created ePerson object.
     * @throws Exception if the request fails.
     */
    public EPerson createEPerson(String firstname, String lastname, String email, boolean active) throws Exception {
        try {
            JSONObject metadata = new JSONObject();
            metadata.put("eperson.firstname", createMetadataArray(firstname));
            metadata.put("eperson.lastname", createMetadataArray(lastname));
            metadata.put("eperson.email", createMetadataArray(email));
            metadata.put("eperson.active", createMetadataArray(active));

            JSONObject requestBody = new JSONObject();
            requestBody.put("metadata", metadata);

            String response = client.post("/server/api/eperson/epersons", requestBody.toString());
            JSONObject ePersonJson = new JSONObject(response);

            return parseEPerson(ePersonJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ePerson: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing ePerson's information.
     *
     * @param personId  The ID of the ePerson to update.
     * @param updates   A JSON object containing the fields to update.
     * @return The updated ePerson object.
     * @throws Exception if the request fails.
     */
    public EPerson updateEPerson(String personId, JSONObject updates) throws Exception {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("metadata", updates);

            String response = client.put("/server/api/eperson/epersons/" + personId, requestBody.toString());
            JSONObject ePersonJson = new JSONObject(response);

            return parseEPerson(ePersonJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ePerson: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an ePerson by their ID.
     *
     * @param personId The ID of the ePerson to delete.
     * @throws Exception if the request fails.
     */
    public void deleteEPerson(String personId) throws Exception {
        try {
            client.delete("/server/api/eperson/epersons/" + personId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete ePerson: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a JSON object representing an ePerson into a Java object.
     *
     * @param ePersonJson The JSON object of the ePerson.
     * @return The parsed ePerson object.
     */
    private EPerson parseEPerson(JSONObject ePersonJson) {
        try {
            String id = ePersonJson.getString("id");
            String email = ePersonJson.getString("email");
            boolean canLogIn = ePersonJson.getBoolean("canLogIn");

            String firstName = ePersonJson.getJSONObject("metadata")
                    .getJSONArray("eperson.firstname").getJSONObject(0).getString("value");

            String lastName = ePersonJson.getJSONObject("metadata")
                    .getJSONArray("eperson.lastname").getJSONObject(0).getString("value");

            return new EPerson(id, email, firstName, lastName, canLogIn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse ePerson JSON: " + e.getMessage(), e);
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
