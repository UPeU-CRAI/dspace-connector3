package com.identicum.connectors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.hc.core5.http.ParseException;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.*;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.*;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.common.exceptions.*;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.evolveum.polygon.rest.AbstractRestConnector;
import com.identicum.connectors.RestUsersConfiguration;
import com.identicum.connectors.RestUsersFilter;
import com.identicum.connectors.RestUsersFilterTranslator;


// ==============================
// Bloque de Definición del Conector
// ==============================

@ConnectorClass(displayNameKey = "connector.identicum.rest.display", configurationClass = RestUsersConfiguration.class)
public class RestUsersConnector extends AbstractRestConnector<RestUsersConfiguration>
    implements CreateOp, UpdateOp, DeleteOp, SchemaOp, SearchOp<RestUsersFilter>, UpdateAttributeValuesOp, TestOp, TestApiOp {

    private static final Log LOG = Log.getLog(RestUsersConnector.class);

    // ==============================
    // Bloque de Variables y Constantes
    // ==============================

    // Endpoints para usuarios y roles
    private static final String USERS_ENDPOINT = "/server/api/eperson/epersons";
    private static final String ROLES_ENDPOINT = "/server/api/eperson/groups";

    // Definición de atributos de usuario
public static final String ATTR_ID = "uuid";
public static final String ATTR_USERNAME = "name";
public static final String ATTR_EMAIL = "email";
public static final String ATTR_FIRST_NAME = "eperson.firstname";
public static final String ATTR_LAST_NAME = "eperson.lastname";
public static final String ATTR_CAN_LOG_IN = "canLogIn";

    // TokenManager para manejar la autenticación
    private TokenManager tokenManager;

    // Método para asegurar que TokenManager esté inicializado
    private void ensureTokenManagerInitialized() {
        if (tokenManager == null) {
            tokenManager = new TokenManager();
        }
    }

    // ==============================
    // Bloque de TokenManager y Autenticación
    // ==============================

    private class TokenManager {
        private String jwtToken;
        private long tokenExpirationTime;
        private final Object lock = new Object();
        private BasicCookieStore cookieStore;

        private String obtainCsrfToken() {
            String endpoint = getConfiguration().getServiceAddress() + "/server/api/authn/status";
            HttpGet request = new HttpGet(endpoint);

            // Manejar cookies
            cookieStore = new BasicCookieStore();
            HttpClientContext context = HttpClientContext.create();
            context.setCookieStore(cookieStore);

            try (CloseableHttpResponse response = getHttpClient().execute(request, context)) {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    List<Cookie> cookies = cookieStore.getCookies();
                    for (Cookie cookie : cookies) {
                        if ("DSPACE-XSRF-COOKIE".equals(cookie.getName())) {
                            String csrfToken = cookie.getValue();
                            LOG.info("CSRF Token obtenido.");
                            return csrfToken;
                        }
                    }
                    throw new ConnectorException("No se encontró el CSRF Token en las cookies");
                } else {
                    throw new ConnectorException("Error al obtener el CSRF Token, código de estado: " + statusCode);
                }
            } catch (IOException e) {
                throw new ConnectorException("Error al obtener el CSRF Token", e);
            }
        }

        private String obtainJwtToken() {
            String csrfToken = obtainCsrfToken();
            String endpoint = getConfiguration().getServiceAddress() + "/server/api/authn/login";
            HttpPost request = new HttpPost(endpoint);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setHeader("X-XSRF-TOKEN", csrfToken);

            // Obtener usuario y contraseña de la configuración
            String username = getConfiguration().getUsername();
            GuardedString passwordGuarded = getConfiguration().getPassword();
            final StringBuilder clearPassword = new StringBuilder();
            passwordGuarded.access(clearPassword::append);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("user", username));
            params.add(new BasicNameValuePair("password", clearPassword.toString()));
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            // Limpiar la contraseña de la memoria
            clearPassword.setLength(0);

            // Reutilizar el cookieStore y el contexto
            HttpClientContext context = HttpClientContext.create();
            context.setCookieStore(cookieStore);

            try (CloseableHttpResponse response = getHttpClient().execute(request, context)) {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    Header authHeader = response.getFirstHeader("Authorization");
                    if (authHeader != null) {
                        String authHeaderValue = authHeader.getValue();
                        if (authHeaderValue.startsWith("Bearer ")) {
                            String jwtToken = authHeaderValue.substring(7);
                            // Establecer el tiempo de expiración del token si es necesario
                            this.tokenExpirationTime = System.currentTimeMillis() + (3600 * 1000); // Asumiendo 1 hora
                            LOG.info("JWT Token obtenido.");
                            return jwtToken;
                        }
                    }
                    throw new ConnectorException("No se encontró el encabezado Authorization en la respuesta");
                } else {
                    throw new ConnectorException("Error al obtener el JWT token, código de estado: " + statusCode);
                }
            } catch (IOException e) {
                throw new ConnectorException("Error al obtener el JWT token", e);
            }
        }

        private void ensureAuthenticated() {
            synchronized (lock) {
                if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
                    jwtToken = obtainJwtToken();
                }
            }
        }

        public String getJwtToken() {
            ensureAuthenticated();
            return jwtToken;
        }
    }

    // ==============================
    // Bloque de Operaciones CRUD
    // ==============================

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions options) {
        ensureTokenManagerInitialized();
        LOG.ok("Entering create with ObjectClass: {0}", objectClass.getObjectClassValue());

        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new UnsupportedOperationException("Create operation is not supported for object class: " + objectClass.getObjectClassValue());
        }

        // Construir el objeto JSON con los atributos
        JSONObject jsonObject = new JSONObject();
        for (Attribute attr : attributes) {
            String attrName = attr.getName();
            Object attrValue = attr.getValue().get(0); // Asumiendo que es single-valued
            jsonObject.put(attrName, attrValue);
            LOG.ok("Added attribute {0}: {1}", attrName, attrValue);
        }

        // Realizar la solicitud HTTP POST
        String endpoint = getConfiguration().getServiceAddress() + USERS_ENDPOINT;
        HttpPost request = new HttpPost(endpoint);
        JSONObject response = callRequest(request, jsonObject);

        // Obtener el UID del nuevo usuario
        String uidValue = response.getString("uuid");
        LOG.ok("Created user with UID: {0}", uidValue);
        return new Uid(uidValue);
    }

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
        ensureTokenManagerInitialized();
        LOG.ok("Entering update with ObjectClass: {0}, UID: {1}", objectClass.getObjectClassValue(), uid.getUidValue());

        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new UnsupportedOperationException("Update operation is not supported for object class: " + objectClass.getObjectClassValue());
        }

        // Construir el objeto JSON con los atributos a actualizar
        JSONObject jsonObject = new JSONObject();
        for (Attribute attr : replaceAttributes) {
            String attrName = attr.getName();
            Object attrValue = attr.getValue().get(0); // Asumiendo que es single-valued
            jsonObject.put(attrName, attrValue);
            LOG.ok("Updating attribute {0}: {1}", attrName, attrValue);
        }

        // Realizar la solicitud HTTP PUT
        String endpoint = getConfiguration().getServiceAddress() + USERS_ENDPOINT + "/" + uid.getUidValue();
        HttpPut request = new HttpPut(endpoint);
        JSONObject response = callRequest(request, jsonObject);

        // Retornar el UID actualizado
        String uidValue = response.getString("uuid");
        LOG.ok("Updated user with UID: {0}", uidValue);
        return new Uid(uidValue);
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        ensureTokenManagerInitialized();
        LOG.ok("Entering delete with ObjectClass: {0}, UID: {1}", objectClass.getObjectClassValue(), uid.getUidValue());

        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new UnsupportedOperationException("Delete operation is not supported for object class: " + objectClass.getObjectClassValue());
        }

        // Realizar la solicitud HTTP DELETE
        String endpoint = getConfiguration().getServiceAddress() + USERS_ENDPOINT + "/" + uid.getUidValue();
        HttpDelete request = new HttpDelete(endpoint);
        try {
            String response = callRequest(request);
            LOG.ok("Deleted user with UID: {0}", uid.getUidValue());
        } catch (Exception e) {
            LOG.error("Error deleting user", e);
            throw new ConnectorException("Error deleting user with UID: " + uid.getUidValue(), e);
        }
    }

    @Override
    public Uid addAttributeValues(ObjectClass objectClass, Uid uid, Set<Attribute> valuesToAdd, OperationOptions options) {
        ensureTokenManagerInitialized();
        LOG.ok("Entering addAttributeValues with ObjectClass: {0}, UID: {1}", objectClass.getObjectClassValue(), uid.getUidValue());
    
        // Implementación específica para añadir valores a atributos multi-valor
        // Aquí puedes agregar la lógica para añadir roles u otros atributos
    
        // Por ahora, lanzamos una excepción indicando que no está implementado
        throw new UnsupportedOperationException("addAttributeValues operation is not implemented yet.");
    }
    
    @Override
    public Uid removeAttributeValues(ObjectClass objectClass, Uid uid, Set<Attribute> valuesToRemove, OperationOptions options) {
        ensureTokenManagerInitialized();
        LOG.ok("Entering removeAttributeValues with ObjectClass: {0}, UID: {1}", objectClass.getObjectClassValue(), uid.getUidValue());
    
        // Implementación específica para eliminar valores de atributos multi-valor
        // Aquí puedes agregar la lógica para eliminar roles u otros atributos
    
        // Por ahora, lanzamos una excepción indicando que no está implementado
        throw new UnsupportedOperationException("removeAttributeValues operation is not implemented yet.");
    }

    // ==============================
    // Bloque de Definición de Esquema
    // ==============================

    @Override
    public org.identityconnectors.framework.common.objects.Schema schema() {
        LOG.ok("Construyendo el esquema del conector");
        SchemaBuilder schemaBuilder = new SchemaBuilder(RestUsersConnector.class);

        // Definir ObjectClass para usuarios
        ObjectClassInfoBuilder userObjClassBuilder = new ObjectClassInfoBuilder();
        userObjClassBuilder.setType(ObjectClass.ACCOUNT_NAME);

        // Agregar atributos al ObjectClass de usuario
        userObjClassBuilder.addAttributeInfo(
            AttributeInfoBuilder.define(ATTR_ID)
                .setRequired(true)
                .setCreateable(false)
                .setUpdateable(false)
                .setReadable(true)
                .build()
        );

        userObjClassBuilder.addAttributeInfo(
            AttributeInfoBuilder.define(ATTR_USERNAME)
                .setRequired(true)
                .build()
        );

        userObjClassBuilder.addAttributeInfo(
            AttributeInfoBuilder.define(ATTR_EMAIL)
                .setRequired(true)
                .build()
        );

        userObjClassBuilder.addAttributeInfo(
            AttributeInfoBuilder.define(ATTR_FIRST_NAME)
                .build()
        );

        userObjClassBuilder.addAttributeInfo(
            AttributeInfoBuilder.define(ATTR_LAST_NAME)
                .build()
        );

        // Puedes agregar más atributos según sea necesario

        schemaBuilder.defineObjectClass(userObjClassBuilder.build());

        LOG.ok("Esquema del conector construido exitosamente");
        return schemaBuilder.build();
    }

    // ==============================
    // Bloque de Búsqueda y Consulta
    // ==============================

    @Override
    public FilterTranslator<RestUsersFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return new RestUsersFilterTranslator();
    }

    @Override
    public void executeQuery(ObjectClass objectClass, RestUsersFilter query, ResultsHandler handler, OperationOptions options) {
        ensureTokenManagerInitialized();
        LOG.ok("Executing query on ObjectClass: {0}", objectClass.getObjectClassValue());

        if (!objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            throw new UnsupportedOperationException("Search operation is not supported for object class: " + objectClass.getObjectClassValue());
        }

        try {
            String endpoint = getConfiguration().getServiceAddress() + USERS_ENDPOINT;
            if (query != null && query.byUid != null) {
                // Búsqueda por UID específico
                endpoint += "/" + query.byUid;
                HttpGet request = new HttpGet(endpoint);
                JSONObject response = new JSONObject(callRequest(request));

                ConnectorObject connectorObject = convertUserToConnectorObject(response);
                handler.handle(connectorObject);
            } else {
                // Búsqueda general
                HttpGet request = new HttpGet(endpoint);
                handleUsers(request, handler, options);
            }
        } catch (Exception e) {
            LOG.error("Error executing query", e);
            throw new ConnectorException("Error executing query", e);
        }
    }

    // Método para manejar la respuesta de usuarios
    private void handleUsers(HttpGet request, ResultsHandler handler, OperationOptions options) throws IOException {
        String responseString = callRequest(request);
        JSONObject responseObject = new JSONObject(responseString);

        if (responseObject.has("_embedded")) {
            JSONObject embedded = responseObject.getJSONObject("_embedded");
            if (embedded.has("epersons")) {
                JSONArray users = embedded.getJSONArray("epersons");
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    ConnectorObject connectorObject = convertUserToConnectorObject(user);
                    boolean continueProcessing = handler.handle(connectorObject);
                    if (!continueProcessing) {
                        break;
                    }
                }
            }
        }
    }

    // ==============================
    // Bloque de Conversión de Objetos
    // ==============================

    private ConnectorObject convertUserToConnectorObject(JSONObject user) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

        String uid = user.getString("uuid");
        builder.setUid(uid);
        builder.setName(user.getString("name"));

        // Agregar atributos
        builder.addAttribute(ATTR_EMAIL, user.optString("email", null));
        builder.addAttribute(ATTR_FIRST_NAME, getMetadataValue(user, ATTR_FIRST_NAME));
        builder.addAttribute(ATTR_LAST_NAME, getMetadataValue(user, ATTR_LAST_NAME));
        builder.addAttribute(ATTR_CAN_LOG_IN, user.optBoolean("canLogIn", true));

        // Agregar otros atributos según sea necesario

        return builder.build();
    }

    private String getMetadataValue(JSONObject user, String key) {
        if (user.has("metadata")) {
            JSONObject metadata = user.getJSONObject("metadata");
            if (metadata.has(key)) {
                JSONArray values = metadata.getJSONArray(key);
                if (values.length() > 0) {
                    return values.getJSONObject(0).getString("value");
                }
            }
        }
        return null;
    }

    // ==============================
    // Bloque de Manejo de Solicitudes HTTP
    // ==============================

    protected JSONObject callRequest(ClassicHttpRequest request, JSONObject jsonObject) {
        ensureTokenManagerInitialized();
        request.setHeader("Authorization", "Bearer " + tokenManager.getJwtToken());
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");

        try {
            StringEntity entity = new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);

            if (request instanceof HttpEntityContainer) {
                ((HttpEntityContainer) request).setEntity(entity);
            } else {
                throw new ConnectorException("Request does not support entity");
            }

            try (CloseableHttpResponse response = getHttpClient().execute(request)) {
                processResponseErrors(response);
                String result = EntityUtils.toString(response.getEntity());
                return new JSONObject(result);
            }
        } catch (IOException | ParseException e) {
            LOG.error("Error executing request", e);
            throw new ConnectorException("Error executing request", e);
        }
    }

    protected String callRequest(ClassicHttpRequest request) {
        ensureTokenManagerInitialized();
        request.setHeader("Authorization", "Bearer " + tokenManager.getJwtToken());
        request.setHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = getHttpClient().execute(request)) {
            processResponseErrors(response);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | ParseException e) {
            LOG.error("Error executing request", e);
            throw new ConnectorException("Error executing request", e);
        }
    }

    public void processResponseErrors(CloseableHttpResponse response) {
        int statusCode = response.getCode();
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
    
        String responseBody = null;
        try {
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException | ParseException e) {
            LOG.warn("Cannot read response body: {0}", e.getMessage());
        }
    
        String reasonPhrase = response.getReasonPhrase();
        String message = "HTTP error " + statusCode + ": " + reasonPhrase;
        if (responseBody != null) {
            message += ". Response body: " + responseBody;
        }
    
        LOG.error("{0}", message);
        // Manejo de errores según el código de estado
        switch (statusCode) {
            case 400:
                throw new ConnectorException(message);
            case 401:
            case 403:
                throw new PermissionDeniedException(message);
            case 404:
                throw new UnknownUidException(message);
            case 409:
                throw new AlreadyExistsException(message);
            default:
                throw new ConnectorException(message);
        }
    }
    

    // ==============================
    // Bloque de Prueba del Conector
    // ==============================

    @Override
    public void test() {
        ensureTokenManagerInitialized();
        LOG.ok("Iniciando prueba de conexión al servicio.");

        try {
            // Verificar que podemos obtener un token JWT
            String jwtToken = tokenManager.getJwtToken();
            LOG.ok("Token JWT obtenido exitosamente.");

            // Realizar una solicitud simple para verificar la conectividad
            String endpoint = getConfiguration().getServiceAddress() + USERS_ENDPOINT;
            HttpGet request = new HttpGet(endpoint);
            String response = callRequest(request);
            LOG.ok("Respuesta recibida durante la prueba: {0}", response);

            LOG.ok("Prueba de conexión exitosa.");
        } catch (Exception e) {
            LOG.error("Error durante la prueba de conexión", e);
            throw new ConnectorException("Error durante la prueba de conexión: " + e.getMessage(), e);
        }
    }
}