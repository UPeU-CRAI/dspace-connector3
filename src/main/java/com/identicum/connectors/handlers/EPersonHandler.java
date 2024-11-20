package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.schemas.EPersonSchema;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.IOException;

/**
 * Handler for managing EPerson operations in DSpace.
 */
public class EPersonHandler extends AbstractHandler {

    private static final String EPERSONS_ENDPOINT = "/server/api/eperson/epersons";

    public EPersonHandler(AuthenticationHandler authenticationHandler) {
        super(authenticationHandler);
    }

    public String createEPerson(EPersonSchema ePersonSchema) throws IOException {
        String endpoint = baseUrl + EPERSONS_ENDPOINT;
        HttpPost request = new HttpPost(endpoint);
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 201) { // Created
                return parseResponseBody(response).getString("id");
            } else {
                throw new RuntimeException("Failed to create EPerson. HTTP Status: " + statusCode);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public EPersonSchema getEPerson(String ePersonId) throws IOException, ParseException {
        String endpoint = baseUrl + EPERSONS_ENDPOINT + "/" + ePersonId;
        HttpGet request = new HttpGet(endpoint);

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                return EPersonSchema.fromJson(parseResponseBody(response));
            } else {
                throw new RuntimeException("Failed to retrieve EPerson with ID: " + ePersonId + ". HTTP Status: " + statusCode);
            }
        }
    }

    public void updateEPerson(String ePersonId, EPersonSchema ePersonSchema) throws IOException {
        String endpoint = baseUrl + EPERSONS_ENDPOINT + "/" + ePersonId;
        HttpPut request = new HttpPut(endpoint);
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode != 200) { // OK
                throw new RuntimeException("Failed to update EPerson with ID: " + ePersonId + ". HTTP Status: " + statusCode);
            }
        }
    }

    public void deleteEPerson(String ePersonId) throws IOException {
        String endpoint = baseUrl + EPERSONS_ENDPOINT + "/" + ePersonId;
        HttpDelete request = new HttpDelete(endpoint);

        try (CloseableHttpResponse response = executeRequest(request)) {
            int statusCode = response.getCode();
            if (statusCode != 204) { // No Content
                throw new RuntimeException("Failed to delete EPerson with ID: " + ePersonId + ". HTTP Status: " + statusCode);
            }
        }
    }
}
