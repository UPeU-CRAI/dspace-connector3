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

    // Predefined endpoints
    public String getEPersonsEndpoint() {
        return buildEndpoint("/server/api/eperson/epersons");
    }

    public String getEPersonByIdEndpoint(String ePersonId) {
        return buildEndpoint(String.format("/server/api/eperson/epersons/%s", ePersonId));
    }

    public String getGroupsEndpoint() {
        return buildEndpoint("/server/api/eperson/groups");
    }

    public String getGroupByIdEndpoint(String groupId) {
        return buildEndpoint(String.format("/server/api/eperson/groups/%s", groupId));
    }

    public String getItemsEndpoint() {
        return buildEndpoint("/server/api/core/items");
    }

    public String getItemByIdEndpoint(String itemId) {
        return buildEndpoint(String.format("/server/api/core/items/%s", itemId));
    }

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
