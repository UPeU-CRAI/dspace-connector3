package com.identicum.connectors;

/**
 * A utility class to centralize API endpoint definitions for the DSpace connector.
 */
public class Endpoints {

    // Base Endpoints
    public static final String BASE_API = "/server/api";

    // EPerson Endpoints
    public static final String EPERSONS = BASE_API + "/eperson/epersons";
    public static final String EPERSON_BY_ID = EPERSONS + "/%s"; // Placeholder for EPerson ID

    // Group Endpoints
    public static final String GROUPS = BASE_API + "/eperson/groups";
    public static final String GROUP_BY_ID = GROUPS + "/%s"; // Placeholder for Group ID

    // Item Endpoints
    public static final String ITEMS = BASE_API + "/core/items";
    public static final String ITEM_BY_ID = ITEMS + "/%s"; // Placeholder for Item ID

    /**
     * Private constructor to prevent instantiation.
     */
    private Endpoints() {
        // Utility class, no need to instantiate
    }
}
