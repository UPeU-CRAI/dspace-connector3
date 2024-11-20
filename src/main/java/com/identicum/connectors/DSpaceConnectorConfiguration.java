package com.identicum.connectors;

import com.evolveum.polygon.rest.AbstractRestConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;

/**
 * Configuration class for the DSpace-CRIS connector.
 * Manages settings for base URL, credentials, and SSL.
 */

public class DSpaceConnectorConfiguration extends AbstractRestConfiguration {

    private Boolean trustAllCertificates = false;
    private String baseUrl;
    private String username;
    private GuardedString password;

    @ConfigurationProperty(
            order = 1,
            displayMessageKey = "rest.config.trustAllCertificates.display",
            helpMessageKey = "rest.config.trustAllCertificates.help",
            required = false, // Not mandatory
            confidential = false
    )
    public Boolean getTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(Boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    @ConfigurationProperty(
            order = 2,
            displayMessageKey = "rest.config.baseUrl.display",
            helpMessageKey = "rest.config.baseUrl.help",
            required = true,
            confidential = false
    )
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @ConfigurationProperty(
            order = 3,
            displayMessageKey = "rest.config.username.display",
            helpMessageKey = "rest.config.username.help",
            required = true,
            confidential = false
    )
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ConfigurationProperty(
            order = 4,
            displayMessageKey = "rest.config.password.display",
            helpMessageKey = "rest.config.password.help",
            required = true,
            confidential = false
    )
    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(GuardedString password) {
        this.password = password;
    }

    // ==============================
    // Validation Block
    // ==============================
    @Override
    public void validate() {

        // Validate Base URL
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new ConfigurationException("La dirección del servicio (Base URL) no puede estar vacía.");
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new ConfigurationException("La dirección de Base URL debe comenzar con 'http://' o 'https://'.");
        }

        // Validate Username
        if (username == null || username.isEmpty()) {
            throw new ConfigurationException("Username no puede estar vacío.");
        }

        // Validate Password
        if (password == null) {
            throw new ConfigurationException("Password no puede estar vacía.");
        }

        // Default trustAllCertificates to false if not set
        if (trustAllCertificates == null) {
            trustAllCertificates = false;
        }
    }
}
