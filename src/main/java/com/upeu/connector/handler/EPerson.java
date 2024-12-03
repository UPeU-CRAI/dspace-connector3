package com.upeu.connector.handler;

import com.upeu.connector.util.ValidationJsonUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.json.JSONObject;

/**
 * Clase que representa un EPerson en DSpace.
 */
public class EPerson {

    private final String id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final boolean canLogIn;

    /**
     * Constructor que inicializa un EPerson desde un JSON.
     *
     * @param json Objeto JSON que contiene los datos de EPerson.
     */
    public EPerson(JSONObject json) {
        // Validar que el JSON no sea nulo
        ValidationJsonUtil.validateNotNull(json, "El JSON proporcionado no puede ser nulo.");

        // Extraer y validar campos obligatorios
        this.id = json.optString("id", null);
        this.email = json.optString("email", null);
        this.firstName = ValidationJsonUtil.extractMetadataValue(json, "eperson.firstname");
        this.lastName = ValidationJsonUtil.extractMetadataValue(json, "eperson.lastname");
        this.canLogIn = json.optBoolean("canLogIn", false);

        // Validación de campos obligatorios
        ValidationJsonUtil.validateNotEmpty(this.id, "El campo 'id' es obligatorio en el JSON del EPerson.");
        ValidationJsonUtil.validateNotEmpty(this.email, "El campo 'email' es obligatorio en el JSON del EPerson.");
        ValidationJsonUtil.validateNotEmpty(this.firstName, "El campo 'firstname' es obligatorio en el JSON del EPerson.");
        ValidationJsonUtil.validateNotEmpty(this.lastName, "El campo 'lastname' es obligatorio en el JSON del EPerson.");
    }

    // ==============================
    // Getters para los atributos
    // ==============================
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isCanLogIn() {
        return canLogIn;
    }

    /**
     * Convierte este EPerson en un ConnectorObject.
     *
     * @return ConnectorObject construido a partir de los atributos de EPerson.
     */
    public ConnectorObject toConnectorObject() {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(this.id);
        builder.setName(this.email); // Usamos el email como nombre único
        builder.addAttribute("firstname", this.firstName);
        builder.addAttribute("lastname", this.lastName);
        builder.addAttribute("canLogIn", this.canLogIn);
        return builder.build();
    }

    @Override
    public String toString() {
        return "EPerson{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", canLogIn=" + canLogIn +
                '}';
    }
}
