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
import com.identicum.schemas.EPersonSchema;
import com.identicum.schemas.GroupSchema;
import com.identicum.schemas.ItemSchema;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Main connector class for DSpace-CRIS integration.
 * Handles EPerson, Group, and Item operations by delegating to specific handlers.
 */
@ConnectorClass(displayNameKey = "connector.dspace.rest.display", configurationClass = DSpaceConnectorConfiguration.class)
public class DSpaceConnector
        extends AbstractRestConnector<DSpaceConnectorConfiguration>
        implements TestOp, CreateOp, UpdateOp, DeleteOp {

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

        // Initialize AuthenticationHandler with configuration values
        authenticationHandler = new AuthenticationHandler(
                dSpaceConfig.getBaseUrl(),
                dSpaceConfig.getUsername(),
                dSpaceConfig.getPassword()
        );

        // Initialize other handlers
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
        try {
            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                EPersonSchema ePersonSchema = mapToEPersonSchema(attributes);
                String ePersonId = ePersonHandler.createEPerson(ePersonSchema);
                return new Uid(ePersonId);
            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                GroupSchema groupSchema = mapToGroupSchema(attributes);
                String groupId = groupHandler.createGroup(groupSchema);
                return new Uid(groupId);
            } else if (objectClass.is("item")) {
                ItemSchema itemSchema = mapToItemSchema(attributes);
                String itemId = itemHandler.createItem(itemSchema);
                return new Uid(itemId);
            } else {
                throw new UnsupportedOperationException("Object class " + objectClass.getObjectClassValue() + " is not supported.");
            }
        } catch (IOException e) {
            throw new ConnectorException("Error during create operation: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Update Operation
    // =====================================
    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
        try {
            String uidValue = uid.getUidValue();

            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                EPersonSchema ePersonSchema = mapToEPersonSchema(replaceAttributes);
                ePersonHandler.updateEPerson(uidValue, ePersonSchema);
                return uid;
            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                GroupSchema groupSchema = mapToGroupSchema(replaceAttributes);
                groupHandler.updateGroup(uidValue, groupSchema);
                return uid;
            } else if (objectClass.is("item")) {
                ItemSchema itemSchema = mapToItemSchema(replaceAttributes);
                itemHandler.updateItem(uidValue, itemSchema);
                return uid;
            } else {
                throw new UnsupportedOperationException("Object class " + objectClass.getObjectClassValue() + " is not supported.");
            }
        } catch (IOException | URISyntaxException e) {
            throw new ConnectorException("Error during update operation: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Delete Operation
    // =====================================
    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        try {
            String uidValue = uid.getUidValue();

            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                ePersonHandler.deleteEPerson(uidValue);
            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                groupHandler.deleteGroup(uidValue);
            } else if (objectClass.is("item")) {
                itemHandler.deleteItem(uidValue);
            } else {
                throw new UnsupportedOperationException("Object class " + objectClass.getObjectClassValue() + " is not supported.");
            }
        } catch (IOException | URISyntaxException e) {
            throw new ConnectorException("Error during delete operation: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Helper Methods for Attribute Mapping
    // =====================================
    private EPersonSchema mapToEPersonSchema(Set<Attribute> attributes) {
        EPersonSchema schema = new EPersonSchema();
        for (Attribute attr : attributes) {
            switch (attr.getName()) {
                case "username":
                    schema.setUsername(AttributeUtil.getAsStringValue(attr));
                    break;
                case "email":
                    schema.setEmail(AttributeUtil.getAsStringValue(attr));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attr.getName());
            }
        }
        return schema;
    }

    private GroupSchema mapToGroupSchema(Set<Attribute> attributes) {
        GroupSchema schema = new GroupSchema();
        for (Attribute attr : attributes) {
            switch (attr.getName()) {
                case "groupName":
                    schema.setGroupName(AttributeUtil.getAsStringValue(attr));
                    break;
                case "description":
                    schema.setDescription(AttributeUtil.getAsStringValue(attr));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attr.getName());
            }
        }
        return schema;
    }

    private ItemSchema mapToItemSchema(Set<Attribute> attributes) {
        ItemSchema schema = new ItemSchema();
        for (Attribute attr : attributes) {
            switch (attr.getName()) {
                case "itemName":
                    schema.setItemName(AttributeUtil.getAsStringValue(attr));
                    break;
                case "itemDescription":
                    schema.setItemDescription(AttributeUtil.getAsStringValue(attr));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown attribute: " + attr.getName());
            }
        }
        return schema;
    }
}
