package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
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
    private String baseUrl; // Ahora configurable desde Midpoint

    /**
     * Constructor para EPersonHandler.
     *
     * @param authenticationHandler Maneja la autenticación.
     */
    public EPersonHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    /**
     * Setter para configurar el baseUrl desde la configuración de Midpoint.
     *
     * @param baseUrl El endpoint base del API REST.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Método para verificar que baseUrl fue inicializado antes de usarlo.
     */
    private void ensureBaseUrlInitialized() {
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalStateException("El baseUrl no está configurado. Por favor inicializa el conector correctamente.");
        }
    }

    /**
     * Crea un nuevo EPerson en DSpace.
     *
     * @param ePersonData JSON con los datos del usuario.
     * @return ID del nuevo EPerson.
     */
    public String createEPerson(JSONObject ePersonData) {
        ensureBaseUrlInitialized(); // Verifica que baseUrl esté configurado
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

    /**
     * Recupera un EPerson por su ID.
     *
     * @param ePersonId ID del EPerson a recuperar.
     * @return Objeto JSON con los detalles del EPerson.
     */
    public JSONObject getEPerson(String ePersonId) {
        ensureBaseUrlInitialized(); // Verifica que baseUrl esté configurado
        String endpoint = baseUrl + "/server/api/eperson/epersons/" + ePersonId;
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int statusCode = response.getCode();
            if (statusCode == 200) { // OK
                return new JSONObject(parseResponseBody(response));
            } else {
                throw new RuntimeException("Failed to retrieve EPerson. Status code: " + statusCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving EPerson", e);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing EPerson response", e);
        }
    }

    /**
     * Actualiza un EPerson existente.
     *
     * @param ePersonId  ID del EPerson a actualizar.
     * @param updatedData JSON con los datos actualizados del EPerson.
     */
    public void updateEPerson(String ePersonId, JSONObject updatedData) {
        ensureBaseUrlInitialized(); // Verifica que baseUrl esté configurado
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

    /**
     * Elimina un EPerson.
     *
     * @param ePersonId ID del EPerson a eliminar.
     */
    public void deleteEPerson(String ePersonId) {
        ensureBaseUrlInitialized(); // Verifica que baseUrl esté configurado
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

    /**
     * Método auxiliar para parsear el cuerpo de la respuesta.
     *
     * @param response La respuesta HTTP.
     * @return El cuerpo de la respuesta como String.
     * @throws IOException Si ocurre un error durante el parsing.
     * @throws ParseException Si ocurre un error al procesar la respuesta.
     */
    private String parseResponseBody(CloseableHttpResponse response) throws IOException, ParseException {
        return EntityUtils.toString(response.getEntity());
    }
}
