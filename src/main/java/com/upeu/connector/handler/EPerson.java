package com.upeu.connector.handler;

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

    // Existing constructor
    public EPerson(String id, String email, String firstName, String lastName, boolean canLogIn) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.canLogIn = canLogIn;
    }

    // New constructor to handle JSONObject input
    public EPerson(JSONObject json) {
        this.id = json.optString("id", null); // Safely handle missing fields
        this.email = json.optString("email", null);
        this.firstName = json.optJSONObject("metadata")
                .optJSONArray("eperson.firstname")
                .optJSONObject(0)
                .optString("value", null);
        this.lastName = json.optJSONObject("metadata")
                .optJSONArray("eperson.lastname")
                .optJSONObject(0)
                .optString("value", null);
        this.canLogIn = json.optBoolean("canLogIn", false); // Default to false if missing
    }

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

    // Changed method name for consistency
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
