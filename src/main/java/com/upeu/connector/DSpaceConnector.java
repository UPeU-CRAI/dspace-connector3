package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.handler.EPerson;
import com.upeu.connector.handler.EPersonHandler;
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
            LOG.error("Invalid configuration type: {}", configuration.getClass().getName());
            throw new IllegalArgumentException("Expected DSpaceConfiguration but got: " + configuration.getClass().getName());
        }

        this.configuration = (DSpaceConfiguration) configuration;

        if (!this.configuration.isInitialized()) {
            LOG.error("Configuration is not initialized. Check baseUrl, username, and password.");
            throw new IllegalStateException("Configuration is not initialized. Check baseUrl, username, and password.");
        }

        LOG.info("Base URL: {}", this.configuration.getBaseUrl());
        LOG.info("Username: {}", this.configuration.getUsername());
        LOG.info("Password: [PROTECTED]");

        // Inicializar AuthManager
        LOG.debug("Initializing AuthManager...");
        this.authManager = new AuthManager(
                this.configuration.getBaseUrl(),
                this.configuration.getUsername(),
                this.configuration.getPassword()
        );

        // Validar autenticación
        validateAuthentication();

        // Inicializar DSpaceClient
        LOG.debug("Initializing DSpaceClient...");
        this.client = new DSpaceClient(this.authManager);

        // Inicializar EPersonHandler
        LOG.debug("Initializing EPersonHandler...");
        this.ePersonHandler = new EPersonHandler(client);

        LOG.info("DSpaceConnector initialized successfully.");
    }

    private void validateAuthentication() {
        LOG.info("Validating authentication...");
        try {
            if (!authManager.isAuthenticated()) {
                LOG.warn("Authentication not valid. Attempting renewal...");
                authManager.renewAuthentication();
                LOG.info("Authentication successfully renewed.");
            }
        } catch (Exception e) {
            LOG.error("Failed to authenticate or renew token: {}", e.getMessage(), e);
            throw new IllegalStateException("Authentication failed. Please check credentials and server connectivity.", e);
        }
        LOG.info("Authentication validated successfully.");
    }

    public void validate() {
        LOG.info("Validating configuration and authentication...");
        try {
            ValidationJsonUtil.validateConfiguration(configuration);
            validateAuthentication();
            LOG.info("Configuration and authentication validated successfully.");
        } catch (Exception e) {
            LOG.error("Validation failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Schema schema() {
        LOG.info("Building schema for DSpaceConnector...");
        SchemaBuilder schemaBuilder = new SchemaBuilder(DSpaceConnector.class);

        try {
            SchemaRegistry.registerSchemas(schemaBuilder);
            LOG.info("Schemas registered successfully.");
        } catch (Exception e) {
            LOG.error("Error while registering schemas: {}", e.getMessage(), e);
            throw new ConnectorException("Failed to build schema.", e);
        }

        return schemaBuilder.build();
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
    // Operaciones CRUD
    // ==============================
    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            return ePersonHandler.create(attributes);
        }
        throw new IllegalArgumentException("Unsupported object class: " + objectClass);
    }

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            return ePersonHandler.update(uid.getUidValue(), attributes);
        }
        throw new IllegalArgumentException("Unsupported object class: " + objectClass);
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            ePersonHandler.delete(uid.getUidValue());
        } else {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass);
        }
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
        LOG.debug("Executing query for ObjectClass: {}", objectClass.getObjectClassValue());

        if (objectClass.is("eperson")) {
            List<JSONObject> results;

            if (query == null || query.isEmpty()) {
                LOG.debug("Query without filter, retrieving all epersons.");
                results = client.search(authManager.buildEndpoint("server/api/eperson/epersons"), "");
            } else {
                LOG.debug("Query with filter: {}", query);
                results = client.search(authManager.buildEndpoint("server/api/eperson/epersons"), query);
            }

            for (JSONObject json : results) {
                EPerson ePerson = new EPerson(json);
                ConnectorObject connectorObject = ePerson.toConnectorObject();
                if (!handler.handle(connectorObject)) {
                    LOG.debug("Result handling interrupted.");
                    break;
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass.getObjectClassValue());
        }
    }

    // ==============================
    // Test de Conectividad
    // ==============================
    @Override
    public void test() {
        try {
            if (configuration == null || !configuration.isInitialized()) {
                throw new IllegalStateException("Configuration is not initialized.");
            }

            if (!authManager.isAuthenticated()) {
                authManager.renewAuthentication();
            }

            LOG.info("Testing connectivity...");
            authManager.validateConnection();
            LOG.info("Connectivity test passed successfully.");
        } catch (Exception e) {
            LOG.error("Connectivity test failed: ", e);
            throw new ConnectorException("Test failed: " + e.getMessage(), e);
        }
    }
}
