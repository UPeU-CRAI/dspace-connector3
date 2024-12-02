package com.upeu.connector.util;

/**
 * Clase para construir endpoints basados en una URL base.
 * NOTA: El valor de `baseUrl` no debe incluir `/server/`. Si no tiene una barra '/' al final el código lo completará
 */
public class EndpointUtil {

    private final String baseUrl;

    public EndpointUtil(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL no puede ser nulo o vacío.");
        }

        // Asegurarse de que baseUrl termine con "/"
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    // Métodos específicos para los endpoints de autenticación
    public String getAuthnStatusEndpoint() {
        return baseUrl + "server/api/authn/status";
    }

    public String getAuthnLoginEndpoint() {
        return baseUrl + "server/api/authn/login";
    }

    // Otros métodos para endpoints específicos
    public String getEpersonsEndpoint() {
        return baseUrl + "server/api/eperson/epersons";
    }

    public String getGroupsEndpoint() {
        return baseUrl + "server/api/eperson/groups";
    }

    public String getItemsEndpoint() {
        return baseUrl + "server/api/core/items";
    }
}
