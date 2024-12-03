package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.handler.EPerson;
import com.upeu.connector.handler.EPersonHandler;
import com.upeu.connector.util.EndpointRegistry;
import com.upeu.connector.util.SchemaRegistry;
import com.upeu.connector.util.ValidationJsonUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.operations.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@ConnectorClass(configurationClass = DSpaceConfiguration.class, displayNameKey = "DSpaceConnector")
public class DSpaceConnector implements Connector, CreateOp, UpdateOp, DeleteOp, SearchOp<String>, SchemaOp, TestOp {

    private static final Logger LOG = LoggerFactory.getLogger(DSpaceConnector.class);

    private AuthManager authManager;
    private DSpaceConfiguration configuration;
    private DSpaceClient client;
    private EPersonHandler ePersonHandler;

    // ==============================
    // Inicialización y Validación
    // ==============================
    @Override
    public void init(Configuration configuration) {
        LOG.info("Initializing DSpaceConnector...");

        if (!(configuration instanceof DSpaceConfiguration)) {
            throw new IllegalArgumentException("Expected DSpaceConfiguration but got: " + configuration.getClass().getName());
        }

        this.configuration = (DSpaceConfiguration) configuration;

        if (!this.configuration.isInitialized()) {
            throw new IllegalStateException("Configuration is not initialized. Check baseUrl, username, and password.");
        }

        LOG.info("Base URL: {}", this.configuration.getBaseUrl());
        LOG.info("Username: {}", this.configuration.getUsername());
        LOG.info("Password: [PROTECTED]");

        this.authManager = new AuthManager(
                this.configuration.getBaseUrl(),
                this.configuration.getUsername(),
                this.configuration.getPassword()
        );

        validateAuthentication();

        this.client = new DSpaceClient(this.authManager);
        this.ePersonHandler = new EPersonHandler(client);

        LOG.info("DSpaceConnector initialized successfully.");
    }

    private void validateAuthentication() {
        try {
            if (!authManager.isAuthenticated()) {
                LOG.warn("Authentication not valid. Attempting renewal...");
                authManager.renewAuthentication();
                LOG.info("Authentication successfully renewed.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Authentication failed. Please check credentials and server connectivity.", e);
        }
    }

    @Override
    public void dispose() {
        LOG.info("Disposing resources in DSpaceConnector...");
        client = null;
        ePersonHandler = null;
        LOG.info("Resources disposed successfully.");
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    // ==============================
    // Operaciones CRUD Centralizadas
    // ==============================
    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions options) {
        return handleCrudOperation(objectClass, OperationType.CREATE, null, attributes);
    }

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions options) {
        return handleCrudOperation(objectClass, OperationType.UPDATE, uid, attributes);
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        handleCrudOperation(objectClass, OperationType.DELETE, uid, null);
    }

    private Uid handleCrudOperation(ObjectClass objectClass, OperationType operationType, Uid uid, Set<Attribute> attributes) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            switch (operationType) {
                case CREATE:
                    return ePersonHandler.create(attributes);
                case UPDATE:
                    return ePersonHandler.update(uid.getUidValue(), attributes);
                case DELETE:
                    ePersonHandler.delete(uid.getUidValue());
                    return null;
            }
        }
        throw new IllegalArgumentException("Unsupported object class: " + objectClass);
    }

    private enum OperationType {
        CREATE, UPDATE, DELETE
    }

    // ==============================
    // Operaciones de Búsqueda
    // ==============================
    @Override
    public FilterTranslator<String> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        if (objectClass.is("eperson")) {
            return new EPersonFilterTranslator();
        } else {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass);
        }
    }

    @Override
    public void executeQuery(ObjectClass objectClass, String query, ResultsHandler handler, OperationOptions options) {
        if (!objectClass.is("eperson")) {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass.getObjectClassValue());
        }

        List<JSONObject> results = client.search("epersons", query == null ? "" : query);

        for (JSONObject json : results) {
            ConnectorObject connectorObject = new EPerson(json).toConnectorObject();
            if (!handler.handle(connectorObject)) {
                LOG.debug("Result handling interrupted.");
                break;
            }
        }
    }

    // ==============================
    // Construcción del Schema
    // ==============================
    @Override
    public Schema schema() {
        LOG.info("Building schema for DSpaceConnector...");
        SchemaBuilder schemaBuilder = new SchemaBuilder(DSpaceConnector.class);

        SchemaRegistry.registerSchemas(schemaBuilder);
        return schemaBuilder.build();
    }

    // ==============================
    // Test de Conectividad
    // ==============================
    @Override
    public void test() {
        if (!authManager.isAuthenticated()) {
            authManager.renewAuthentication();
        }
        authManager.validateConnection();
        LOG.info("Connectivity test passed successfully.");
    }
}
