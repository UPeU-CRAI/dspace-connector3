package com.identicum.connectors;

import com.evolveum.polygon.rest.AbstractRestConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for the DSpace-CRIS connector.
 * Manages settings for base URL, credentials, and SSL.
 */

public class DSpaceConnectorConfiguration extends AbstractRestConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(DSpaceConnectorConfiguration.class);

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
        if (baseUrl != null) {
            // Normalize the baseUrl to remove trailing slashes
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            LOG.debug("Base URL set to: {}", this.baseUrl);
        } else {
            this.baseUrl = baseUrl;
        }
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
        LOG.info("Validating DSpaceConnectorConfiguration...");

        // Validate Base URL
        if (baseUrl == null || baseUrl.isEmpty()) {
            LOG.error("Validation failed: Base URL is empty.");
            throw new ConfigurationException("La dirección del servicio (Base URL) no puede estar vacía.");
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            LOG.error("Validation failed: Base URL must start with 'http://' or 'https://'.");
            throw new ConfigurationException("La dirección de Base URL debe comenzar con 'http://' o 'https://'.");
        }

        // Validate Username
        if (username == null || username.isEmpty()) {
            LOG.error("Validation failed: Username is empty.");
            throw new ConfigurationException("Username no puede estar vacío.");
        }

        // Validate Password
        if (password == null) {
            LOG.error("Validation failed: Password is empty.");
            throw new ConfigurationException("Password no puede estar vacía.");
        }

        // Default trustAllCertificates to false if not set
        if (trustAllCertificates == null) {
            trustAllCertificates = false;
            LOG.debug("Trust all certificates not set. Defaulting to false.");
        }

        LOG.info("DSpaceConnectorConfiguration validated successfully.");
    }
}
