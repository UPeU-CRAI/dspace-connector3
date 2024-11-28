package com.upeu.connector.handler;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents an ePerson entity in the DSpace-CRIS system.
 */
public class EPerson {
    private final String id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final boolean canLogIn;

    /**
     * Constructor para crear un EPerson desde atributos individuales.
     *
     * @param id        ID del EPerson.
     * @param email     Correo electrónico del EPerson.
     * @param firstName Nombre del EPerson.
     * @param lastName  Apellido del EPerson.
     * @param canLogIn  Indica si el EPerson puede iniciar sesión.
     */
    public EPerson(String id, String email, String firstName, String lastName, boolean canLogIn) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.canLogIn = canLogIn;
    }

    /**
     * Constructor para crear un EPerson desde un objeto JSON.
     *
     * @param json Objeto JSON que contiene los datos del EPerson.
     */
    public EPerson(JSONObject json) {
        this.id = json.optString("id", null);
        this.email = json.optString("email", null);
        this.firstName = extractMetadataValue(json, "eperson.firstname");
        this.lastName = extractMetadataValue(json, "eperson.lastname");
        this.canLogIn = json.optBoolean("canLogIn", false); // Predeterminado: false
    }

    /**
     * Extrae un valor de metadatos de un objeto JSON.
     *
     * @param json Objeto JSON que contiene los metadatos.
     * @param key  Clave del metadato a extraer.
     * @return Valor del metadato o null si no está presente.
     */
    private String extractMetadataValue(JSONObject json, String key) {
        if (json == null || !json.has("metadata")) {
            return null;
        }
        JSONObject metadata = json.optJSONObject("metadata");
        if (metadata == null || !metadata.has(key)) {
            return null;
        }
        JSONArray valuesArray = metadata.optJSONArray(key);
        if (valuesArray != null && valuesArray.length() > 0) {
            return valuesArray.optJSONObject(0).optString("value", null);
        }
        return null;
    }

    // Getters para todos los atributos

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

    public boolean canLogIn() {
        return canLogIn;
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
