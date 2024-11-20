package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Handler for managing EPerson operations in DSpace.
 * Supports creation, retrieval, updating, and deletion of users.
 */
public class EPersonHandler {

    private final AuthenticationHandler authenticationHandler;
    private final String baseUrl;

    // =====================================
    // Constructor
    // =====================================
    public EPersonHandler(AuthenticationHandler authenticationHandler, String baseUrl) {
        this.authenticationHandler = authenticationHandler;
        this.baseUrl = baseUrl;
    }

    // =====================================
    // Create EPerson
    // =====================================
    public String createEPerson(JSONObject ePersonData) {
        String endpoint = baseUrl + "/server/api/eperson/epersons";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");

        try {
            request.setEntity(new StringEntity(ePersonData.toString()));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(request)) {

                int statusCode = response.getCode();
                if (statusCode == 201) { // Created
                    String responseBody = parseResponseBody(response);
                    return new JSONObject(responseBody).getString("id");
                } else {
                    throw new RuntimeException("Failed to create EPerson. Status code: " + statusCode);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating EPerson", e);
        }
    }

    // =====================================
    // Retrieve EPerson by ID
    // =====================================
    public JSONObject getEPerson(String ePersonId) {
        String endpoint = baseUrl + "/server/api/eperson/epersons/" + ePersonId;
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                String responseBody = parseResponseBody(response);
                return new JSONObject(responseBody);
            } else {
                throw new RuntimeException("Failed to retrieve EPerson. Status code: " + statusCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving EPerson", e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // =====================================
    // Update EPerson
    // =====================================
    public void updateEPerson(String ePersonId, JSONObject updatedData) {
        String endpoint = baseUrl + "/server/api/eperson/epersons/" + ePersonId;
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");

        try {
            request.setEntity(new StringEntity(updatedData.toString()));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(request)) {

                int statusCode = response.getCode();
                if (statusCode != 200) { // OK
                    throw new RuntimeException("Failed to update EPerson. Status code: " + statusCode);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating EPerson", e);
        }
    }

    // =====================================
    // Delete EPerson
    // =====================================
    public void deleteEPerson(String ePersonId) {
        String endpoint = baseUrl + "/server/api/eperson/epersons/" + ePersonId;
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getCode();
            if (statusCode != 204) { // No Content
                throw new RuntimeException("Failed to delete EPerson. Status code: " + statusCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error deleting EPerson", e);
        }
    }

    // =====================================
    // Helper Method: Parse Response Body
    // =====================================
    private String parseResponseBody(CloseableHttpResponse response) throws IOException, ParseException {
        return EntityUtils.toString(response.getEntity());
    }
}
