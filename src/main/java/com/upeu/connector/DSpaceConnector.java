package com.upeu.connector;

import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.handler.EPerson;
import com.upeu.connector.handler.EPersonHandler;
import com.upeu.connector.schema.EPersonSchema;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.operations.*;
import org.json.JSONObject;

import java.util.List;
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
    public void init(Configuration configuration) {
        if (!(configuration instanceof DSpaceConfiguration)) {
            throw new IllegalArgumentException("Invalid configuration type. Expected DSpaceConfiguration.");
        }
        this.configuration = (DSpaceConfiguration) configuration;
        this.client = new DSpaceClient(this.configuration);
        this.ePersonHandler = new EPersonHandler(client);
    }

    public void validate() {
        if (configuration == null || !configuration.isInitialized()) {
            throw new IllegalStateException("Configuration is not initialized.");
        }
        try {
            client.authenticate();
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
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
            // Traduce el query (String) a un Filter adecuado
            Filter filter = createFilterFromQuery(query);
            if (filter == null) {
                throw new IllegalArgumentException("Generated filter is null");
            }

            // Usa EPersonFilterTranslator para generar los parámetros de consulta
            EPersonFilterTranslator translator = new EPersonFilterTranslator();
            List<String> translatedQueries = translator.translate(filter);

            // Itera sobre las consultas y ejecuta la búsqueda en la API
            for (String queryParam : translatedQueries) {
                for (EPerson ePerson : ePersonHandler.getEPersons(filter)) { // Ahora pasamos el objeto Filter
                    ConnectorObject connectorObject = new ConnectorObjectBuilder()
                            .setUid(ePerson.getId())
                            .setName(ePerson.getEmail())
                            .addAttribute("firstname", ePerson.getFirstName())
                            .addAttribute("lastname", ePerson.getLastName())
                            .addAttribute("canLogIn", ePerson.canLogIn())
                            .build();
                    handler.handle(connectorObject);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    // Método utilitario para crear un Filter desde un String
    private Filter createFilterFromQuery(String query) {
        // Si el query es nulo o vacío, busca todos los objetos devolviendo un filtro genérico válido
        if (query == null || query.isEmpty()) {
            // Crea un filtro genérico "siempre verdadero" utilizando un atributo vacío
            return new EqualsFilter(new org.identityconnectors.framework.common.objects.AttributeBuilder()
                    .setName("id") // Usa "id" como atributo genérico, pero ajusta según tus requisitos
                    .addValue("")  // Valor vacío
                    .build());
        }

        // Divide el query en clave y valor
        String[] parts = query.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid query format. Expected 'key:value'.");
        }

        String key = parts[0].trim();
        String value = parts[1].trim();

        // Validación de clave y valor
        if (key.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Key or value in the query is empty.");
        }

        // Crear un EqualsFilter como ejemplo básico
        return new EqualsFilter(new org.identityconnectors.framework.common.objects.AttributeBuilder()
                .setName(key)
                .addValue(value)
                .build());
    }

    public void setClient(DSpaceClient client) {
        this.client = client;
        this.ePersonHandler = new EPersonHandler(client); // Actualiza el handler con el nuevo cliente
    }


}
