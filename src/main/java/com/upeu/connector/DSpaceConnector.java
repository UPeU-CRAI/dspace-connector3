package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.handler.EPerson;
import com.upeu.connector.handler.EPersonHandler;
import com.upeu.connector.handler.FilterHandler;
import com.upeu.connector.schema.EPersonSchema;
import com.upeu.connector.util.SchemaRegistry;
import com.upeu.connector.util.TestUtil;
import com.upeu.connector.util.ValidationUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
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

// ==============================
// Clase DSpaceConnector
// ==============================
// Esta es la clase principal del conector que implementa las interfaces necesarias para la integración con DSpace-CRIS
// usando MidPoint. Maneja autenticación, conectividad y operaciones CRUD para objetos "EPerson".
@ConnectorClass(configurationClass = DSpaceConfiguration.class, displayNameKey = "DSpaceConnector")
public class DSpaceConnector implements Connector, CreateOp, UpdateOp, DeleteOp, SearchOp<String>, SchemaOp, TestOp {

    private static final Logger LOG = LoggerFactory.getLogger(DSpaceConnector.class);

    private AuthManager authManager; // Maneja la autenticación contra el API de DSpace
    private DSpaceConfiguration configuration; // Configuración del conector
    private DSpaceClient client; // Cliente HTTP para interacciones con el API
    private EPersonHandler ePersonHandler; // Maneja operaciones relacionadas con "EPerson" en DSpace

    // ==============================
    // Inicialización y Validación
    // ==============================
    // Este bloque maneja la inicialización del conector, asegurando que la configuración
    // sea válida y estableciendo autenticación y conectividad con el API de DSpace.
    @Override
    public void init(Configuration configuration) {
        if (!(configuration instanceof DSpaceConfiguration)) {
            throw new IllegalArgumentException("Invalid configuration type. Expected DSpaceConfiguration.");
        }
        this.configuration = (DSpaceConfiguration) configuration;

        // Inicializar AuthManager para manejar la autenticación
        this.authManager = new AuthManager(
                this.configuration.getBaseUrl(),
                this.configuration.getUsername(),
                this.configuration.getPassword()
        );

        // Autenticación inicial: obtener el token JWT
        authManager.getJwtToken();

        // Inicializar el cliente HTTP con autenticación configurada
        this.client = new DSpaceClient(this.configuration, this.authManager);

        // Inicializar el manejador de EPerson
        this.ePersonHandler = new EPersonHandler(client);

        LOG.info("Conector DSpace inicializado correctamente.");
    }

    // ==============================
    // Método validate()
    // ==============================
    // Este método valida que la configuración y autenticación sean válidas
    // antes de ejecutar cualquier operación en el API de DSpace.
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
        LOG.info("Configuración y autenticación validadas con éxito.");
    }

    // ==============================
    // Métodos de Configuración y Esquema
    // ==============================
    // Este bloque incluye la definición del esquema del conector, limpieza de recursos
    // y obtención de la configuración actual.

    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(DSpaceConnector.class);

        // Delegar la definición de esquemas al registro centralizado
        SchemaRegistry.registerSchemas(schemaBuilder);

        LOG.info("Esquemas registrados correctamente.");
        return schemaBuilder.build();
    }

    @Override
    public void dispose() {
        // Liberar recursos asignados durante la ejecución
        client = null;
        ePersonHandler = null;
        LOG.info("Recursos del conector liberados correctamente.");
    }

    @Override
    public Configuration getConfiguration() {
        // Retornar la configuración actual del conector
        return this.configuration;
    }

    // ==============================
    // Operaciones CRUD Principales
    // ==============================
    // Este bloque incluye la implementación de las operaciones principales de creación,
    // actualización y eliminación de objetos "EPerson" en DSpace-CRIS.

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            // Delegar a EPersonHandler
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
            // Delegar la eliminación al EPersonHandler
            ePersonHandler.delete(uid.getUidValue());
            return;
        }
        throw new IllegalArgumentException("Unsupported object class: " + objectClass);
    }

    // ==============================
    // Operaciones de Búsqueda y Filtros
    // ==============================
    // Este bloque implementa la búsqueda de objetos EPerson en DSpace-CRIS,
    // manejando consultas y traducción de filtros.

    @Override
    public FilterTranslator<String> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME) || "eperson".equalsIgnoreCase(objectClass.getObjectClassValue())) {
            // Retornar un traductor específico para EPerson
            LOG.info("Creando traductor de filtros para EPerson.");
            return new EPersonFilterTranslator();
        }
        throw new IllegalArgumentException("Unsupported object class: " + objectClass);
    }

    @Override
    public void executeQuery(ObjectClass objectClass, String query, ResultsHandler handler, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            // Delegar la búsqueda al EPersonHandler
            ePersonHandler.search(query, handler);
            return;
        }
        throw new IllegalArgumentException("Unsupported object class: " + objectClass);
    }

    private Filter createFilterFromQuery(String query) {
        // Crear filtro desde una consulta básica (formato key:value)
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

        LOG.info("Filtro creado: key=" + key + ", value=" + value);
        return new EqualsFilter(new AttributeBuilder()
                .setName(key)
                .addValue(value)
                .build());
    }

    // ==============================
    // Pruebas de Conectividad y Autenticación del conector para Midpoint
    // ==============================
    // Este bloque implementa el método TestApiOp para validar que el conector
    // puede autenticarse y conectarse al API de DSpace-CRIS de forma exitosa.

    @Override
    public void test() {
        try {
            // Validar configuración y autenticación
            ValidationUtil.validateConfiguration(configuration);
            ValidationUtil.validateAuthentication(authManager);
            LOG.info("Validación de configuración y autenticación completada.");

            // Probar conectividad al API
            LOG.info("Probando conectividad...");
            TestUtil.validateConnection(client);
            LOG.info("Conexión con el API exitosa.");

            LOG.info("TestApiOp completado exitosamente.");
        } catch (RuntimeException e) {
            LOG.error("Error en TestApiOp: " + e.getMessage(), e);
            throw new ConnectorException("TestApiOp fallido: " + e.getMessage(), e);
        }
    }

}