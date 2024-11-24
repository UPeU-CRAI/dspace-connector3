package com.upeu.connector;

import com.upeu.connector.handler.EPerson;
import com.upeu.connector.handler.EPersonHandler;
import com.upeu.connector.schema.EPersonSchema;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.operations.*;
import org.json.JSONObject;

import java.util.Set;

/**
 * DSpaceConnector is the main implementation of the connector
 * for interacting with DSpace-CRIS via REST API.
 */
@ConnectorClass(configurationClass = DSpaceConfiguration.class, displayNameKey = "DSpaceConnector")
public class DSpaceConnector implements Connector, CreateOp, UpdateOp, DeleteOp, SearchOp<String>, SchemaOp {

    private DSpaceConfiguration configuration;
    private DSpaceClient client;
    private EPersonHandler ePersonHandler;

    @Override
    public void init(Configuration config) {
        this.configuration = (DSpaceConfiguration) config;
        this.client = new DSpaceClient(this.configuration);
        this.ePersonHandler = new EPersonHandler(this.client);

        try {
            // Authenticate the client
            this.client.authenticate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate the client: " + e.getMessage(), e);
        }
    }

    public void setClient(DSpaceClient client) {
        this.client = client;
    }

    public void validate() {
        if (this.configuration == null) {
            throw new IllegalStateException("Configuration is not initialized");
        }

        if (this.configuration.getBaseUrl() == null || this.configuration.getBaseUrl().isEmpty()) {
            throw new IllegalArgumentException("Base URL is not set");
        }

        if (this.configuration.getUsername() == null || this.configuration.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username is not set");
        }

        if (this.configuration.getPassword() == null || this.configuration.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is not set");
        }
    }

    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(DSpaceConnector.class);
        EPersonSchema.define(schemaBuilder);
        return schemaBuilder.build();
    }

    @Override
    public void dispose() {
        // Clean up resources if needed
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions options) {
        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass);
        }

        String email = AttributeUtil.getStringValue(AttributeUtil.find("email", attributes));
        String firstName = AttributeUtil.getStringValue(AttributeUtil.find("firstname", attributes));
        String lastName = AttributeUtil.getStringValue(AttributeUtil.find("lastname", attributes));
        Boolean canLogIn = AttributeUtil.getBooleanValue(AttributeUtil.find("canLogIn", attributes));

        try {
            EPerson ePerson = ePersonHandler.createEPerson(firstName, lastName, email, canLogIn != null && canLogIn);
            return new Uid(ePerson.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ePerson: " + e.getMessage(), e);
        }
    }

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions options) {
        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass);
        }

        try {
            JSONObject updates = new JSONObject();
            for (Attribute attr : attributes) {
                if (!OperationalAttributes.PASSWORD_NAME.equals(attr.getName())) {
                    updates.put(attr.getName(), AttributeUtil.getSingleValue(attr));
                }
            }

            EPerson updatedEPerson = ePersonHandler.updateEPerson(uid.getUidValue(), updates);
            return new Uid(updatedEPerson.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ePerson: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass);
        }

        try {
            ePersonHandler.deleteEPerson(uid.getUidValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete ePerson: " + e.getMessage(), e);
        }
    }

    @Override
    public FilterTranslator<String> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return filter -> null; // Filtering not implemented
    }

    @Override
    public void executeQuery(ObjectClass objectClass, String query, ResultsHandler handler, OperationOptions options) {
        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass);
        }

        try {
            for (EPerson ePerson : ePersonHandler.getAllEPersons()) {
                ConnectorObject connectorObject = new ConnectorObjectBuilder()
                        .setUid(ePerson.getId())
                        .setName(ePerson.getEmail())
                        .addAttribute("firstname", ePerson.getFirstName())
                        .addAttribute("lastname", ePerson.getLastName())
                        .addAttribute("canLogIn", ePerson.canLogIn())
                        .build();
                handler.handle(connectorObject);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }
}
