package com.upeu.connector;

import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

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
    private int connectTimeout = 10000; // 10 seconds
    private int readTimeout = 30000;    // 30 seconds

    @ConfigurationProperty(order = 1, displayMessageKey = "Base URL",
            helpMessageKey = "The base URL of the DSpace-CRIS API (e.g., https://example.com).",
            required = true)
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @ConfigurationProperty(order = 2, displayMessageKey = "Username",
            helpMessageKey = "Username for authentication with the DSpace-CRIS API.",
            required = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ConfigurationProperty(order = 3, displayMessageKey = "Password",
            helpMessageKey = "Password for authentication with the DSpace-CRIS API.",
            confidential = true, required = true)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ConfigurationProperty(order = 4, displayMessageKey = "Connection Timeout",
            helpMessageKey = "Connection timeout in milliseconds.",
            required = false)
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @ConfigurationProperty(order = 5, displayMessageKey = "Read Timeout",
            helpMessageKey = "Read timeout in milliseconds.",
            required = false)
    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public void validate() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("Base URL must be provided.");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username must be provided.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must be provided.");
        }
    }
}
