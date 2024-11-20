package com.identicum.connectors;

import com.evolveum.polygon.rest.AbstractRestConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.ConfigurationClass;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;

/**
 * Configuration class for the DSpace-CRIS connector.
 * Manages settings for service address, credentials, and SSL.
 */
@ConfigurationClass(skipUnsupported = false)
public class DSpaceConnectorConfiguration extends AbstractRestConfiguration {

    private Boolean trustAllCertificates = false;
    private String serviceAddress;
    private String username;
    private GuardedString password;

    @ConfigurationProperty(
            order = 1,
            displayMessageKey = "rest.config.trustAllCertificates.display",
            helpMessageKey = "rest.config.trustAllCertificates.help",
            required = true
    )
    public Boolean getTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(Boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    @ConfigurationProperty(
            order = 2,
            displayMessageKey = "rest.config.serviceAddress.display",
            helpMessageKey = "rest.config.serviceAddress.help",
            required = true
    )
    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    @ConfigurationProperty(
            order = 3,
            displayMessageKey = "rest.config.username.display",
            helpMessageKey = "rest.config.username.help",
            required = true
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
            confidential = true,
            required = true
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
        super.validate();

        if (serviceAddress == null || serviceAddress.isEmpty()) {
            throw new ConfigurationException("Service address cannot be empty.");
        }
        if (username == null || username.isEmpty()) {
            throw new ConfigurationException("Username cannot be empty.");
        }
        if (password == null) {
            throw new ConfigurationException("Password cannot be empty.");
        }
        if (trustAllCertificates == null) {
            trustAllCertificates = false;
        }

        // Validate that the service URL is well-formed
        if (!serviceAddress.startsWith("http://") && !serviceAddress.startsWith("https://")) {
            throw new ConfigurationException("Service address must start with 'http://' or 'https://'.");
        }
    }
}
