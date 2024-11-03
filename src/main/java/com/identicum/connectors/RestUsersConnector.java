package com.identicum.connectors;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.exceptions.PermissionDeniedException;
import org.identityconnectors.framework.common.exceptions.PreconditionFailedException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.json.JSONArray;
import org.json.JSONObject;

import com.evolveum.polygon.rest.AbstractRestConnector;

// ==============================
// Bloque de Definición del Conector
// ==============================
// Declaración de la clase principal del conector, indicando el nombre para MidPoint y la configuración que implementa.
@ConnectorClass(displayNameKey = "connector.identicum.rest.display", configurationClass = RestUsersConfiguration.class)
public class RestUsersConnector 
    extends AbstractRestConnector<RestUsersConfiguration> 
    implements CreateOp, UpdateOp, SchemaOp, SearchOp<RestUsersFilter>, DeleteOp, UpdateAttributeValuesOp, TestOp, TestApiOp {

    // ==============================
    // Bloque de Variables del Conector
    // ==============================
    // Definición de variables para el manejo de autenticación y configuración de endpoints.
    private String jwtToken = null; // Almacena el token JWT actual
    private long tokenExpirationTime = 0; // Expiración del token JWT en milisegundos
    private static final Log LOG = Log.getLog(RestUsersConnector.class);
    private static final String USERS_ENDPOINT = "/server/api/eperson/epersons";
    private static final String ROLES_ENDPOINT = "/roles";

    // Declaración de TokenManager para gestionar el JWT y CSRF Tokens
    private TokenManager tokenManager;

    // Asegura que TokenManager esté inicializado
    private void ensureTokenManagerInitialized() {
        if (tokenManager == null) {
            tokenManager = new TokenManager(getConfiguration().getServiceAddress());
        }
    }

    // Método para inicializar el TokenManager
    public void initializeTokenManager(RestUsersConfiguration configuration) {
        if (configuration != null) {
            tokenManager = new TokenManager(configuration.getServiceAddress());
        }
    }
    
    // ==============================
    // Bloque de Definición de Atributos
    // ==============================
    // Definición de los atributos que serán utilizados para la gestión de usuarios en DSpace.
    public static final String ATTR_ID = "uuid";
    public static final String ATTR_USERNAME = "username";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_FIRST_NAME = "eperson.firstname";
    public static final String ATTR_LAST_NAME = "eperson.lastname";
    public static final String ATTR_CAN_LOG_IN = "canLogIn";
    public static final String ATTR_LAST_ACTIVE = "lastActive";
    public static final String ATTR_REQUIRE_CERTIFICATE = "requireCertificate";
    public static final String ATTR_NET_ID = "netid";
    public static final String ATTR_SELF_REGISTERED = "selfRegistered";
    public static final String ATTR_ALERT_EMBARGO = "eperson.alert.embargo";
    public static final String ATTR_LANGUAGE = "eperson.language";
    public static final String ATTR_LICENSE_ACCEPTED = "eperson.license.accepted";
    public static final String ATTR_LICENSE_ACCEPTED_DATE = "eperson.license.accepteddate";
    public static final String ATTR_ORCID_SCOPE = "eperson.orcid.scope";
    public static final String ATTR_ORCID = "eperson.orcid";
    public static final String ATTR_PHONE = "eperson.phone";

    // ==============================
    // Clase Interna TokenManager
    // ==============================
    // Esta clase gestiona la generación, renovación y almacenamiento de los tokens
    // JWT y CSRF necesarios para la autenticación en el servidor DSpace.

    private class TokenManager {
        private String jwtToken = null; // Almacena el token JWT actual
        private long tokenExpirationTime = 0; // Expiración del token JWT en milisegundos
        private final String serviceAddress;

        // Constructor de TokenManager
        public TokenManager(String serviceAddress) {
            this.serviceAddress = serviceAddress;
        }

        // Método para obtener el CSRF Token
        private String getCsrfToken() throws IOException {
            LOG.info("Iniciando obtención del CSRF Token.");
            HttpGet csrfRequest = new HttpGet(serviceAddress + "/server/api/authn/status");
            CloseableHttpResponse response = execute(csrfRequest);

            String csrfToken = null;
            for (Header header : response.getHeaders("Set-Cookie")) {
                if (header.getValue().contains("DSPACE-XSRF-COOKIE")) {
                    csrfToken = header.getValue().split(";")[0].split("=")[1];
                    LOG.info("CSRF Token obtenido: {0}", csrfToken);
                    break;
                }
            }
            closeResponse(response);
            if (csrfToken == null) {
                LOG.error("No se pudo obtener el CSRF Token.");
                throw new ConnectorException("No se pudo obtener el CSRF Token");
            }
            return csrfToken;
        }

        // Método para obtener el JWT Token utilizando el CSRF Token
        private String authenticateAndGetJwtToken(String csrfToken) throws IOException {
            LOG.info("Iniciando autenticación para obtener el JWT Token.");
            HttpPost loginRequest = new HttpPost(serviceAddress + "/server/api/authn/login");
            loginRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
            loginRequest.setHeader("X-XSRF-TOKEN", csrfToken);

            List<NameValuePair> urlParameters = new ArrayList<>();
            String username = getConfiguration().getUsername();
            GuardedString passwordGuarded = getConfiguration().getPassword();
            final StringBuilder clearPassword = new StringBuilder();

            passwordGuarded.access(new GuardedString.Accessor() {
                @Override
                public void access(char[] chars) {
                    clearPassword.append(chars);
                }
            });

            LOG.info("Usuario para autenticación: {0}", username);
            urlParameters.add(new BasicNameValuePair("user", username));
            urlParameters.add(new BasicNameValuePair("password", clearPassword.toString()));
            loginRequest.setEntity(new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8));

            CloseableHttpResponse response = execute(loginRequest);
            Header authHeader = response.getFirstHeader("Authorization");
            if (authHeader == null) {
                LOG.error("No se recibió el token JWT en la respuesta.");
                throw new ConnectorException("No se recibió el token JWT en la respuesta.");
            }
            closeResponse(response);

            // Procesar y almacenar el tiempo de expiración del token JWT
            String jwt = authHeader.getValue().replace("Bearer ", "");
            this.tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // Asume 1 hora de validez
            LOG.info("JWT Token obtenido: {0}", jwt);
            LOG.info("Tiempo de expiración del token JWT: {0}", tokenExpirationTime);
            return jwt;
        }

        // Método para verificar y renovar el token JWT si es necesario
        private void ensureAuthenticated() {
            LOG.info("Verificando si el JWT Token está vigente.");
            if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                LOG.info("Token JWT inexistente o expirado. Iniciando autenticación.");
                try {
                    LOG.info("Autenticando para obtener un nuevo token JWT.");
                    String csrfToken = getCsrfToken();
                    this.jwtToken = authenticateAndGetJwtToken(csrfToken);
                    LOG.info("Nuevo token JWT obtenido y autenticación completada.");
                } catch (IOException e) {
                    LOG.error("Error al obtener el JWT Token", e);
                    throw new ConnectorException("Error al obtener el JWT Token", e);
                }
            } else {
                LOG.info("Token JWT vigente. No se requiere autenticación.");
            }
        }

        // Método para obtener el token JWT actual
        public String getJwtToken() {
            ensureAuthenticated(); // Asegura que el token esté actualizado
            return jwtToken;
        }
    }

    // ==============================
    // Bloque de Operaciones CRUD
    // ==============================
    // Este bloque agrupa los métodos CRUD principales para la gestión de usuarios
    // y grupos en DSpace, permitiendo la creación, actualización, adición, remoción,
    // y eliminación de atributos y usuarios.

    // Operación de creación de un nuevo objeto (usuario o grupo)
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions operationOptions) {
        // Asegura que el TokenManager esté inicializado
        ensureTokenManagerInitialized();

        LOG.ok("Entering create with objectClass: {0}", objectClass.toString());
        JSONObject response = null;
        JSONObject jo = new JSONObject();
        
        // Construir el objeto JSON con los atributos recibidos
        for (Attribute attr : attributes) {
            LOG.ok("Reading attribute {0} with value {1}", attr.getName(), attr.getValue());
            jo.put(attr.getName(), getStringAttr(attributes, attr.getName()));
        }
        
        // Definir el endpoint según el tipo de objeto (usuario o grupo)
        String endpoint = getConfiguration().getServiceAddress();
        if (ObjectClass.ACCOUNT.is(objectClass.getObjectClassValue())) {
            endpoint = endpoint.concat(USERS_ENDPOINT);
        } else if (ObjectClass.GROUP.is(objectClass.getObjectClassValue())) {
            endpoint = endpoint.concat(ROLES_ENDPOINT);
        } else {
            throw new ConnectorException("Unknown object class " + objectClass);
        }
        
        // Realizar la solicitud HTTP POST para crear el objeto
        HttpEntityEnclosingRequestBase request = new HttpPost(endpoint);
        response = callRequest(request, jo);

        String newUid = response.get("id").toString();
        LOG.info("response UID: {0}", newUid);
        return new Uid(newUid);
    }

    // Operación de actualización de un objeto (usuario o grupo)
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions operationOptions) {
        // Asegura que el TokenManager esté inicializado
        ensureTokenManagerInitialized();

        LOG.ok("Entering update with objectClass: {0}", objectClass.toString());
        JSONObject response = null;
        JSONObject jo = new JSONObject();
        
        // Construir el objeto JSON con los atributos modificados
        for (Attribute attribute : attributes) {
            LOG.info("Update - Atributo recibido {0}: {1}", attribute.getName(), attribute.getValue());
            jo.put(attribute.getName(), getStringAttr(attributes, attribute.getName()));
        }
        LOG.info("Delta a enviar por Rest: {0}", jo.toString());
        
        // Definir el endpoint adecuado para usuarios o grupos
        String endpoint = getConfiguration().getServiceAddress();
        if (ObjectClass.ACCOUNT.is(objectClass.getObjectClassValue())) {
            endpoint = endpoint.concat(USERS_ENDPOINT) + "/" + uid.getUidValue();
        } else if (ObjectClass.GROUP.is(objectClass.getObjectClassValue())) {
            endpoint = endpoint.concat(ROLES_ENDPOINT) + "/" + uid.getUidValue();
        } else {
            throw new ConnectorException("Unknown object class " + objectClass);
        }
        
        // Realizar la solicitud HTTP PATCH para actualizar el objeto
        try {
            HttpEntityEnclosingRequestBase request = new HttpPatch(endpoint);
            response = callRequest(request, jo);
        } catch (Exception io) {
            throw new RuntimeException("Error modificando usuario por rest", io);
        }
        
        // Devolver el UID del objeto actualizado
        String newUid = response.get("id").toString();
        LOG.info("response UID: {0}", newUid);
        return new Uid(newUid);
    }

    // Añadir valores a un atributo multi-valor (por ejemplo, añadir roles a un usuario)
    @Override
    public Uid addAttributeValues(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions operationOptions) {
        // Asegura que el TokenManager esté inicializado
        ensureTokenManagerInitialized();

        LOG.ok("Entering addValue with objectClass: {0}", objectClass.toString());
        try {
            for (Attribute attribute : attributes) {
                LOG.info("AddAttributeValue - Atributo recibido {0}: {1}", attribute.getName(), attribute.getValue());
                if (attribute.getName().equals("roles")) {
                    List<Object> addedRoles = attribute.getValue();
                    // Añadir cada rol individualmente
                    for (Object role : addedRoles) {
                        JSONObject json = new JSONObject();
                        json.put("id", role.toString());

                        String endpoint = String.format("%s/%s/%s/%s", getConfiguration().getServiceAddress(), USERS_ENDPOINT, uid.getUidValue(), ROLES_ENDPOINT);
                        LOG.info("Adding role {0} for user {1} on endpoint {2}", role.toString(), uid.getUidValue(), endpoint);
                        HttpEntityEnclosingRequestBase request = new HttpPost(endpoint);
                        callRequest(request, json);
                    }
                }
            }
        } catch (Exception io) {
            throw new RuntimeException("Error modificando usuario por rest", io);
        }
        return uid;
    }

    // Remover valores de un atributo multi-valor (por ejemplo, quitar roles a un usuario)
    @Override
    public Uid removeAttributeValues(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions operationOptions) {
        // Asegura que el TokenManager esté inicializado
        ensureTokenManagerInitialized();

        LOG.ok("Entering removeValue with objectClass: {0}", objectClass.toString());
        try {
            for (Attribute attribute : attributes) {
                LOG.info("RemoveAttributeValue - Atributo recibido {0}: {1}", attribute.getName(), attribute.getValue());
                if (attribute.getName().equals("roles")) {
                    List<Object> revokedRoles = attribute.getValue();
                    for (Object role : revokedRoles) {
                        String endpoint = String.format("%s/%s/%s/%s/%s", getConfiguration().getServiceAddress(), USERS_ENDPOINT, uid.getUidValue(), ROLES_ENDPOINT, role.toString());
                        LOG.info("Revoking role {0} for user {1} on endpoint {2}", role.toString(), uid.getUidValue(), endpoint);
                        HttpDelete request = new HttpDelete(endpoint);
                        callRequest(request);
                    }
                }
            }
        } catch (Exception io) {
            throw new RuntimeException("Error modificando usuario por rest", io);
        }
        return uid;
    }

    // Operación de eliminación de un usuario o grupo
    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        // Asegura que el TokenManager esté inicializado
        ensureTokenManagerInitialized();

        try {
            HttpDelete deleteReq = new HttpDelete(getConfiguration().getServiceAddress() + USERS_ENDPOINT + "/" + uid.getUidValue());
            callRequest(deleteReq);
        } catch (Exception io) {
            throw new RuntimeException("Error eliminando usuario por rest", io);
        }
    }

    // ==============================
    // Bloque de Manejo de Esquema
    // ==============================
    // Este bloque define el esquema de objetos gestionados por el conector,
    // especificando los atributos y clases de objetos (usuario o grupo).

    // Método que define el esquema (schema) del conector: qué objetos y atributos maneja
    public Schema schema() {
        LOG.ok("Reading schema");
        SchemaBuilder schemaBuilder = new SchemaBuilder(RestUsersConnector.class);
        ObjectClassInfoBuilder accountBuilder = new ObjectClassInfoBuilder();
        accountBuilder.setType(ObjectClass.ACCOUNT_NAME);

        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_ID).setRequired(true).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_USERNAME).setRequired(true).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_EMAIL).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_FIRST_NAME).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_LAST_NAME).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_CAN_LOG_IN).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_LAST_ACTIVE).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_REQUIRE_CERTIFICATE).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_NET_ID).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_SELF_REGISTERED).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_ALERT_EMBARGO).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_LANGUAGE).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_LICENSE_ACCEPTED).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_LICENSE_ACCEPTED_DATE).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_ORCID_SCOPE).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_ORCID).setRequired(false).build());
        accountBuilder.addAttributeInfo(new AttributeInfoBuilder(ATTR_PHONE).setRequired(false).build());

        schemaBuilder.defineObjectClass(accountBuilder.build());
        LOG.ok("Exiting schema");
        return schemaBuilder.build();
    }

    // ==============================
    // Bloque de Búsqueda y Consulta
    // ==============================
    // Este bloque permite traducir y ejecutar filtros de búsqueda,
    // así como manejar los resultados para usuarios y roles en DSpace.

    // Traducción del filtro de búsqueda
    @Override
    public FilterTranslator<RestUsersFilter> createFilterTranslator(ObjectClass arg0, OperationOptions arg1) {
        return new RestUsersFilterTranslator();
    }

    // Ejecución de consultas (búsquedas) en los usuarios y grupos
    @Override
    public void executeQuery(ObjectClass objectClass, RestUsersFilter query, ResultsHandler handler, OperationOptions options) {
        // Asegura que el TokenManager esté inicializado
        ensureTokenManagerInitialized();        

        try {
            LOG.info("executeQuery on {0}, query: {1}, options: {2}", objectClass, query, options);
            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                // Consulta específica por UID de usuario
                if (query != null && query.byUid != null) {
                    HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + USERS_ENDPOINT + "/" + query.byUid);
                    JSONObject response = new JSONObject(callRequest(request));

                    ConnectorObject connectorObject = convertUserToConnectorObject(response);
                    LOG.info("Calling handler.handle on single object of AccountObjectClass");
                    handler.handle(connectorObject);
                    LOG.info("Called handler.handle on single object of AccountObjectClass");
                } else {
                    // Consulta de múltiples usuarios con posibles filtros
                    String filters = new String();
                    if (query != null && StringUtil.isNotBlank(query.byUsername)) {
                        filters = "?username=" + query.byUsername;
                    }
                    HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + USERS_ENDPOINT + filters);
                    LOG.info("Calling handleUsers for multiple objects of AccountObjectClass");
                    handleUsers(request, handler, options, false);
                    LOG.info("Called handleUsers for multiple objects of AccountObjectClass");
                }
            } else if (objectClass.is(ObjectClass.GROUP_NAME)) {
                // Consulta específica por UID de grupo
                if (query != null && query.byUid != null) {
                    HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + ROLES_ENDPOINT + "/" + query.byUid);
                    JSONObject response = new JSONObject(callRequest(request));

                    ConnectorObject connectorObject = convertRoleToConnectorObject(response);
                    LOG.info("Calling handler.handle on single object of GroupObjectClass");
                    handler.handle(connectorObject);
                    LOG.info("Called handler.handle on single object of GroupObjectClass");
                } else {
                    // Consulta de múltiples grupos con posibles filtros
                    String filters = new String();
                    if (query != null && StringUtil.isNotBlank(query.byName)) {
                        filters = "?name=" + query.byName;
                    }
                    HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + ROLES_ENDPOINT + filters);
                    LOG.info("Calling handleRoles for multiple objects of GroupObjectClass");
                    handleRoles(request, handler, options, false);
                    LOG.info("Called handleRoles for multiple objects of GroupObjectClass");
                }
            }
        } catch (IOException e) {
            LOG.error("Error querying objects on Rest Resource", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // ==============================
    // Bloque de Métodos Auxiliares para Manejo de Respuestas
    // ==============================
    // Este bloque define métodos auxiliares para manejar y convertir las
    // respuestas de usuarios y roles, transformándolos en objetos utilizables
    // por MidPoint (ConnectorObject).

    // Método para manejar la estructura de respuesta de DSpace para usuarios
    private boolean handleUsers(HttpGet request, ResultsHandler handler, OperationOptions options, boolean findAll) throws IOException {
        String responseString = callRequest(request);
        LOG.ok("responseString: {0}", responseString);
    
        JSONObject responseObject = new JSONObject(responseString);
    
        if (responseObject.has("_embedded")) {
            JSONObject embedded = responseObject.getJSONObject("_embedded");
            if (embedded.has("epersons")) {
                JSONArray users = embedded.getJSONArray("epersons");
                LOG.ok("Número de usuarios: {0}", users.length());
    
                // Procesar cada usuario en la respuesta
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    ConnectorObject connectorObject = convertUserToConnectorObject(user);
                    LOG.info("Llamando handler.handle en iteración #{0}", i);
                    boolean finish = !handler.handle(connectorObject);
                    LOG.info("handler.handle llamado en iteración #{0}", i);
                    if (finish) {
                        return true;
                    }
                }
            } else {
                LOG.error("No se encontraron usuarios en la respuesta.");
                throw new ConnectorException("No se encontraron usuarios en la respuesta.");
            }
        } else {
            LOG.error("Formato de respuesta inesperado al manejar usuarios.");
            throw new ConnectorException("Formato de respuesta inesperado al manejar usuarios.");
        }
        return false;
    }

    // Método para convertir datos de usuario en un ConnectorObject
    private ConnectorObject convertUserToConnectorObject(JSONObject user) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        
        builder.setUid(new Uid(user.getString("uuid")));
        builder.setName(user.optString("name", "unknown"));
        addAttr(builder, ATTR_EMAIL, user.optString("email", null));
        addAttr(builder, ATTR_CAN_LOG_IN, user.optBoolean("canLogIn", false));
        addAttr(builder, ATTR_LAST_ACTIVE, user.optString("lastActive", null));
        addAttr(builder, ATTR_REQUIRE_CERTIFICATE, user.optBoolean("requireCertificate", false));
        addAttr(builder, ATTR_NET_ID, user.optString("netid", null));
        addAttr(builder, ATTR_SELF_REGISTERED, user.optBoolean("selfRegistered", false));
        
        // Manejo de metadatos adicionales en la estructura JSON
        if (user.has("metadata")) {
            JSONObject metadata = user.getJSONObject("metadata");
            addAttr(builder, ATTR_FIRST_NAME, getMetadataValue(metadata, "eperson.firstname"));
            addAttr(builder, ATTR_LAST_NAME, getMetadataValue(metadata, "eperson.lastname"));
            addAttr(builder, ATTR_LANGUAGE, getMetadataValue(metadata, "eperson.language"));
            addAttr(builder, ATTR_ALERT_EMBARGO, getMetadataValue(metadata, "eperson.alert.embargo"));
            addAttr(builder, ATTR_LICENSE_ACCEPTED, getMetadataValue(metadata, "eperson.license.accepted"));
            addAttr(builder, ATTR_LICENSE_ACCEPTED_DATE, getMetadataValue(metadata, "eperson.license.accepteddate"));
            addAttr(builder, ATTR_ORCID_SCOPE, getMetadataValue(metadata, "eperson.orcid.scope"));
            addAttr(builder, ATTR_ORCID, getMetadataValue(metadata, "eperson.orcid"));
            addAttr(builder, ATTR_PHONE, getMetadataValue(metadata, "eperson.phone"));
        }

        return builder.build();
    }

    // Método para extraer valores específicos de metadatos en JSON
    private String getMetadataValue(JSONObject metadata, String key) {
        if (metadata.has(key)) {
            JSONArray values = metadata.getJSONArray(key);
            if (values.length() > 0) {
                return values.getJSONObject(0).getString("value");
            }
        }
        return null;
    }

    // Método para manejar la estructura de respuesta de DSpace para roles
    private boolean handleRoles(HttpGet request, ResultsHandler handler, OperationOptions options, boolean findAll) throws IOException {
        JSONArray roles = new JSONArray(callRequest(request));
        LOG.ok("Number of roles: {0}", roles.length());

        // Procesar cada rol en la respuesta
        for (int i = 0; i < roles.length(); i++) {
            JSONObject role = roles.getJSONObject(i);
            ConnectorObject connectorObject = convertRoleToConnectorObject(role);
            boolean finish = !handler.handle(connectorObject);
            if (finish) {
                return true;
            }
        }
        return false;
    }

    // Método para convertir datos de rol en un ConnectorObject para MidPoint
    private ConnectorObject convertRoleToConnectorObject(JSONObject role) throws IOException {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(role.get("id").toString()));
        builder.setName(role.getString("name"));

        ConnectorObject connectorObject = builder.build();
        LOG.ok("convertRoleToConnectorObject, role: {0}, \n\tconnectorObject: {1}", role.get("id").toString(), connectorObject);
        return connectorObject;
    }

    // ==============================
    // Bloque de Manejo de Solicitudes HTTP
    // ==============================
    // Este bloque contiene métodos para realizar solicitudes HTTP autenticadas,
    // así como el manejo de respuestas y posibles errores.

    // Método callRequest para realizar solicitudes autenticadas usando un JSON
    protected JSONObject callRequest(HttpEntityEnclosingRequestBase request, JSONObject jo) {
        request.setHeader("Authorization", "Bearer " + tokenManager.getJwtToken()); // Usa TokenManager
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        try {
            HttpEntity entity = new ByteArrayEntity(StringUtils.getBytesUtf8(jo.toString()));
            request.setEntity(entity);
            CloseableHttpResponse response = execute(request);
            this.processResponseErrors(response);

            String result = EntityUtils.toString(response.getEntity());
            closeResponse(response);
            return new JSONObject(result);
        } catch (IOException e) {
            throw new ConnectorException("Error en la solicitud API", e);
        }
    }

    // Método callRequest (Sobrecarga) para realizar solicitudes autenticadas sin JSON
    protected String callRequest(HttpRequestBase request) throws IOException {
        request.setHeader("Authorization", "Bearer " + tokenManager.getJwtToken()); // Usa TokenManager
        request.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = execute(request);
        super.processResponseErrors(response);

        String result = EntityUtils.toString(response.getEntity());
        closeResponse(response);
        return result;
    }

    // ==============================
    // Bloque de Manejo de Errores
    // ==============================
    // Este bloque define cómo manejar los errores HTTP basados en el código de estado
    // de la respuesta y lanza excepciones adecuadas para cada caso.

    // Manejo de errores en la respuesta del servidor
    public void processResponseErrors(CloseableHttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 200 && statusCode <= 299) {
            return;
        }
        String responseBody = null;
        try {
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            LOG.warn("Cannot read response body: " + e, e);
        }

        String message = "HTTP error " + statusCode + " " + response.getStatusLine().getReasonPhrase() + " : " + responseBody;
        LOG.error("{0}", message);
        if (statusCode == 400 || statusCode == 405 || statusCode == 406) {
            closeResponse(response);
            throw new ConnectorIOException(message);
        }
        if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 407) {
            closeResponse(response);
            throw new PermissionDeniedException(message);
        }
        if (statusCode == 404 || statusCode == 410) {
            closeResponse(response);
            throw new UnknownUidException(message);
        }
        if (statusCode == 408) {
            closeResponse(response);
            throw new OperationTimeoutException(message);
        }
        if (statusCode == 409) {
            closeResponse(response);
            throw new AlreadyExistsException();
        }
        if (statusCode == 412) {
            closeResponse(response);
            throw new PreconditionFailedException(message);
        }
        if (statusCode == 418) {
            closeResponse(response);
            throw new UnsupportedOperationException("Sorry, no coffee: " + message);
        }
        closeResponse(response);
        throw new ConnectorException(message);
    }

    // ==============================
    // Bloque de Prueba del Conector
    // ==============================
    // Este bloque permite verificar que el servicio está disponible,
    // realizando una solicitud HTTP GET al endpoint de usuarios.

    // Método de prueba del conector para verificar que el servicio está disponible
    @Override
    public void test() {
        // Asegura que el TokenManager esté inicializado
        ensureTokenManagerInitialized();

        LOG.info("Iniciando prueba de conexión en el método test().");
    
        try {
            // Comprobar si la configuración está presente y válida
            if (getConfiguration() == null) {
                LOG.error("La configuración no está inicializada.");
                throw new ConnectorException("Configuración no inicializada. Verifica la configuración del conector.");
            }
    
            // Inicializar TokenManager si es nulo
            if (tokenManager == null) {
                LOG.info("TokenManager no inicializado, procediendo con la inicialización.");
                tokenManager = new TokenManager(getConfiguration().getServiceAddress());
            }
    
            // Verificar la URL del servicio
            String serviceAddress = getConfiguration().getServiceAddress();
            if (serviceAddress == null || serviceAddress.isEmpty()) {
                LOG.error("La URL del servicio no está configurada.");
                throw new ConnectorException("La URL del servicio no está configurada. Por favor, verifica la configuración.");
            }
    
            LOG.info("Service URL: {0}", serviceAddress);
    
            // Intentar obtener el token JWT usando TokenManager
            LOG.info("Probando autenticación mediante obtención de JWT Token.");
            String jwtToken = tokenManager.getJwtToken();
            LOG.info("Token JWT obtenido exitosamente: {0}", jwtToken);
    
            // Realizar una solicitud GET básica al endpoint de usuarios para verificar la conectividad
            HttpGet request = new HttpGet(serviceAddress + USERS_ENDPOINT);
            LOG.info("Enviando solicitud GET al endpoint de prueba: {0}", USERS_ENDPOINT);
    
            String response = callRequest(request);
            LOG.info("Respuesta del servidor obtenida: {0}", response);
    
            // Confirmar que la prueba ha sido exitosa
            LOG.info("Prueba de conexión y autenticación exitosa.");
    
        } catch (ConnectorException e) {
            LOG.error("Error en el test de conexión: {0}", e.getMessage());
            throw e; // Re-lanzar la excepción para que MidPoint la capture
        } catch (Exception e) {
            LOG.error("Error inesperado durante el test de conexión: {0}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado durante el test de conexión", e);
        }
    }    
}