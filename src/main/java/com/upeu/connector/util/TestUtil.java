package com.upeu.connector.util;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.DSpaceClient;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * Utility class for validating DSpace connection and authentication.
 */
public class TestUtil {

    /**
     * Validates the connection to the DSpace server by checking the status endpoint.
     *
     * @param client DSpaceClient instance to test the connection.
     * @throws ConnectorException if the connection test fails.
     */
    public static void validateConnection(DSpaceClient client) {
        if (client == null) {
            throw new IllegalArgumentException("DSpaceClient cannot be null.");
        }

        try {
            String response = client.get("/server/api/authn/status");
            if (response == null || response.isEmpty()) {
                throw new ConnectorException("No response from the authentication status endpoint.");
            }
        } catch (Exception e) {
            throw new ConnectorException("Failed to validate connection with DSpace: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the authentication process by checking if a valid JWT token is obtained.
     *
     * @param authManager AuthManager instance to validate authentication.
     * @throws ConnectorException if authentication validation fails.
     */
    public static void validateAuthentication(AuthManager authManager) {
        if (authManager == null) {
            throw new IllegalArgumentException("AuthManager cannot be null.");
        }

        try {
            String token = authManager.getJwtToken();
            if (token == null || token.isEmpty()) {
                throw new ConnectorException("Authentication failed: No valid token obtained.");
            }
        } catch (Exception e) {
            throw new ConnectorException("Error during authentication: " + e.getMessage(), e);
        }
    }
}
