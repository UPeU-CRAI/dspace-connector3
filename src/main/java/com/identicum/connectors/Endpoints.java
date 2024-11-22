package com.identicum.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to centralize API endpoint definitions for the DSpace connector.
 * It dynamically builds endpoints using the provided baseUrl.
 */
public class Endpoints {

    private static final Logger LOG = LoggerFactory.getLogger(Endpoints.class);

    private final String baseUrl;

    /**
     * Constructor to initialize Endpoints with a base URL.
     *
     * @param baseUrl The base URL for the DSpace server, e.g., "http://192.168.15.231:8080".
     *                The value is assumed to be validated in DSpaceConnectorConfiguration.
     */
    public Endpoints(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            LOG.error("La URL base no puede ser nula o vacía.");
            throw new IllegalArgumentException("La URL base no puede ser nula o vacía.");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl; // Remove trailing slash if present
        LOG.info("Endpoints inicializados con la URL base: {}", this.baseUrl);
    }

    /**
     * Builds a full endpoint URL dynamically by appending the given path to the base URL.
     *
     * @param path The relative API path to append to the base URL.
     * @return The full endpoint URL.
     */
    public String buildEndpoint(String path) {
        if (path == null || path.isEmpty()) {
            LOG.error("El path proporcionado no puede ser nulo o vacío.");
            throw new IllegalArgumentException("El path no puede ser nulo o vacío.");
        }
        String endpoint = baseUrl + (path.startsWith("/") ? path : "/" + path);
        LOG.debug("Endpoint construido: {}", endpoint);
        return endpoint;
    }

    /**
     * Builds a full endpoint URL with query parameters.
     *
     * @param path   The relative API path to append to the base URL.
     * @param params The query parameters in "key=value" format.
     * @return The full endpoint URL with query parameters.
     */
    public String buildEndpointWithParams(String path, String... params) {
        String endpoint = buildEndpoint(path);
        if (params != null && params.length > 0) {
            String queryParams = String.join("&", params);
            endpoint += "?" + queryParams;
            LOG.debug("Endpoint con parámetros construido: {}", endpoint);
        }
        return endpoint;
    }

    // ============================
    // Metadata Schemas and Fields
    // ============================
    public String getMetadataSchemasEndpoint() {
        String endpoint = buildEndpoint("/server/api/core/metadataschemas");
        LOG.info("Metadata Schemas Endpoint: {}", endpoint);
        return endpoint;
    }

    public String getMetadataSchemaByIdEndpoint(String schemaId) {
        if (schemaId == null || schemaId.isEmpty()) {
            LOG.error("El schemaId proporcionado no puede ser nulo o vacío.");
            throw new IllegalArgumentException("El schemaId no puede ser nulo o vacío.");
        }
        String endpoint = buildEndpoint(String.format("/server/api/core/metadataschemas/%s", schemaId));
        LOG.info("Metadata Schema by ID Endpoint: {}", endpoint);
        return endpoint;
    }

    public String getMetadataFieldsEndpoint() {
        String endpoint = buildEndpoint("/server/api/core/metadatafields");
        LOG.info("Metadata Fields Endpoint: {}", endpoint);
        return endpoint;
    }

    // ============================
    // EPerson Endpoints
    // ============================
    public String getEPersonsEndpoint() {
        String endpoint = buildEndpoint("/server/api/eperson/epersons");
        LOG.info("EPersons Endpoint: {}", endpoint);
        return endpoint;
    }

    public String getEPersonByIdEndpoint(String ePersonId) {
        if (ePersonId == null || ePersonId.isEmpty()) {
            LOG.error("El ePersonId proporcionado no puede ser nulo o vacío.");
            throw new IllegalArgumentException("El ePersonId no puede ser nulo o vacío.");
        }
        String endpoint = buildEndpoint(String.format("/server/api/eperson/epersons/%s", ePersonId));
        LOG.info("EPerson by ID Endpoint: {}", endpoint);
        return endpoint;
    }

    public String getProfilesEndpoint() {
        String endpoint = buildEndpoint("/server/api/eperson/profiles");
        LOG.info("Profiles Endpoint: {}", endpoint);
        return endpoint;
    }

    // ============================
    // Group Endpoints
    // ============================
    public String getGroupsEndpoint() {
        String endpoint = buildEndpoint("/server/api/eperson/groups");
        LOG.info("Groups Endpoint: {}", endpoint);
        return endpoint;
    }

    public String getGroupByIdEndpoint(String groupId) {
        if (groupId == null || groupId.isEmpty()) {
            LOG.error("El groupId proporcionado no puede ser nulo o vacío.");
            throw new IllegalArgumentException("El groupId no puede ser nulo o vacío.");
        }
        String endpoint = buildEndpoint(String.format("/server/api/eperson/groups/%s", groupId));
        LOG.info("Group by ID Endpoint: {}", endpoint);
        return endpoint;
    }

    // ============================
    // Item Endpoints
    // ============================
    public String getItemsEndpoint() {
        String endpoint = buildEndpoint("/server/api/core/items");
        LOG.info("Items Endpoint: {}", endpoint);
        return endpoint;
    }

    public String getItemByIdEndpoint(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            LOG.error("El itemId proporcionado no puede ser nulo o vacío.");
            throw new IllegalArgumentException("El itemId no puede ser nulo o vacío.");
        }
        String endpoint = buildEndpoint(String.format("/server/api/core/items/%s", itemId));
        LOG.info("Item by ID Endpoint: {}", endpoint);
        return endpoint;
    }

    // ============================
    // Authentication Endpoints
    // ============================
    public String getAuthnStatusEndpoint() {
        String endpoint = buildEndpoint("/server/api/authn/status");
        LOG.info("Authn Status Endpoint: {}", endpoint);
        return endpoint;
    }

    public String getLoginEndpoint() {
        String endpoint = buildEndpoint("/server/api/authn/login");
        LOG.info("Login Endpoint: {}", endpoint);
        return endpoint;
    }

    public String getLogoutEndpoint() {
        String endpoint = buildEndpoint("/server/api/authn/logout");
        LOG.info("Logout Endpoint: {}", endpoint);
        return endpoint;
    }
}
