package com.identicum.connectors;

import com.identicum.connectors.DSpaceConnectorConfiguration;

/**
 * A utility class to centralize API endpoint definitions for the DSpace connector.
 * Dynamically generates full API URLs based on the provided configuration.
 */
public class Endpoints {

    private final String baseUrl; // Base URL for the DSpace API

    /**
     * Constructor to initialize Endpoints with a valid configuration.
     *
     * @param config The DSpaceConnectorConfiguration instance.
     */
    public Endpoints(DSpaceConnectorConfiguration config) {
        if (config == null || config.getBaseUrl() == null || config.getBaseUrl().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty in configuration.");
        }
        String configuredBaseUrl = config.getBaseUrl();
        this.baseUrl = configuredBaseUrl.endsWith("/") ?
                configuredBaseUrl.substring(0, configuredBaseUrl.length() - 1) : configuredBaseUrl; // Ensure no trailing slash
    }

    // Authentication Endpoints
    public String getLoginEndpoint() {
        return baseUrl + "/server/api/authn/login";
    }

    public String getStatusEndpoint() {
        return baseUrl + "/server/api/authn/status";
    }

    // EPerson Endpoints
    public String getEPersonsEndpoint() {
        return baseUrl + "/server/api/eperson/epersons";
    }

    public String getEPersonByIdEndpoint(String id) {
        return String.format(baseUrl + "/server/api/eperson/epersons/%s", id);
    }

    // Group Endpoints
    public String getGroupsEndpoint() {
        return baseUrl + "/server/api/eperson/groups";
    }

    public String getGroupByIdEndpoint(String id) {
        return String.format(baseUrl + "/server/api/eperson/groups/%s", id);
    }

    // Item Endpoints
    public String getItemsEndpoint() {
        return baseUrl + "/server/api/core/items";
    }

    public String getItemByIdEndpoint(String id) {
        return String.format(baseUrl + "/server/api/core/items/%s", id);
    }
}
