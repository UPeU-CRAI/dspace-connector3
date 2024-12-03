package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.util.EndpointRegistry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles API communication with DSpace-CRIS.
 */
public class DSpaceClient {

    private static final Logger LOG = LoggerFactory.getLogger(DSpaceClient.class);

    private final AuthManager authManager;

    /**
     * Constructor for DSpaceClient.
     *
     * @param authManager AuthManager instance for handling authentication.
     */
    public DSpaceClient(AuthManager authManager) {
        if (authManager == null) {
            throw new IllegalArgumentException("AuthManager no puede ser nulo.");
        }
        this.authManager = authManager;
        LOG.info("DSpaceClient initialized.");
    }

    /**
     * Returns the AuthManager instance used by this DSpaceClient.
     *
     * @return AuthManager instance.
     */
    public AuthManager getAuthManager() {
        return this.authManager;
    }

    /**
     * Searches for resources using a specific endpoint and query.
     *
     * @param endpointKey The endpoint key (e.g., "epersons").
     * @param query       The query string.
     * @return List of results as JSON objects.
     */
    public List<JSONObject> search(String endpointKey, String query) {
        validateNonEmpty(endpointKey, "El endpointKey no puede ser nulo ni vacío.");
        LOG.debug("Recuperando endpoint para la clave: {}", endpointKey);

        String endpoint = EndpointRegistry.getEndpoint(endpointKey);
        LOG.debug("Endpoint recuperado: {}", endpoint);

        // Construir la URL
        String url = authManager.buildEndpoint(endpoint);
        if (query != null && !query.isEmpty()) {
            url += "?query=" + query;
        }

        LOG.info("Realizando búsqueda en URL: {}", url);

        try {
            String response = authManager.get(url);
            JSONObject jsonResponse = new JSONObject(response);

            // Validación de la respuesta
            if (!jsonResponse.has("results") || !(jsonResponse.get("results") instanceof JSONArray)) {
                throw new IllegalArgumentException("La respuesta no contiene un array válido bajo 'results'.");
            }

            // Procesamiento de los resultados
            return jsonResponse.getJSONArray("results")
                    .toList()
                    .stream()
                    .map(obj -> new JSONObject((Map<?, ?>) obj))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Error mientras se buscaba en el endpoint: {}", url, e);
            throw new RuntimeException("No se pudo realizar la búsqueda en el endpoint: " + url, e);
        }
    }

    /**
     * Performs a GET request to the specified endpoint.
     *
     * @param endpointKey The endpoint key.
     * @return Response as a JSON-formatted string.
     */
    public String get(String endpointKey) throws Exception {
        validateNonEmpty(endpointKey, "El endpoint no puede ser nulo ni vacío.");
        LOG.debug("Recuperando endpoint para operación GET con clave: {}", endpointKey);

        String endpoint = EndpointRegistry.getEndpoint(endpointKey);
        LOG.info("Realizando operación GET en el endpoint: {}", endpoint);

        try {
            return authManager.get(authManager.buildEndpoint(endpoint));
        } catch (Exception e) {
            LOG.error("Error en la operación GET para el endpoint: {}", endpoint, e);
            throw e;
        }
    }

    /**
     * Performs a POST request to the specified endpoint.
     *
     * @param endpointKey The endpoint key.
     * @param body        The JSON body of the request.
     * @return Response as a JSON-formatted string.
     */
    public String post(String endpointKey, String body) throws Exception {
        validateNonEmpty(endpointKey, "El endpoint no puede ser nulo ni vacío.");
        validateNonEmpty(body, "El cuerpo de la solicitud no puede ser nulo ni vacío.");

        LOG.debug("Recuperando endpoint para operación POST con clave: {}", endpointKey);

        String endpoint = EndpointRegistry.getEndpoint(endpointKey);
        LOG.info("Realizando operación POST en el endpoint: {}", endpoint);

        try {
            return authManager.post(authManager.buildEndpoint(endpoint), body);
        } catch (Exception e) {
            LOG.error("Error en la operación POST para el endpoint: {}", endpoint, e);
            throw e;
        }
    }

    /**
     * Performs a PUT request to the specified endpoint.
     *
     * @param endpointKey The endpoint key.
     * @param body        The JSON body of the request.
     * @return Response as a JSON-formatted string.
     */
    public String put(String endpointKey, String body) throws Exception {
        validateNonEmpty(endpointKey, "El endpoint no puede ser nulo ni vacío.");
        validateNonEmpty(body, "El cuerpo de la solicitud no puede ser nulo ni vacío.");

        LOG.debug("Recuperando endpoint para operación PUT con clave: {}", endpointKey);

        String endpoint = EndpointRegistry.getEndpoint(endpointKey);
        LOG.info("Realizando operación PUT en el endpoint: {}", endpoint);

        try {
            return authManager.put(authManager.buildEndpoint(endpoint), body);
        } catch (Exception e) {
            LOG.error("Error en la operación PUT para el endpoint: {}", endpoint, e);
            throw e;
        }
    }

    /**
     * Performs a DELETE request to the specified endpoint.
     *
     * @param endpointKey The endpoint key.
     */
    public void delete(String endpointKey) throws Exception {
        validateNonEmpty(endpointKey, "El endpoint no puede ser nulo ni vacío.");
        LOG.debug("Recuperando endpoint para operación DELETE con clave: {}", endpointKey);

        String endpoint = EndpointRegistry.getEndpoint(endpointKey);
        LOG.info("Realizando operación DELETE en el endpoint: {}", endpoint);

        try {
            authManager.delete(authManager.buildEndpoint(endpoint));
        } catch (Exception e) {
            LOG.error("Error en la operación DELETE para el endpoint: {}", endpoint, e);
            throw e;
        }
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param value       The string to validate.
     * @param errorMessage The error message to throw.
     */
    private void validateNonEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            LOG.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
