package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.schemas.EPersonSchema;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Handler for managing EPerson operations in DSpace.
 * Supports creation, retrieval, updating, and deletion of users using EPersonSchema.
 */
public class EPersonHandler {

    private final AuthenticationHandler authenticationHandler;
    private String baseUrl;

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
     * @param ePersonSchema Instancia de EPersonSchema con los datos del usuario.
     * @return ID del nuevo EPerson.
     * @throws IOException, ParseException Si ocurre un error durante la operación.
     */
    public String createEPerson(EPersonSchema ePersonSchema) throws IOException, ParseException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/eperson/epersons";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() == 201) { // Created
                JSONObject jsonResponse = parseResponseBody(response);
                return jsonResponse.getString("id");
            } else {
                throw new RuntimeException("Failed to create EPerson. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Recupera un EPerson por su ID.
     *
     * @param ePersonId ID del EPerson.
     * @return Instancia de EPersonSchema con los datos del usuario.
     * @throws IOException, ParseException Si ocurre un error durante la operación.
     */
    public EPersonSchema getEPerson(String ePersonId) throws IOException, ParseException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/eperson/epersons/" + ePersonId;
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() == 200) { // OK
                JSONObject jsonResponse = parseResponseBody(response);
                return EPersonSchema.fromJson(jsonResponse);
            } else {
                throw new RuntimeException("Failed to retrieve EPerson. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Actualiza un EPerson existente.
     *
     * @param ePersonId ID del EPerson a actualizar.
     * @param ePersonSchema Instancia de EPersonSchema con los datos actualizados.
     * @throws IOException, ParseException Si ocurre un error durante la operación.
     */
    public void updateEPerson(String ePersonId, EPersonSchema ePersonSchema) throws IOException, ParseException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/eperson/epersons/" + ePersonId;
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(ePersonSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() != 200) { // OK
                throw new RuntimeException("Failed to update EPerson. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Elimina un EPerson.
     *
     * @param ePersonId ID del EPerson a eliminar.
     * @throws IOException, ParseException Si ocurre un error durante la operación.
     */
    public void deleteEPerson(String ePersonId) throws IOException, ParseException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/eperson/epersons/" + ePersonId;
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() != 204) { // No Content
                throw new RuntimeException("Failed to delete EPerson. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Método auxiliar para parsear el cuerpo de la respuesta.
     *
     * @param response Respuesta HTTP.
     * @return El cuerpo de la respuesta como JSONObject.
     * @throws IOException Si ocurre un error durante el parsing.
     */
    private JSONObject parseResponseBody(CloseableHttpResponse response) throws IOException, ParseException {
        String responseBody = EntityUtils.toString(response.getEntity());
        return new JSONObject(responseBody);
    }
}
