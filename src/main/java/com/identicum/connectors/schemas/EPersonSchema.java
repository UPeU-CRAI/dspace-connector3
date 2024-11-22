package com.identicum.schemas;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the schema for an EPerson object in DSpace.
 * Handles serialization and deserialization from/to JSON.
 */
public class EPersonSchema {

    private static final Logger LOG = LoggerFactory.getLogger(EPersonSchema.class);

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean canLogIn;
    private boolean requireCertificate;

    // =====================================
    // Constructors
    // =====================================
    public EPersonSchema() {
        LOG.debug("EPersonSchema creado con valores por defecto.");
    }

    public EPersonSchema(String id, String email, String firstName, String lastName, boolean canLogIn, boolean requireCertificate) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.canLogIn = canLogIn;
        this.requireCertificate = requireCertificate;
        LOG.info("EPersonSchema creado con ID: {}, Email: {}", id, email);
    }

    // =====================================
    // Getters and Setters
    // =====================================
    public String getId() {
        LOG.debug("Obteniendo ID: {}", id);
        return id;
    }

    public void setId(String id) {
        LOG.debug("Estableciendo ID: {}", id);
        this.id = id;
    }

    public String getEmail() {
        LOG.debug("Obteniendo Email: {}", email);
        return email;
    }

    public void setEmail(String email) {
        LOG.debug("Estableciendo Email: {}", email);
        this.email = email;
    }

    public String getFirstName() {
        LOG.debug("Obteniendo Nombre: {}", firstName);
        return firstName;
    }

    public void setFirstName(String firstName) {
        LOG.debug("Estableciendo Nombre: {}", firstName);
        this.firstName = firstName;
    }

    public String getLastName() {
        LOG.debug("Obteniendo Apellido: {}", lastName);
        return lastName;
    }

    public void setLastName(String lastName) {
        LOG.debug("Estableciendo Apellido: {}", lastName);
        this.lastName = lastName;
    }

    public boolean isCanLogIn() {
        LOG.debug("Obteniendo estado de canLogIn: {}", canLogIn);
        return canLogIn;
    }

    public void setCanLogIn(boolean canLogIn) {
        LOG.debug("Estableciendo canLogIn: {}", canLogIn);
        this.canLogIn = canLogIn;
    }

    public boolean isRequireCertificate() {
        LOG.debug("Obteniendo estado de requireCertificate: {}", requireCertificate);
        return requireCertificate;
    }

    public void setRequireCertificate(boolean requireCertificate) {
        LOG.debug("Estableciendo requireCertificate: {}", requireCertificate);
        this.requireCertificate = requireCertificate;
    }

    // =====================================
    // Serialization to JSON
    // =====================================
    public JSONObject toJson() {
        LOG.info("Serializando EPersonSchema a JSON.");
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("email", email);
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("canLogIn", canLogIn);
        json.put("requireCertificate", requireCertificate);
        LOG.debug("EPersonSchema serializado: {}", json);
        return json;
    }

    // =====================================
    // Deserialization from JSON
    // =====================================
    public static EPersonSchema fromJson(JSONObject json) {
        LOG.info("Deserializando JSON a EPersonSchema.");
        EPersonSchema schema = new EPersonSchema(
                json.optString("id"),
                json.optString("email"),
                json.optString("firstName"),
                json.optString("lastName"),
                json.optBoolean("canLogIn"),
                json.optBoolean("requireCertificate")
        );
        LOG.debug("EPersonSchema deserializado: ID={}, Email={}", schema.getId(), schema.getEmail());
        return schema;
    }

    public void setUsername(String username) {
        LOG.debug("Estableciendo Username: {}", username);
    }
}
