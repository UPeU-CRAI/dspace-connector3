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
        String response = client.get("/server/api/eperson/epersons");
        JSONObject jsonResponse = new JSONObject(response);

        JSONArray ePersonsArray = jsonResponse.getJSONObject("_embedded").getJSONArray("epersons");
        List<EPerson> ePersons = new ArrayList<>();

        for (int i = 0; i < ePersonsArray.length(); i++) {
            JSONObject ePersonJson = ePersonsArray.getJSONObject(i);
            EPerson ePerson = parseEPerson(ePersonJson);
            ePersons.add(ePerson);
        }

        return ePersons;
    }

    /**
     * Fetch details of a specific ePerson by their ID.
     *
     * @param personId The ID of the ePerson.
     * @return The ePerson object with its details.
     * @throws Exception if the request fails.
     */
    public EPerson getEPersonById(String personId) throws Exception {
        String response = client.get("/server/api/eperson/epersons/" + personId);
        JSONObject ePersonJson = new JSONObject(response);

        return parseEPerson(ePersonJson);
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
        JSONObject metadata = new JSONObject();
        metadata.put("eperson.firstname", new JSONArray().put(new JSONObject().put("value", firstname)));
        metadata.put("eperson.lastname", new JSONArray().put(new JSONObject().put("value", lastname)));
        metadata.put("eperson.email", new JSONArray().put(new JSONObject().put("value", email)));
        metadata.put("eperson.active", new JSONArray().put(new JSONObject().put("value", active)));

        JSONObject requestBody = new JSONObject();
        requestBody.put("metadata", metadata);

        String response = client.post("/server/api/eperson/epersons", requestBody.toString());
        JSONObject ePersonJson = new JSONObject(response);

        return parseEPerson(ePersonJson);
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
        JSONObject requestBody = new JSONObject();
        requestBody.put("metadata", updates);

        String response = client.put("/server/api/eperson/epersons/" + personId, requestBody.toString());
        JSONObject ePersonJson = new JSONObject(response);

        return parseEPerson(ePersonJson);
    }

    /**
     * Delete an ePerson by their ID.
     *
     * @param personId The ID of the ePerson to delete.
     * @throws Exception if the request fails.
     */
    public void deleteEPerson(String personId) throws Exception {
        client.delete("/server/api/eperson/epersons/" + personId);
    }

    /**
     * Parses a JSON object representing an ePerson into a Java object.
     *
     * @param ePersonJson The JSON object of the ePerson.
     * @return The parsed ePerson object.
     */
    private EPerson parseEPerson(JSONObject ePersonJson) {
        String id = ePersonJson.getString("id");
        String email = ePersonJson.getString("email");
        boolean canLogIn = ePersonJson.getBoolean("canLogIn");

        String firstName = ePersonJson.getJSONObject("metadata")
                .getJSONArray("eperson.firstname").getJSONObject(0).getString("value");

        String lastName = ePersonJson.getJSONObject("metadata")
                .getJSONArray("eperson.lastname").getJSONObject(0).getString("value");

        return new EPerson(id, email, firstName, lastName, canLogIn);
    }
}
