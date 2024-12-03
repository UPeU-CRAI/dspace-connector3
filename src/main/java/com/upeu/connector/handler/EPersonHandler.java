package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import com.upeu.connector.util.EndpointRegistry;
import com.upeu.connector.util.ValidationJsonUtil;
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
        // Validación de atributos requeridos
        String email = ValidationJsonUtil.validateNotNull(
                AttributeUtil.getStringValue(AttributeUtil.find("email", attributes)),
                "El atributo 'email' es requerido."
        );
        String firstName = ValidationJsonUtil.validateNotNull(
                AttributeUtil.getStringValue(AttributeUtil.find("firstname", attributes)),
                "El atributo 'firstname' es requerido."
        );
        String lastName = ValidationJsonUtil.validateNotNull(
                AttributeUtil.getStringValue(AttributeUtil.find("lastname", attributes)),
                "El atributo 'lastname' es requerido."
        );

        // Crear payload utilizando las utilidades centralizadas
        JSONObject payload = new JSONObject();
        payload.put("email", email);
        payload.put("metadata", new JSONObject()
                .put("eperson.firstname", ValidationJsonUtil.createMetadataArray(firstName))
                .put("eperson.lastname", ValidationJsonUtil.createMetadataArray(lastName)));

        // Crear EPerson usando el método genérico de BaseHandler
        JSONObject response = create(EndpointRegistry.getEndpoint("epersons"), payload);

        // Validar y devolver el ID del nuevo recurso
        return new Uid(ValidationJsonUtil.validateNotNull(response.getString("id"), "La respuesta no contiene un ID."));
    }

    /**
     * Actualiza un EPerson existente.
     */
    public Uid update(String id, Set<Attribute> attributes) {
        // Validar ID
        ValidationJsonUtil.validateId(id, "El ID del EPerson es requerido para la actualización.");

        // Construir actualizaciones
        JSONObject updates = new JSONObject();
        attributes.forEach(attr -> updates.put(attr.getName(), AttributeUtil.getSingleValue(attr)));

        // Realizar la actualización
        JSONObject response = update(EndpointRegistry.getEndpoint("epersons"), id, updates);

        // Validar y devolver el ID actualizado
        return new Uid(ValidationJsonUtil.validateNotNull(response.getString("id"), "La respuesta no contiene un ID actualizado."));
    }

    /**
     * Elimina un EPerson.
     */
    public void delete(String id) {
        // Validar ID
        ValidationJsonUtil.validateId(id, "El ID del EPerson es requerido para eliminar.");

        // Realizar la eliminación
        super.delete(EndpointRegistry.getEndpoint("epersons"), id);
    }

    /**
     * Valida un objeto EPerson.
     */
    @Override
    protected boolean validate(Object entity) {
        return entity instanceof JSONObject && ((JSONObject) entity).has("email");
    }
}
