package com.identicum.connectors;

import com.evolveum.polygon.rest.AbstractRestConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.common.security.GuardedString;

public class RestUsersConfiguration extends AbstractRestConfiguration {

    private Boolean trustAllCertificates;
    private String serviceAddress;
    private String username;
    private GuardedString password;

    @ConfigurationProperty(order = 1, displayMessageKey = "rest.config.trustAllCertificates.display", helpMessageKey = "rest.config.trustAllCertificates.help", required = true)
    public Boolean getTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(Boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    @ConfigurationProperty(order = 2, displayMessageKey = "serviceAddress.display", helpMessageKey = "serviceAddress.help", required = true)
    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    @ConfigurationProperty(order = 3, displayMessageKey = "username.display", helpMessageKey = "username.help", required = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ConfigurationProperty(order = 4, displayMessageKey = "password.display", helpMessageKey = "password.help", confidential = true, required = true)
    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(GuardedString password) {
        this.password = password;
    }
}
