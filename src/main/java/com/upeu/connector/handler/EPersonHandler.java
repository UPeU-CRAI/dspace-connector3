package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import com.upeu.connector.util.EndpointRegistry;
import org.identityconnectors.framework.common.objects.*;
import org.json.JSONObject;

import java.util.Set;

/**
 * Handler para gestionar operaciones relacionadas con EPersons.
 */
public class EPersonHandler extends BaseHandler {

    /**
     * Constructor de EPersonHandler.
     *
     * @param dSpaceClient Instancia del cliente DSpace.
     */
    public EPersonHandler(DSpaceClient dSpaceClient) {
        super(dSpaceClient);
    }

    /**
     * Crea un nuevo EPerson.
     */
    public Uid create(Set<Attribute> attributes) {
        String email = AttributeUtil.getStringValue(AttributeUtil.find("email", attributes));
        String firstName = AttributeUtil.getStringValue(AttributeUtil.find("firstname", attributes));
        String lastName = AttributeUtil.getStringValue(AttributeUtil.find("lastname", attributes));

        // Crear payload
        JSONObject payload = new JSONObject();
        payload.put("email", email);
        payload.put("metadata", new JSONObject()
                .put("eperson.firstname", createMetadataArray(firstName))
                .put("eperson.lastname", createMetadataArray(lastName)));

        // Crear EPerson usando el método genérico de BaseHandler
        JSONObject response = create(EndpointRegistry.getEndpoint("epersons"), payload);
        return new Uid(response.getString("id"));
    }

    /**
     * Actualiza un EPerson existente.
     */
    public Uid update(String id, Set<Attribute> attributes) {
        JSONObject updates = new JSONObject();
        attributes.forEach(attr -> updates.put(attr.getName(), AttributeUtil.getSingleValue(attr)));

        JSONObject response = update(EndpointRegistry.getEndpoint("epersons"), id, updates);
        return new Uid(response.getString("id"));
    }

    /**
     * Elimina un EPerson.
     */
    public void delete(String id) {
        super.delete(EndpointRegistry.getEndpoint("epersons"), id);
    }

    /**
     * Valida un objeto EPerson.
     */
    @Override
    protected boolean validate(Object entity) {
        return entity instanceof JSONObject && ((JSONObject) entity).has("email");
    }

    /**
     * Crea un array de metadatos en formato esperado.
     */
    private JSONObject createMetadataArray(String value) {
        return new JSONObject().put("value", value);
    }
}
