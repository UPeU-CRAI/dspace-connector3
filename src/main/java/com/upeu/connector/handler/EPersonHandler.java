package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.util.EndpointUtil;
import com.upeu.connector.util.JsonUtil;
import com.upeu.connector.util.ValidationUtil;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Handler para gestionar operaciones relacionadas con EPersons.
 */
public class EPersonHandler extends BaseHandler {

    private static final Logger LOGGER = Logger.getLogger(EPersonHandler.class.getName());
    private final EPersonFilterTranslator filterTranslator;

    /**
     * Constructor de EPersonHandler.
     *
     * @param dSpaceClient Instancia del cliente DSpace.
     * @param endpointUtil Utilidad para construir endpoints.
     */
    public EPersonHandler(DSpaceClient dSpaceClient, EndpointUtil endpointUtil) {
        super(dSpaceClient, endpointUtil);
        this.filterTranslator = new EPersonFilterTranslator();
    }

    /**
     * Crea un nuevo EPerson.
     *
     * @param attributes Conjunto de atributos del nuevo EPerson.
     * @return UID del EPerson creado.
     */
    public Uid create(Set<Attribute> attributes) {
        try {
            // Validar y extraer atributos obligatorios
            String email = AttributeUtil.getStringValue(AttributeUtil.find("email", attributes));
            String firstName = AttributeUtil.getStringValue(AttributeUtil.find("firstname", attributes));
            String lastName = AttributeUtil.getStringValue(AttributeUtil.find("lastname", attributes));
            Boolean canLogIn = AttributeUtil.getBooleanValue(AttributeUtil.find("canLogIn", attributes));

            ValidationUtil.validateRequiredFields(email, firstName, lastName);
            ValidationUtil.validateEmail(email);

            // Crear el payload JSON
            JSONObject payload = new JSONObject();
            payload.put("email", email);
            payload.put("metadata", new JSONObject()
                    .put("eperson.firstname", JsonUtil.createMetadataArray(firstName))
                    .put("eperson.lastname", JsonUtil.createMetadataArray(lastName))
            );
            payload.put("canLogIn", canLogIn != null ? canLogIn : false);

            // Construir el endpoint usando EndpointUtil
            String endpoint = endpointUtil.getEpersonsEndpoint();

            // Llamar al método genérico de BaseHandler para realizar la creación
            JSONObject jsonResponse = create(endpoint, payload);

            // Validar la respuesta
            validateJsonResponse(jsonResponse, "id");

            // Retornar el UID del objeto creado
            return new Uid(jsonResponse.getString("id"));
        } catch (Exception e) {
            handleApiException("Error al crear EPerson", e);
            return null;
        }
    }

    /**
     * Actualiza un EPerson existente.
     *
     * @param id        ID del EPerson a actualizar.
     * @param attributes Atributos a actualizar.
     * @return UID actualizado del EPerson.
     */
    public Uid update(String id, Set<Attribute> attributes) {
        try {
            // Crear el objeto JSON con los datos actualizados
            JSONObject updates = new JSONObject();
            for (Attribute attribute : attributes) {
                updates.put(attribute.getName(), AttributeUtil.getSingleValue(attribute));
            }

            // Construir el endpoint usando EndpointUtil
            String endpoint = endpointUtil.getEpersonsEndpoint();

            // Usar el método genérico de BaseHandler para realizar la actualización
            JSONObject jsonResponse = update(endpoint, id, updates);

            // Validar la respuesta
            validateJsonResponse(jsonResponse, "id");

            // Retornar el UID actualizado
            return new Uid(jsonResponse.getString("id"));
        } catch (Exception e) {
            handleApiException("Error al actualizar el EPerson con ID: " + id, e);
            return null;
        }
    }

    /**
     * Elimina un EPerson.
     *
     * @param id ID del EPerson a eliminar.
     */
    public void delete(String id) {
        try {
            // Construir el endpoint usando EndpointUtil
            String endpoint = endpointUtil.getEpersonsEndpoint();
            delete(endpoint, id);
        } catch (Exception e) {
            handleApiException("Error al eliminar el EPerson con ID: " + id, e);
        }
    }

    /**
     * Busca EPersons utilizando filtros de MidPoint.
     *
     * @param query   Parámetro de consulta.
     * @param handler Handler para procesar los resultados.
     */
    public void search(String query, ResultsHandler handler) {
        try {
            // Construir el endpoint usando EndpointUtil
            String endpoint = endpointUtil.getEpersonsEndpoint();

            // Llama al cliente DSpace para obtener resultados
            List<JSONObject> results = dSpaceClient.search(endpoint, query);

            // Convierte cada resultado en un ConnectorObject y pásalo al handler
            for (JSONObject json : results) {
                ConnectorObject connectorObject = buildConnectorObject(json);
                if (!handler.handle(connectorObject)) {
                    break; // Detener si el handler devuelve false
                }
            }
        } catch (Exception e) {
            handleApiException("Error al buscar EPersons", e);
        }
    }

    /**
     * Construye un ConnectorObject a partir de un JSON de EPerson.
     *
     * @param json JSON del EPerson.
     * @return ConnectorObject construido.
     */
    private ConnectorObject buildConnectorObject(JSONObject json) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(json.getString("id"));
        builder.setName(json.optString("email", null));
        builder.addAttribute("firstname", JsonUtil.extractMetadataValue(json.optJSONObject("metadata"), "eperson.firstname"));
        builder.addAttribute("lastname", JsonUtil.extractMetadataValue(json.optJSONObject("metadata"), "eperson.lastname"));
        builder.addAttribute("canLogIn", json.optBoolean("canLogIn", false));
        return builder.build();
    }

    /**
     * Valida un objeto EPerson.
     *
     * @param entity Objeto a validar.
     * @return true si es válido, de lo contrario false.
     */
    @Override
    protected boolean validate(Object entity) {
        if (entity instanceof EPerson) {
            EPerson ePerson = (EPerson) entity;
            try {
                ValidationUtil.validateRequiredFields(
                        ePerson.getEmail(),
                        ePerson.getFirstName(),
                        ePerson.getLastName()
                );
                ValidationUtil.validateEmail(ePerson.getEmail());
                return true;
            } catch (IllegalArgumentException e) {
                LOGGER.severe("Validación fallida para EPerson: " + e.getMessage());
                return false;
            }
        }
        LOGGER.severe("El objeto no es una instancia de EPerson.");
        return false;
    }
}
