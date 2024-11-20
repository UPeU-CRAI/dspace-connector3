package com.identicum.connectors;

import com.evolveum.polygon.rest.AbstractRestConnector;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.common.objects.*;
import com.identicum.connectors.handlers.EPersonHandler;
import com.identicum.connectors.handlers.GroupHandler;
import com.identicum.connectors.handlers.ItemHandler;

import java.util.Set;
/**
 * Main connector class for DSpace-CRIS integration.
 * Handles EPerson, Group, and Item operations.
 */

@ConnectorClass(displayNameKey = "connector.identicum.rest.display", configurationClass = DSpaceConnectorConfiguration.class)
public class DSpaceConnector
        extends AbstractRestConnector<DSpaceConnectorConfiguration>
        implements TestOp, CreateOp, UpdateOp, DeleteOp {

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

        // Cast configuration
        DSpaceConnectorConfiguration dSpaceConfig = (DSpaceConnectorConfiguration) config;

        // Extract configuration values
        this.serviceAddress = dSpaceConfig.getServiceAddress();
        this.username = dSpaceConfig.getUsername();
        dSpaceConfig.getPassword().access(chars -> this.password = new String(chars));

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
    // Create Operation
    // =====================================
    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            // Convertir Set<Attribute> a EPersonSchema
            com.identicum.schemas.EPersonSchema ePersonSchema = mapToEPersonSchema(attributes);
            return ePersonHandler.createEPerson(ePersonSchema);
        } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
            // Convertir Set<Attribute> a GroupSchema
            com.identicum.schemas.GroupSchema groupSchema = mapToGroupSchema(attributes);
            return groupHandler.createGroup(groupSchema);
        } else {
            throw new UnsupportedOperationException("Object class " + objectClass.getObjectClassValue() + " is not supported.");
        }
    }

    // =====================================
    // Update Operation
    // =====================================
    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            EPersonSchema ePersonSchema = mapToEPersonSchema(replaceAttributes);
            return ePersonHandler.updateEPerson(uid, ePersonSchema);
        } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
            GroupSchema groupSchema = mapToGroupSchema(replaceAttributes);
            return groupHandler.updateGroup(uid, groupSchema);
        } else {
            throw new UnsupportedOperationException("Object class " + objectClass.getObjectClassValue() + " is not supported.");
        }
    }

    // =====================================
    // Delete Operation
    // =====================================
    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            ePersonHandler.deleteEPerson(uid);
        } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
            groupHandler.deleteGroup(uid);
        } else {
            throw new UnsupportedOperationException("Object class " + objectClass.getObjectClassValue() + " is not supported.");
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
