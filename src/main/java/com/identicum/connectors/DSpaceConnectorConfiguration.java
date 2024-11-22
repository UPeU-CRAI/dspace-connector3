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
            required = false,
            confidential = false
    )
    public Boolean getTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(Boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
        LOG.debug("Configuración de 'trustAllCertificates' establecida en: {}", trustAllCertificates);
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
            LOG.info("URL base configurada: {}", this.baseUrl);
        } else {
            this.baseUrl = baseUrl;
            LOG.warn("URL base configurada como nula.");
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
        LOG.debug("Usuario configurado: {}", username != null ? "****" : "nulo");
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
        LOG.debug("Password configurada: {}", password != null ? "****" : "nula");
    }

    // ==============================
    // Validation Block
    // ==============================
    @Override
    public void validate() {
        LOG.info("Iniciando la validación de la configuración de DSpaceConnector...");

        // Validate Base URL
        if (baseUrl == null || baseUrl.isEmpty()) {
            LOG.error("Validación fallida: La URL base está vacía.");
            throw new ConfigurationException("La dirección del servicio (Base URL) no puede estar vacía.");
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            LOG.error("Validación fallida: La URL base debe comenzar con 'http://' o 'https://'.");
            throw new ConfigurationException("La dirección de Base URL debe comenzar con 'http://' o 'https://'.");
        }

        // Validate Username
        if (username == null || username.isEmpty()) {
            LOG.error("Validación fallida: El nombre de usuario está vacío.");
            throw new ConfigurationException("El nombre de usuario no puede estar vacío.");
        }

        // Validate Password
        if (password == null) {
            LOG.error("Validación fallida: La contraseña está vacía.");
            throw new ConfigurationException("La contraseña no puede estar vacía.");
        }

        // Default trustAllCertificates to false if not set
        if (trustAllCertificates == null) {
            trustAllCertificates = false;
            LOG.debug("El valor de 'trustAllCertificates' no se estableció. Configurando valor predeterminado a false.");
        }

        LOG.info("Configuración de DSpaceConnector validada exitosamente.");
    }
}
