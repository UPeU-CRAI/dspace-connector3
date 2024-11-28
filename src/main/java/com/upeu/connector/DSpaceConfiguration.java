package com.upeu.connector;

import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Configuration class for the DSpace Connector.
 * Holds properties and settings needed to communicate with the DSpace-CRIS API.
 */
public class DSpaceConfiguration extends AbstractConfiguration {

    // Base URL for the DSpace-CRIS server
    private String baseUrl;

    // API authentication properties
    private String username;
    private String password;

    // Timeout settings
    private int connectTimeout = 10000; // Default: 10 seconds
    private int readTimeout = 30000;    // Default: 30 seconds

    /**
     * Gets the base URL for the DSpace-CRIS API.
     *
     * @return The base URL.
     */
    @ConfigurationProperty(order = 1, displayMessageKey = "Base URL",
            helpMessageKey = "The base URL of the DSpace-CRIS API (e.g., https://example.com).",
            required = true)
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL for the DSpace-CRIS API.
     *
     * @param baseUrl The base URL.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Gets the username for API authentication.
     *
     * @return The username.
     */
    @ConfigurationProperty(order = 2, displayMessageKey = "Username",
            helpMessageKey = "Username for authentication with the DSpace-CRIS API.",
            required = true)
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for API authentication.
     *
     * @param username The username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password for API authentication.
     *
     * @return The password.
     */
    @ConfigurationProperty(order = 3, displayMessageKey = "Password",
            helpMessageKey = "Password for authentication with the DSpace-CRIS API.",
            confidential = true, required = true)
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for API authentication.
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the connection timeout in milliseconds.
     *
     * @return The connection timeout.
     */
    @ConfigurationProperty(order = 4, displayMessageKey = "Connection Timeout",
            helpMessageKey = "Connection timeout in milliseconds (default: 10,000 ms).",
            required = false)
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connection timeout in milliseconds.
     *
     * @param connectTimeout The connection timeout.
     */
    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout <= 0) {
            throw new IllegalArgumentException("Connection timeout must be greater than 0.");
        }
        this.connectTimeout = connectTimeout;
    }

    /**
     * Gets the read timeout in milliseconds.
     *
     * @return The read timeout.
     */
    @ConfigurationProperty(order = 5, displayMessageKey = "Read Timeout",
            helpMessageKey = "Read timeout in milliseconds (default: 30,000 ms).",
            required = false)
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout in milliseconds.
     *
     * @param readTimeout The read timeout.
     */
    public void setReadTimeout(int readTimeout) {
        if (readTimeout <= 0) {
            throw new IllegalArgumentException("Read timeout must be greater than 0.");
        }
        this.readTimeout = readTimeout;
    }

    /**
     * Checks if the configuration is properly initialized.
     *
     * @return true if the configuration is valid, false otherwise.
     */
    public boolean isInitialized() {
        return baseUrl != null && !baseUrl.isEmpty() &&
                username != null && !username.isEmpty() &&
                password != null && !password.isEmpty();
    }

    /**
     * Validates the configuration properties.
     */
    @Override
    public void validate() {
        validateBaseUrl();
        validateCredentials();
        validateTimeouts();
    }

    /**
     * Validates the base URL format.
     */
    private void validateBaseUrl() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Base URL must be provided.");
        }
        try {
            new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Base URL is not a valid URL: " + baseUrl, e);
        }
    }

    /**
     * Validates the username and password.
     */
    private void validateCredentials() {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username must be provided.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must be provided.");
        }
    }

    /**
     * Validates timeout settings.
     */
    private void validateTimeouts() {
        if (connectTimeout <= 0) {
            throw new IllegalArgumentException("Connection timeout must be greater than 0.");
        }
        if (readTimeout <= 0) {
            throw new IllegalArgumentException("Read timeout must be greater than 0.");
        }
    }
}
