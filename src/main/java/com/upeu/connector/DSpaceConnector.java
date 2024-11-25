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

    private DSpaceConfiguration configuration; // Almacena la configuración
    private DSpaceClient client; // Cliente DSpace
    private EPersonHandler ePersonHandler; // Handler para gestionar ePerson

    @Override
    public void init(Configuration configuration) {
        if (!(configuration instanceof DSpaceConfiguration)) {
            throw new IllegalArgumentException("Invalid configuration type. Expected DSpaceConfiguration.");
        }
        this.configuration = (DSpaceConfiguration) configuration;
        this.client = new DSpaceClient(this.configuration); // Inicializa el cliente con la configuración
        this.ePersonHandler = new EPersonHandler(client); // Inicializa el handler con el cliente
    }

    // Método para delegar la llamada GET al cliente DSpace
    public String get(String endpoint) throws Exception {
        if (client == null) {
            throw new IllegalStateException("DSpaceClient is not initialized");
        }
        return client.get(endpoint);
    }

    // Método para inyectar el cliente DSpace (para pruebas)
    public void setClient(DSpaceClient client) {
        this.client = client;
    }

    public void validate() throws Exception { // Declarar que el método puede lanzar Exception
        if (configuration == null || !configuration.isInitialized()) {
            throw new IllegalStateException("Configuration is not initialized");
        }

        if (client == null) {
            throw new IllegalStateException("DSpaceClient is not initialized");
        }

        // Realiza la autenticación del cliente
        client.authenticate(); // Si falla, propagará la excepción
    }

    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(DSpaceConnector.class);
        EPersonSchema.define(schemaBuilder); // Define la estructura del esquema para ePerson
        return schemaBuilder.build();
    }

    @Override
    public void dispose() {
        // Libera recursos si es necesario
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
