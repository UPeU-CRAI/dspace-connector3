package com.identicum.connectors.schemas;

import org.json.JSONObject;

/**
 * Schema representation for EPerson in DSpace.
 * Helps to construct and validate EPerson data payloads.
 */
public class EPersonSchema {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean canLogIn;
    private boolean requireCertificate;

    // Default Constructor
    public EPersonSchema() {
    }

    // Constructor with all fields
    public EPersonSchema(String id, String email, String firstName, String lastName, boolean canLogIn, boolean requireCertificate) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.canLogIn = canLogIn;
        this.requireCertificate = requireCertificate;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isCanLogIn() {
        return canLogIn;
    }

    public void setCanLogIn(boolean canLogIn) {
        this.canLogIn = canLogIn;
    }

    public boolean isRequireCertificate() {
        return requireCertificate;
    }

    public void setRequireCertificate(boolean requireCertificate) {
        this.requireCertificate = requireCertificate;
    }

    /**
     * Convert this object to a JSON representation.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", this.id);
        json.put("email", this.email);
        json.put("firstName", this.firstName);
        json.put("lastName", this.lastName);
        json.put("canLogIn", this.canLogIn);
        json.put("requireCertificate", this.requireCertificate);
        return json;
    }

    /**
     * Construct an EPersonSchema object from a JSON representation.
     */
    public static EPersonSchema fromJson(JSONObject json) {
        EPersonSchema schema = new EPersonSchema();
        schema.setId(json.optString("id"));
        schema.setEmail(json.optString("email"));
        schema.setFirstName(json.optString("firstName"));
        schema.setLastName(json.optString("lastName"));
        schema.setCanLogIn(json.optBoolean("canLogIn"));
        schema.setRequireCertificate(json.optBoolean("requireCertificate"));
        return schema;
    }
}
