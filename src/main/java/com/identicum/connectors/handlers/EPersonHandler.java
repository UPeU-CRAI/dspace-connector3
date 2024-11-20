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
     * Método auxiliar para parsear el cuerpo de la respuesta.
     */
    private String parseResponseBody(CloseableHttpResponse response) throws IOException, ParseException {
        return EntityUtils.toString(response.getEntity());
    }
}
