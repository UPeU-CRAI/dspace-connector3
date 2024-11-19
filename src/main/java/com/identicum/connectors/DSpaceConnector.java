package com.identicum.connectors;

import com.evolveum.polygon.rest.AbstractRestConnector;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.operations.TestOp;
import com.identicum.connectors.handlers.EPersonHandler;
import com.identicum.connectors.handlers.GroupHandler;
import com.identicum.connectors.handlers.ItemHandler;

/**
 * Main connector class for DSpace-CRIS integration.
 * Handles EPerson, Group, and Item operations.
 */
public class DSpaceConnector extends AbstractRestConnector implements TestOp {

    // =====================================
    // Variables for Configuration
    // =====================================
    private String serviceAddress;
    private String username;
    private String password;

    private AuthenticationHandler authenticationHandler;
    private EPersonHandler ePersonHandler;
    private GroupHandler groupHandler;
    private ItemHandler itemHandler;

    // =====================================
    // Initialization and Configuration
    // =====================================
    @Override
    public void init(Configuration config) {
        if (!(config instanceof DSpaceConnectorConfiguration)) {
            throw new IllegalArgumentException("Invalid configuration class: " + config.getClass().getName());
        }

        // Cast configuration and extract values
        DSpaceConnectorConfiguration dSpaceConfig = (DSpaceConnectorConfiguration) config;
        this.serviceAddress = dSpaceConfig.getServiceAddress();
        this.username = dSpaceConfig.getUsername();
        this.password = dSpaceConfig.getPassword();

        // Initialize AuthenticationHandler
        authenticationHandler = new AuthenticationHandler(serviceAddress, username, password);

        // Initialize Handlers
        ePersonHandler = new EPersonHandler(authenticationHandler);
        groupHandler = new GroupHandler(authenticationHandler);
        itemHandler = new ItemHandler(authenticationHandler);
    }

    // =====================================
    // Test Operation
    // =====================================
    @Override
    public void test() {
        try {
            authenticationHandler.testConnection();
        } catch (Exception e) {
            throw new ConnectorException("Test connection failed: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Getters for Configuration
    // =====================================
    public String getServiceAddress() {
        return serviceAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
