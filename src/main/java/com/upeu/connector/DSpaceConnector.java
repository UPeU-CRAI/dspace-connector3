package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.handler.EPersonHandler;
import com.upeu.connector.schema.EPersonSchema;
import com.upeu.connector.util.SchemaRegistry;
import com.upeu.connector.util.TestUtil;
import com.upeu.connector.util.ValidationUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.operations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (!(configuration instanceof DSpaceConfiguration)) {
            throw new IllegalArgumentException("Expected DSpaceConfiguration.");
        }
        this.configuration = (DSpaceConfiguration) configuration;

        if (!this.configuration.isInitialized()) {
            throw new IllegalStateException("Configuration is not initialized.");
        }

        this.authManager = new AuthManager(
                this.configuration.getBaseUrl(),
                this.configuration.getUsername(),
                this.configuration.getPassword()
        );

        if (!authManager.isAuthenticated()) {
            authManager.renewAuthentication();
        }

        this.client = new DSpaceClient(this.configuration, this.authManager);
        this.ePersonHandler = new EPersonHandler(client);

        LOG.info("DSpaceConnector initialized successfully.");
    }

    public void validate() {
        // Validar configuración utilizando el utilitario
        ValidationUtil.validateConfiguration(configuration);

        // Validar autenticación
        if (!authManager.isAuthenticated()) {
            authManager.renewAuthentication();
        }
        LOG.info("Configuration and authentication validated successfully.");
    }

    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(DSpaceConnector.class);
        SchemaRegistry.registerSchemas(schemaBuilder);
        LOG.info("Schemas registered successfully.");
        return schemaBuilder.build();
    }

    @Override
    public void dispose() {
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
        }
        throw new IllegalArgumentException("Unsupported object class: " + objectClass);
    }

    @Override
    public void executeQuery(ObjectClass objectClass, String query, ResultsHandler handler, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            if (query == null || query.isEmpty()) {
                throw new IllegalArgumentException("Query cannot be null or empty.");
            }

            // Delegar la búsqueda al manejador de EPerson
            ePersonHandler.search(query, handler);
        } else {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass);
        }
    }

    // ==============================
    // Test de Conectividad
    // ==============================
    @Override
    public void test() {
        try {
            // Validar configuración
            if (configuration == null || !configuration.isInitialized()) {
                throw new IllegalStateException("Configuration is not initialized.");
            }

            // Validar autenticación
            if (!authManager.isAuthenticated()) {
                authManager.renewAuthentication();
            }

            // Probar conectividad
            LOG.info("Testing connectivity...");
            TestUtil.validateConnection(client);
            LOG.info("Connectivity test passed successfully.");
        } catch (Exception e) {
            LOG.error("Connectivity test failed: ", e);
            throw new ConnectorException("Test failed: " + e.getMessage(), e);
        }
    }

}
