package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import org.json.JSONObject;

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
     * @return A JSON string containing the list of ePersons.
     * @throws Exception if the request fails.
     */
    public String getAllEPersons() throws Exception {
        return client.get("/server/api/eperson/epersons");
    }

    /**
     * Fetch details of a specific ePerson by their ID.
     *
     * @param personId The ID of the ePerson.
     * @return A JSON string containing the details of the ePerson.
     * @throws Exception if the request fails.
     */
    public String getEPersonById(String personId) throws Exception {
        return client.get("/server/api/eperson/epersons/" + personId);
    }

    /**
     * Create a new ePerson in the DSpace-CRIS system.
     *
     * @param firstname The first name of the ePerson.
     * @param lastname  The last name of the ePerson.
     * @param email     The email address of the ePerson.
     * @param active    Whether the ePerson is active.
     * @return A JSON string containing the created ePerson.
     * @throws Exception if the request fails.
     */
    public String createEPerson(String firstname, String lastname, String email, boolean active) throws Exception {
        JSONObject metadata = new JSONObject();
        metadata.put("cris.person.firstname", new JSONObject().put("value", firstname));
        metadata.put("cris.person.lastname", new JSONObject().put("value", lastname));
        metadata.put("cris.person.email", new JSONObject().put("value", email));
        metadata.put("cris.person.active", new JSONObject().put("value", active));

        JSONObject requestBody = new JSONObject();
        requestBody.put("metadata", metadata);

        return client.post("/server/api/eperson/epersons", requestBody.toString());
    }

    /**
     * Update an existing ePerson's information.
     *
     * @param personId  The ID of the ePerson to update.
     * @param updates   A JSON object containing the fields to update.
     * @return A JSON string containing the updated ePerson.
     * @throws Exception if the request fails.
     */
    public String updateEPerson(String personId, JSONObject updates) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("metadata", updates);

        return client.put("/server/api/eperson/epersons/" + personId, requestBody.toString());
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
}
