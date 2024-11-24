package com.upeu.connector.handler;

/**
 * Represents an ePerson entity in the DSpace-CRIS system.
 */
public class EPerson {
    private final String id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final boolean canLogIn;

    public EPerson(String id, String email, String firstName, String lastName, boolean canLogIn) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.canLogIn = canLogIn;
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
