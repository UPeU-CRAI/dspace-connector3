package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.util.HttpUtil;
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
    private final HttpUtil httpUtil;

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
        this.httpUtil = new HttpUtil(authManager);
        LOG.info("DSpaceClient initialized.");
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

        String url = authManager.getEndpoint(endpointKey); // Obtains endpoint via AuthManager
        if (query != null && !query.isEmpty()) {
            url += "?query=" + query;
        }

        LOG.debug("Executing search on URL: {}", url);

        try {
            String response = httpUtil.get(url);
            return new JSONObject(response)
                    .getJSONArray("results")
                    .toList()
                    .stream()
                    .map(obj -> new JSONObject((Map<?, ?>) obj))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Error while performing search on endpoint: {}", url, e);
            throw new RuntimeException("Failed to execute search on endpoint: " + url, e);
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
        return httpUtil.get(authManager.getEndpoint(endpointKey));
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
        return httpUtil.post(authManager.getEndpoint(endpointKey), body);
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
        return httpUtil.put(authManager.getEndpoint(endpointKey), body);
    }

    /**
     * Performs a DELETE request to the specified endpoint.
     *
     * @param endpointKey The endpoint key.
     */
    public void delete(String endpointKey) throws Exception {
        validateNonEmpty(endpointKey, "El endpoint no puede ser nulo ni vacío.");
        httpUtil.delete(authManager.getEndpoint(endpointKey));
    }

    /**
     * Validates that a string is not null or empty.
     *
     * @param value       The string to validate.
     * @param errorMessage The error message to throw.
     */
    private void validateNonEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
