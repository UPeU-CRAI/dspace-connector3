package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.handler.EPerson;
import com.upeu.connector.handler.EPersonHandler;
import com.upeu.connector.handler.FilterHandler;
import com.upeu.connector.schema.EPersonSchema;
import com.upeu.connector.util.TestUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.operations.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * DSpaceConnector is the main implementation of the connector
 * for interacting with DSpace-CRIS via REST API.
 */
@ConnectorClass(configurationClass = DSpaceConfiguration.class, displayNameKey = "DSpaceConnector")
public class DSpaceConnector implements Connector, CreateOp, UpdateOp, DeleteOp, SearchOp<String>, SchemaOp, TestOp {

    private static final Log LOG = Log.getLog(DSpaceConnector.class);

    private AuthManager authManager;
    private DSpaceConfiguration configuration;
    private DSpaceClient client;
    private EPersonHandler ePersonHandler;

    @Override
    public void init(Configuration configuration) {
        if (!(configuration instanceof DSpaceConfiguration)) {
            throw new IllegalArgumentException("Invalid configuration type. Expected DSpaceConfiguration.");
        }
        this.configuration = (DSpaceConfiguration) configuration;

        // Initialize AuthManager
        this.authManager = new AuthManager(
                this.configuration.getBaseUrl(),
                this.configuration.getUsername(),
                this.configuration.getPassword()
        );

        // Autenticación inicial
        authManager.getJwtToken(); // Este método maneja errores internamente

        // Initialize DSpaceClient with AuthManager
        this.client = new DSpaceClient(this.configuration, this.authManager);

        // Initialize EPersonHandler with the authenticated client
        this.ePersonHandler = new EPersonHandler(client);
    }

    public void validate() {
        if (configuration == null || !configuration.isInitialized()) {
            throw new IllegalStateException("Configuration is not initialized.");
        }
        if (!authManager.isAuthenticated()) {
            try {
                authManager.renewAuthentication();
            } catch (Exception e) {
                throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
            }
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
        client = null;
        ePersonHandler = null;
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
        try {
            String email = AttributeUtil.getStringValue(AttributeUtil.find("email", attributes));
            String firstName = AttributeUtil.getStringValue(AttributeUtil.find("firstname", attributes));
            String lastName = AttributeUtil.getStringValue(AttributeUtil.find("lastname", attributes));
            Boolean canLogIn = AttributeUtil.getBooleanValue(AttributeUtil.find("canLogIn", attributes));

            if (email == null || firstName == null || lastName == null) {
                throw new IllegalArgumentException("Mandatory attributes (email, firstname, lastname) are missing.");
            }

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
            if (uid == null) {
                throw new IllegalArgumentException("UID is required for update operations.");
            }

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
            if (uid == null) {
                throw new IllegalArgumentException("UID is required for delete operations.");
            }
            ePersonHandler.deleteEPerson(uid.getUidValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete ePerson: " + e.getMessage(), e);
        }
    }

    @Override
    public FilterTranslator<String> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME) || "eperson".equalsIgnoreCase(objectClass.getObjectClassValue())) {
            return new EPersonFilterTranslator();
        }
        throw new IllegalArgumentException("Unsupported object class: " + objectClass);
    }

    @Override
    public void executeQuery(ObjectClass objectClass, String query, ResultsHandler handler, OperationOptions options) {
        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new IllegalArgumentException("Unsupported object class: " + objectClass);
        }

        try {
            // Crear instancia de FilterHandler
            FilterHandler filterHandler = new FilterHandler();

            // Crear filtro desde la consulta
            Filter filter = createFilterFromQuery(query);

            // Validar el filtro
            filterHandler.validateFilter(filter);

            // Traducir el filtro a parámetros de consulta
            List<String> queryParams = filterHandler.translateFilter(filter);

            // Iterar sobre los parámetros traducidos y manejar resultados
            for (String queryParam : queryParams) {
                for (EPerson ePerson : ePersonHandler.getEPersons(filter)) {
                    // Construir el objeto ConnectorObject
                    ConnectorObject connectorObject = new ConnectorObjectBuilder()
                            .setUid(ePerson.getId())
                            .setName(ePerson.getEmail())
                            .addAttribute("firstname", ePerson.getFirstName())
                            .addAttribute("lastname", ePerson.getLastName())
                            .addAttribute("canLogIn", ePerson.isCanLogIn())
                            .build();

                    // Manejar el objeto resultante
                    handler.handle(connectorObject);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    private Filter createFilterFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return new EqualsFilter(new AttributeBuilder()
                    .setName("id")
                    .addValue("")
                    .build());
        }

        String[] parts = query.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid query format. Expected 'key:value'.");
        }

        String key = parts[0].trim();
        String value = parts[1].trim();

        if (key.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Key or value in the query is empty.");
        }

        return new EqualsFilter(new AttributeBuilder()
                .setName(key)
                .addValue(value)
                .build());
    }

    public void setAuthManager(AuthManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public void test() {
        try {
            validate(); // Validar configuración básica
            LOG.info("Probando autenticación...");
            TestUtil.validateAuthentication(authManager); // Llamada centralizada
            LOG.info("Probando conectividad...");
            TestUtil.validateConnection(client); // Llamada centralizada
            LOG.info("TestApiOp completado exitosamente.");
        } catch (Exception e) {
            LOG.error("Error en TestApiOp: " + e.getMessage(), e);
            throw new ConnectorException("TestApiOp fallido: " + e.getMessage(), e);
        }
    }

}
