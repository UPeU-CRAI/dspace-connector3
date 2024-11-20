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
        // Use the baseUrl as-is since it's already validated.
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl; // Remove trailing slash if present
    }

    /**
     * @return Endpoint for retrieving all EPersons.
     */
    public String getEPersonsEndpoint() {
        return baseUrl + "/server/api/eperson/epersons";
    }

    /**
     * Builds an endpoint for a specific EPerson by ID.
     *
     * @param ePersonId The ID of the EPerson.
     * @return The endpoint for the specified EPerson.
     */
    public String getEPersonByIdEndpoint(String ePersonId) {
        return String.format(baseUrl + "/server/api/eperson/epersons/%s", ePersonId);
    }

    /**
     * @return Endpoint for retrieving all Groups.
     */
    public String getGroupsEndpoint() {
        return baseUrl + "/server/api/eperson/groups";
    }

    /**
     * Builds an endpoint for a specific Group by ID.
     *
     * @param groupId The ID of the Group.
     * @return The endpoint for the specified Group.
     */
    public String getGroupByIdEndpoint(String groupId) {
        return String.format(baseUrl + "/server/api/eperson/groups/%s", groupId);
    }

    /**
     * @return Endpoint for retrieving all Items.
     */
    public String getItemsEndpoint() {
        return baseUrl + "/server/api/core/items";
    }

    /**
     * Builds an endpoint for a specific Item by ID.
     *
     * @param itemId The ID of the Item.
     * @return The endpoint for the specified Item.
     */
    public String getItemByIdEndpoint(String itemId) {
        return String.format(baseUrl + "/server/api/core/items/%s", itemId);
    }

    /**
     * @return Endpoint for authentication status.
     */
    public String getAuthnStatusEndpoint() {
        return baseUrl + "/server/api/authn/status";
    }

    /**
     * @return Endpoint for login authentication.
     */
    public String getLoginEndpoint() {
        return baseUrl + "/server/api/authn/login";
    }

    /**
     * @return Endpoint for logout authentication.
     */
    public String getLogoutEndpoint() {
        return baseUrl + "/server/api/authn/logout";
    }
}
