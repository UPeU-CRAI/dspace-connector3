package com.upeu.connector.handler;

import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.json.JSONObject;
import com.upeu.connector.util.JsonUtil;

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
        if (json == null) {
            throw new IllegalArgumentException("El JSON proporcionado no puede ser nulo.");
        }
        this.id = json.optString("id", null);
        this.email = json.optString("email", null);
        this.firstName = JsonUtil.extractMetadataValue(json.optJSONObject("metadata"), "eperson.firstname");
        this.lastName = JsonUtil.extractMetadataValue(json.optJSONObject("metadata"), "eperson.lastname");
        this.canLogIn = json.optBoolean("canLogIn", false);

        // Validación de campos obligatorios
        if (this.id == null || this.email == null || this.firstName == null || this.lastName == null) {
            throw new IllegalArgumentException("Campos obligatorios faltantes en el JSON del EPerson.");
        }
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
