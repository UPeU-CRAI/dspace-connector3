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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

@ConnectorClass(displayNameKey = "connector.dspace.rest.display", configurationClass = DSpaceConnectorConfiguration.class)
public class DSpaceConnector
        extends AbstractRestConnector<DSpaceConnectorConfiguration>
        implements TestOp, CreateOp, UpdateOp, DeleteOp {

    private static final Logger LOG = LoggerFactory.getLogger(DSpaceConnector.class);

    private AuthenticationHandler authenticationHandler;
    private EPersonHandler ePersonHandler;
    private GroupHandler groupHandler;
    private ItemHandler itemHandler;

    private Endpoints endpoints;

    // =====================================
    // Initialization and Configuration
    // =====================================
    @Override
    public void init(Configuration config) {
        LOG.info("Inicializando el conector DSpace...");
        if (!(config instanceof DSpaceConnectorConfiguration)) {
            LOG.error("Clase de configuración inválida: {}", config.getClass().getName());
            throw new IllegalArgumentException("Clase de configuración inválida: " + config.getClass().getName());
        }

        DSpaceConnectorConfiguration dSpaceConfig = (DSpaceConnectorConfiguration) config;

        try {
            authenticationHandler = new AuthenticationHandler(dSpaceConfig);
            endpoints = new Endpoints(dSpaceConfig.getBaseUrl());
            ePersonHandler = new EPersonHandler(authenticationHandler, endpoints);
            groupHandler = new GroupHandler(authenticationHandler, endpoints);
            itemHandler = new ItemHandler(authenticationHandler, endpoints);
            LOG.info("Conector DSpace inicializado correctamente.");
        } catch (Exception e) {
            LOG.error("Error al inicializar el conector: {}", e.getMessage(), e);
            throw new ConnectorException("Error al inicializar el conector", e);
        }
    }

    // =====================================
    // Test Operation
    // =====================================
    @Override
    public void test() {
        LOG.info("Iniciando prueba de conexión con el servidor DSpace...");
        try {
            authenticationHandler.testConnection();
            LOG.info("Prueba de conexión exitosa.");
        } catch (Exception e) {
            LOG.error("Fallo en la prueba de conexión: {}", e.getMessage(), e);
            throw new ConnectorException("Prueba de conexión fallida: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Create Operation
    // =====================================
    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions options) {
        LOG.info("Iniciando operación de creación para la clase de objeto: {}", objectClass.getObjectClassValue());
        try {
            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                EPersonSchema ePersonSchema = mapToEPersonSchema(attributes);
                String ePersonId = ePersonHandler.createEPerson(ePersonSchema);
                LOG.info("EPerson creado exitosamente con ID: {}", ePersonId);
                return new Uid(ePersonId);
            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                GroupSchema groupSchema = mapToGroupSchema(attributes);
                String groupId = groupHandler.createGroup(groupSchema);
                LOG.info("Grupo creado exitosamente con ID: {}", groupId);
                return new Uid(groupId);
            } else if (objectClass.is("item")) {
                ItemSchema itemSchema = mapToItemSchema(attributes);
                String itemId = itemHandler.createItem(itemSchema);
                LOG.info("Ítem creado exitosamente con ID: {}", itemId);
                return new Uid(itemId);
            } else {
                LOG.warn("Clase de objeto no soportada: {}", objectClass.getObjectClassValue());
                throw new UnsupportedOperationException("Clase de objeto no soportada: " + objectClass.getObjectClassValue());
            }
        } catch (IOException e) {
            LOG.error("Error durante la operación de creación: {}", e.getMessage(), e);
            throw new ConnectorException("Error durante la operación de creación: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Update Operation
    // =====================================
    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
        LOG.info("Iniciando operación de actualización para la clase de objeto: {}, UID: {}", objectClass.getObjectClassValue(), uid.getUidValue());
        try {
            String uidValue = uid.getUidValue();

            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                EPersonSchema ePersonSchema = mapToEPersonSchema(replaceAttributes);
                ePersonHandler.updateEPerson(uidValue, ePersonSchema);
                LOG.info("EPerson actualizado exitosamente con UID: {}", uidValue);
                return uid;
            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                GroupSchema groupSchema = mapToGroupSchema(replaceAttributes);
                groupHandler.updateGroup(uidValue, groupSchema);
                LOG.info("Grupo actualizado exitosamente con UID: {}", uidValue);
                return uid;
            } else if (objectClass.is("item")) {
                ItemSchema itemSchema = mapToItemSchema(replaceAttributes);
                itemHandler.updateItem(uidValue, itemSchema);
                LOG.info("Ítem actualizado exitosamente con UID: {}", uidValue);
                return uid;
            } else {
                LOG.warn("Clase de objeto no soportada: {}", objectClass.getObjectClassValue());
                throw new UnsupportedOperationException("Clase de objeto no soportada: " + objectClass.getObjectClassValue());
            }
        } catch (IOException e) {
            LOG.error("Error durante la operación de actualización: {}", e.getMessage(), e);
            throw new ConnectorException("Error durante la operación de actualización: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Delete Operation
    // =====================================
    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        LOG.info("Iniciando operación de eliminación para la clase de objeto: {}, UID: {}", objectClass.getObjectClassValue(), uid.getUidValue());
        try {
            String uidValue = uid.getUidValue();

            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                ePersonHandler.deleteEPerson(uidValue);
                LOG.info("EPerson eliminado exitosamente con UID: {}", uidValue);
            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                groupHandler.deleteGroup(uidValue);
                LOG.info("Grupo eliminado exitosamente con UID: {}", uidValue);
            } else if (objectClass.is("item")) {
                itemHandler.deleteItem(uidValue);
                LOG.info("Ítem eliminado exitosamente con UID: {}", uidValue);
            } else {
                LOG.warn("Clase de objeto no soportada: {}", objectClass.getObjectClassValue());
                throw new UnsupportedOperationException("Clase de objeto no soportada: " + objectClass.getObjectClassValue());
            }
        } catch (IOException e) {
            LOG.error("Error durante la operación de eliminación: {}", e.getMessage(), e);
            throw new ConnectorException("Error durante la operación de eliminación: " + e.getMessage(), e);
        }
    }

    // =====================================
    // Helper Methods for Attribute Mapping
    // =====================================
    private EPersonSchema mapToEPersonSchema(Set<Attribute> attributes) {
        LOG.debug("Mapeando atributos para EPerson.");
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
                    LOG.warn("Atributo desconocido para EPerson: {}", attr.getName());
                    throw new IllegalArgumentException("Atributo desconocido: " + attr.getName());
            }
        }
        return schema;
    }

    private GroupSchema mapToGroupSchema(Set<Attribute> attributes) {
        LOG.debug("Mapeando atributos para Group.");
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
                    LOG.warn("Atributo desconocido para Group: {}", attr.getName());
                    throw new IllegalArgumentException("Atributo desconocido: " + attr.getName());
            }
        }
        return schema;
    }

    private ItemSchema mapToItemSchema(Set<Attribute> attributes) {
        LOG.debug("Mapeando atributos para Item.");
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
                    LOG.warn("Atributo desconocido para Item: {}", attr.getName());
                    throw new IllegalArgumentException("Atributo desconocido: " + attr.getName());
            }
        }
        return schema;
    }
}
