package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.schemas.GroupSchema;
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
 * Handler for managing Group operations in DSpace.
 * Supports creation, retrieval, updating, and deletion of groups using GroupSchema.
 */
public class GroupHandler {

    private final AuthenticationHandler authenticationHandler;
    private String baseUrl;

    /**
     * Constructor para GroupHandler.
     *
     * @param authenticationHandler Maneja la autenticación.
     */
    public GroupHandler(AuthenticationHandler authenticationHandler) {
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
     * Crea un nuevo grupo en DSpace.
     *
     * @param groupSchema Instancia de GroupSchema con los datos del grupo.
     * @return ID del nuevo grupo.
     * @throws IOException, ParseException Si ocurre un error durante la operación.
     */
    public String createGroup(GroupSchema groupSchema) throws IOException, ParseException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/eperson/groups";
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(groupSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() == 201) { // Created
                JSONObject jsonResponse = parseResponseBody(response);
                return jsonResponse.getString("id");
            } else {
                throw new RuntimeException("Failed to create group. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Recupera un grupo por su ID.
     *
     * @param groupId ID del grupo.
     * @return Instancia de GroupSchema con los datos del grupo.
     * @throws IOException, ParseException Si ocurre un error durante la operación.
     */
    public GroupSchema getGroup(String groupId) throws IOException, ParseException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/eperson/groups/" + groupId;
        HttpGet request = new HttpGet(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() == 200) { // OK
                JSONObject jsonResponse = parseResponseBody(response);
                return GroupSchema.fromJson(jsonResponse);
            } else {
                throw new RuntimeException("Failed to retrieve group. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Actualiza un grupo existente.
     *
     * @param groupId    ID del grupo a actualizar.
     * @param groupSchema Instancia de GroupSchema con los datos actualizados.
     * @throws IOException, ParseException Si ocurre un error durante la operación.
     */
    public void updateGroup(String groupId, GroupSchema groupSchema) throws IOException, ParseException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/eperson/groups/" + groupId;
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(groupSchema.toJson().toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() != 200) { // OK
                throw new RuntimeException("Failed to update group. HTTP Status: " + response.getCode());
            }
        }
    }

    /**
     * Elimina un grupo.
     *
     * @param groupId ID del grupo a eliminar.
     * @throws IOException, ParseException Si ocurre un error durante la operación.
     */
    public void deleteGroup(String groupId) throws IOException, ParseException {
        ensureBaseUrlInitialized();
        String endpoint = baseUrl + "/server/api/eperson/groups/" + groupId;
        HttpDelete request = new HttpDelete(endpoint);
        request.setHeader("Authorization", "Bearer " + authenticationHandler.getJwtToken());

        try (CloseableHttpClient httpClient = authenticationHandler.getHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {

            if (response.getCode() != 204) { // No Content
                throw new RuntimeException("Failed to delete group. HTTP Status: " + response.getCode());
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
