package com.identicum.connectors;

import com.evolveum.polygon.rest.AbstractRestConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.ConfigurationClass;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;

@ConfigurationClass(skipUnsupported = false)
public class RestUsersConfiguration extends AbstractRestConfiguration {

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
    // Bloque de Validación de Configuración
    // ==============================

    // Si la superclase tiene un método validate(), puedes mantener @Override y super.validate()
    @Override
    public void validate() {
        super.validate(); // Llama al método de la superclase si es necesario

        if (serviceAddress == null || serviceAddress.isEmpty()) {
            throw new ConfigurationException("La dirección del servicio (serviceAddress) no puede estar vacía.");
        }
        if (username == null || username.isEmpty()) {
            throw new ConfigurationException("El nombre de usuario (username) no puede estar vacío.");
        }
        if (password == null) {
            throw new ConfigurationException("La contraseña (password) no puede estar vacía.");
        }
        if (trustAllCertificates == null) {
            trustAllCertificates = false;
        }

        // Validar que la URL del servicio es válida
        if (!serviceAddress.startsWith("http://") && !serviceAddress.startsWith("https://")) {
            throw new ConfigurationException("La dirección del servicio debe comenzar con 'http://' o 'https://'.");
        }
    }

    // ==============================
    // Bloque de Métodos Adicionales
    // ==============================

    // Cambiamos isSslTrustAll() por getSslTrustAll()
    //@Override
    //public boolean getSslTrustAll() {
    //    return trustAllCertificates;
    //}
}
