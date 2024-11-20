package com.identicum.schemas;

import org.json.JSONObject;

/**
 * Represents the schema for an EPerson object in DSpace.
 * Handles serialization and deserialization from/to JSON.
 */
public class EPersonSchema {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean canLogIn;
    private boolean requireCertificate;

    // =====================================
    // Constructors
    // =====================================
    public EPersonSchema() {}

    public EPersonSchema(String id, String email, String firstName, String lastName, boolean canLogIn, boolean requireCertificate) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.canLogIn = canLogIn;
        this.requireCertificate = requireCertificate;
    }

    // =====================================
    // Getters and Setters
    // =====================================
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

    // =====================================
    // Serialization to JSON
    // =====================================
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("email", email);
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("canLogIn", canLogIn);
        json.put("requireCertificate", requireCertificate);
        return json;
    }

    // =====================================
    // Deserialization from JSON
    // =====================================
    public static EPersonSchema fromJson(JSONObject json) {
        return new EPersonSchema(
                json.optString("id"),
                json.optString("email"),
                json.optString("firstName"),
                json.optString("lastName"),
                json.optBoolean("canLogIn"),
                json.optBoolean("requireCertificate")
        );
    }
}
