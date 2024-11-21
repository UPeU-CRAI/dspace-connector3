package com.identicum.connectors;

/**
 * A utility class to centralize API endpoint definitions for the DSpace connector.
 * It dynamically builds endpoints using the provided baseUrl.
 */
public class Endpoints {

    private final String baseUrl;

    /**
     * Constructor to initialize Endpoints with a base URL.
     *
     * @param baseUrl The base URL for the DSpace server, e.g., "http://192.168.15.231:8080".
     *                The value is assumed to be validated in DSpaceConnectorConfiguration.
     */
    public Endpoints(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty.");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl; // Remove trailing slash if present
    }

    /**
     * Builds a full endpoint URL dynamically by appending the given path to the base URL.
     *
     * @param path The relative API path to append to the base URL.
     * @return The full endpoint URL.
     */
    public String buildEndpoint(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty.");
        }
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
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
        }
        return endpoint;
    }

    // ============================
    // Metadata Schemas and Fields
    // ============================
    public String getMetadataSchemasEndpoint() {
        return buildEndpoint("/server/api/core/metadataschemas");
    }

    public String getMetadataSchemaByIdEndpoint(String schemaId) {
        return buildEndpoint(String.format("/server/api/core/metadataschemas/%s", schemaId));
    }

    public String getMetadataFieldsEndpoint() {
        return buildEndpoint("/server/api/core/metadatafields");
    }

    // ============================
    // EPerson Endpoints
    // ============================
    public String getEPersonsEndpoint() {
        return buildEndpoint("/server/api/eperson/epersons");
    }

    public String getEPersonByIdEndpoint(String ePersonId) {
        return buildEndpoint(String.format("/server/api/eperson/epersons/%s", ePersonId));
    }

    public String getProfilesEndpoint() {
        return buildEndpoint("/server/api/eperson/profiles");
    }

    // ============================
    // Group Endpoints
    // ============================
    public String getGroupsEndpoint() {
        return buildEndpoint("/server/api/eperson/groups");
    }

    public String getGroupByIdEndpoint(String groupId) {
        return buildEndpoint(String.format("/server/api/eperson/groups/%s", groupId));
    }

    // ============================
    // Item Endpoints
    // ============================
    public String getItemsEndpoint() {
        return buildEndpoint("/server/api/core/items");
    }

    public String getItemByIdEndpoint(String itemId) {
        return buildEndpoint(String.format("/server/api/core/items/%s", itemId));
    }

    // ============================
    // Authentication Endpoints
    // ============================
    public String getAuthnStatusEndpoint() {
        return buildEndpoint("/server/api/authn/status");
    }

    public String getLoginEndpoint() {
        return buildEndpoint("/server/api/authn/login");
    }

    public String getLogoutEndpoint() {
        return buildEndpoint("/server/api/authn/logout");
    }
}
