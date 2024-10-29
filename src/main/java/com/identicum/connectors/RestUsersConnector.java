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

// Definición del conector REST, incluyendo las operaciones que implementa
@ConnectorClass(displayNameKey = "connector.identicum.rest.display", configurationClass = RestUsersConfiguration.class)
public class RestUsersConnector 
	extends AbstractRestConnector<RestUsersConfiguration> 
	implements CreateOp, UpdateOp, SchemaOp, SearchOp<RestUsersFilter>, DeleteOp, UpdateAttributeValuesOp, TestOp, TestApiOp {

    // Variables para el manejo del token JWT y su expiración
    private String jwtToken = null; // Para almacenar el token JWT actual
    private long tokenExpirationTime = 0; // Momento de expiración del token JWT en milisegundos

    
    // Definición de log y constantes para los endpoints de usuarios y roles
    private static final Log LOG = Log.getLog(RestUsersConnector.class);
    private static final String USERS_ENDPOINT = "/server/api/eperson/epersons";
    private static final String ROLES_ENDPOINT = "/roles";

    // Definición de los atributos manejados por el conector
    public static final String ATTR_FIRST_NAME = "eperson.firstname";
    public static final String ATTR_LAST_NAME = "eperson.lastname";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_USERNAME = "name";
    public static final String ATTR_ROLES = "roles";

    // Método para obtener el CSRF Token
    private String getCsrfToken() throws IOException {
        HttpGet csrfRequest = new HttpGet(getConfiguration().getServiceAddress() + "/server/api/authn/status");
        CloseableHttpResponse response = execute(csrfRequest);
        
        // Buscar en las cookies el token CSRF
        String csrfToken = null;
        for (Header header : response.getHeaders("Set-Cookie")) {
            if (header.getValue().contains("DSPACE-XSRF-COOKIE")) {
                csrfToken = header.getValue().split(";")[0].split("=")[1];
                break;
            }
        }
        closeResponse(response);
        if (csrfToken == null) {
            throw new ConnectorException("No se pudo obtener el CSRF Token");
        }
        return csrfToken;
    }

    // Método para obtener el JWT Token utilizando el CSRF Token con usuario y contraseña fijos
    private String authenticateAndGetJwtToken(String csrfToken) throws IOException {
        HttpPost loginRequest = new HttpPost(getConfiguration().getServiceAddress() + "/server/api/authn/login");
        loginRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
        loginRequest.setHeader("X-XSRF-TOKEN", csrfToken);
    
        List<NameValuePair> urlParameters = new ArrayList<>();
        
        // Obtener el usuario y contraseña desde la configuración del conector
        String username = getConfiguration().getUsername();
        GuardedString passwordGuarded = getConfiguration().getPassword();
        
        // Usar una clase anónima para extraer la contraseña en formato compatible con Java 7
        final StringBuilder clearPassword = new StringBuilder();
        passwordGuarded.access(new GuardedString.Accessor() {
            @Override
            public void access(char[] chars) {
                clearPassword.append(chars);
            }
        });
    
        urlParameters.add(new BasicNameValuePair("user", username));
        urlParameters.add(new BasicNameValuePair("password", clearPassword.toString()));
    
        loginRequest.setEntity(new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8));
    
        CloseableHttpResponse response = execute(loginRequest);
        Header authHeader = response.getFirstHeader("Authorization");
        if (authHeader == null) {
            throw new ConnectorException("No se recibió el token JWT en la respuesta.");
        }
        closeResponse(response);
    
        // Procesar el token y almacenar el tiempo de expiración
        String jwt = authHeader.getValue().replace("Bearer ", "");
        this.tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // Asumiendo 1 hora de validez
        return jwt;
    }

    // Método para verificar y renovar el token JWT si es necesario
    private void ensureAuthenticated() {
        if (jwtToken == null || System.currentTimeMillis() > tokenExpirationTime) {
            try {
                LOG.info("Autenticando para obtener un nuevo token JWT.");
                String csrfToken = getCsrfToken();
                this.jwtToken = authenticateAndGetJwtToken(csrfToken);
            } catch (IOException e) {
                throw new ConnectorException("Error al obtener el JWT Token", e);
            }
        }
    }


    // Método que define el esquema (schema) del conector: qué objetos y atributos maneja
    public Schema schema() {
        LOG.ok("Reading schema");
        SchemaBuilder schemaBuilder = new SchemaBuilder(RestUsersConnector.class);
        ObjectClassInfoBuilder accountBuilder = new ObjectClassInfoBuilder();
        accountBuilder.setType(ObjectClass.ACCOUNT_NAME);

        // Definición de los atributos para la clase de objeto ACCOUNT
        AttributeInfoBuilder attrUsername = new AttributeInfoBuilder(ATTR_USERNAME);
        attrUsername.setRequired(true);
        accountBuilder.addAttributeInfo(attrUsername.build());

        AttributeInfoBuilder attrEmail = new AttributeInfoBuilder(ATTR_EMAIL);
        attrEmail.setRequired(false);
        accountBuilder.addAttributeInfo(attrEmail.build());

        AttributeInfoBuilder attrFirstName = new AttributeInfoBuilder(ATTR_FIRST_NAME);
        attrFirstName.setRequired(true);
        accountBuilder.addAttributeInfo(attrFirstName.build());

        AttributeInfoBuilder attrLastName = new AttributeInfoBuilder(ATTR_LAST_NAME);
        attrLastName.setRequired(true);
        accountBuilder.addAttributeInfo(attrLastName.build());

        // Atributo "dummy" opcional, no funcional, solo como ejemplo
        AttributeInfoBuilder attrDummy = new AttributeInfoBuilder("dummy");
        attrDummy.setRequired(false);
        accountBuilder.addAttributeInfo(attrDummy.build());

        // Atributo multi-valor para los roles
        AttributeInfoBuilder attrRoles = new AttributeInfoBuilder(ATTR_ROLES);
        attrRoles.setMultiValued(true);
        attrRoles.setRequired(false);
        accountBuilder.addAttributeInfo(attrRoles.build());

	// Definir la clase de objeto Account
        schemaBuilder.defineObjectClass(accountBuilder.build());

        // Definir la clase de objeto Group (roles/grupos)
        ObjectClassInfoBuilder groupBuilder = new ObjectClassInfoBuilder();
        groupBuilder.setType(ObjectClass.GROUP_NAME);

        schemaBuilder.defineObjectClass(groupBuilder.build());

        LOG.ok("Exiting schema");
        return schemaBuilder.build();
    }

    // Operación de creación de un nuevo objeto (usuario o grupo)
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions operationOptions) {
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

    // Método callRequest para Usar el Token Dinámico
    protected JSONObject callRequest(HttpEntityEnclosingRequestBase request, JSONObject jo) {
        ensureAuthenticated(); // Verifica o renueva el token JWT si es necesario
        request.setHeader("Authorization", "Bearer " + this.jwtToken);
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

    // Método callRequest (Sobrecarga) para Usar el Token Dinámico
    protected String callRequest(HttpRequestBase request) throws IOException {
        ensureAuthenticated(); // Verifica o renueva el token JWT si es necesario
        request.setHeader("Authorization", "Bearer " + this.jwtToken);
        request.setHeader("Content-Type", "application/json");
    
        CloseableHttpResponse response = execute(request);
        super.processResponseErrors(response);
    
        String result = EntityUtils.toString(response.getEntity());
        closeResponse(response);
        return result;
    }    

    private void handleResponse(String responseString, ResultsHandler handler) throws IOException {
        if (responseString.startsWith("{")) {
            JSONObject jsonObject = new JSONObject(responseString);
            ConnectorObject connectorObject = convertUserToConnectorObject(jsonObject);
            handler.handle(connectorObject);
        } else if (responseString.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(responseString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ConnectorObject connectorObject = convertUserToConnectorObject(jsonObject);
                handler.handle(connectorObject);
            }
        } else {
            throw new ConnectorException("Unexpected response format");
        }
    }
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
    // Traducción del filtro de búsqueda
    @Override
    public FilterTranslator<RestUsersFilter> createFilterTranslator(ObjectClass arg0, OperationOptions arg1) {
        return new RestUsersFilterTranslator();
    }
    // Ejecución de consultas (búsquedas) en los usuarios y grupos
    @Override
    public void executeQuery(ObjectClass objectClass, RestUsersFilter query, ResultsHandler handler, OperationOptions options) {
        try {
            LOG.info("executeQuery on {0}, query: {1}, options: {2}", objectClass, query, options);
            if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
                if (query != null && query.byUid != null) {
                    HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + USERS_ENDPOINT + "/" + query.byUid);
                    JSONObject response = new JSONObject(callRequest(request));

                    ConnectorObject connectorObject = convertUserToConnectorObject(response);
                    LOG.info("Calling handler.handle on single object of AccountObjectClass");
                    handler.handle(connectorObject);
                    LOG.info("Called handler.handle on single object of AccountObjectClass");
                } else {
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
                if (query != null && query.byUid != null) {
                    HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + ROLES_ENDPOINT + "/" + query.byUid);
                    JSONObject response = new JSONObject(callRequest(request));

                    ConnectorObject connectorObject = convertRoleToConnectorObject(response);
                    LOG.info("Calling handler.handle on single object of GroupObjectClass");
                    handler.handle(connectorObject);
                    LOG.info("Called handler.handle on single object of GroupObjectClass");
                } else {
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

    // Modificar el método handleUsers para manejar la estructura de respuesta de DSpace 7
    private boolean handleUsers(HttpGet request, ResultsHandler handler, OperationOptions options, boolean findAll) throws IOException {
        String responseString = callRequest(request);
        LOG.ok("responseString: {0}", responseString);
    
        JSONObject responseObject = new JSONObject(responseString);
    
        if (responseObject.has("_embedded")) {
            JSONObject embedded = responseObject.getJSONObject("_embedded");
            if (embedded.has("epersons")) {
                JSONArray users = embedded.getJSONArray("epersons");
                LOG.ok("Número de usuarios: {0}", users.length());
    
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
    
    // Modificar el método convertUserToConnectorObject para mapear correctamente los atributos
    private ConnectorObject convertUserToConnectorObject(JSONObject user) throws IOException {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        if (!user.has("uuid")) {
            throw new ConnectorException("UUID no encontrado en el objeto JSON del usuario");
        }
        builder.setUid(new Uid(user.getString("uuid")));
        builder.setName(user.optString("name", "unknown"));
        addAttr(builder, ATTR_EMAIL, user.optString("email", null));
    
        // Obtener los metadatos
        if (user.has("metadata")) {
            JSONObject metadata = user.getJSONObject("metadata");
            addAttr(builder, ATTR_FIRST_NAME, getMetadataValue(metadata, "eperson.firstname"));
            addAttr(builder, ATTR_LAST_NAME, getMetadataValue(metadata, "eperson.lastname"));
        } else {
            LOG.warn("El usuario no tiene metadatos.");
        }
    
        ConnectorObject connectorObject = builder.build();
        LOG.ok("Usuario convertido a ConnectorObject: {0}", connectorObject);
        return connectorObject;
    }
    
    private String getMetadataValue(JSONObject metadata, String key) {
        if (metadata.has(key)) {
            JSONArray values = metadata.getJSONArray(key);
            if (values.length() > 0) {
                return values.getJSONObject(0).getString("value");
            }
        }
        return null;
    }
    

    private boolean handleRoles(HttpGet request, ResultsHandler handler, OperationOptions options, boolean findAll) throws IOException {
        JSONArray roles = new JSONArray(callRequest(request));
        LOG.ok("Number of roles: {0}", roles.length());

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
    // Conversión de un rol en un ConnectorObject para MidPoint
    private ConnectorObject convertRoleToConnectorObject(JSONObject role) throws IOException {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(role.get("id").toString()));
        builder.setName(role.getString("name"));

        ConnectorObject connectorObject = builder.build();
        LOG.ok("convertRoleToConnectorObject, role: {0}, \n\tconnectorObject: {1}", role.get("id").toString(), connectorObject);
        return connectorObject;
    }
    // Operación de eliminación de un usuario o grupo
    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        try {
            HttpDelete deleteReq = new HttpDelete(getConfiguration().getServiceAddress() + USERS_ENDPOINT + "/" + uid.getUidValue());
            callRequest(deleteReq);
        } catch (Exception io) {
            throw new RuntimeException("Error eliminando usuario por rest", io);
        }
    }
    // Método de prueba del conector para verificar que el servicio está disponible
    @Override
    public void test() {
        LOG.info("Entering test");
        try {
            HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + USERS_ENDPOINT);
            callRequest(request);
            LOG.info("Test OK");
        } catch (Exception io) {
            LOG.error("Error testing connector", io);
            throw new RuntimeException("Error testing endpoint", io);
        }
    }
}
