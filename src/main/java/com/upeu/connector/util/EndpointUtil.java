package com.upeu.connector.util;

public class EndpointUtil {

    private String baseUrl;

    public EndpointUtil(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL no puede ser nulo o vacío.");
        }

        // Asegurarse de que baseUrl termine con "/"
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public String buildEndpoint(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new IllegalArgumentException("El path relativo no puede ser nulo o vacío.");
        }

        // Asegurarse de que el path relativo comience sin "/"
        relativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;

        return this.baseUrl + "server/" + relativePath;
    }

    // Métodos específicos para los endpoints de autenticación
    public String getAuthnStatusEndpoint() {
        return buildEndpoint("api/authn/status");
    }

    public String getAuthnLoginEndpoint() {
        return buildEndpoint("api/authn/login");
    }

    // Agregar otros métodos específicos para futuros endpoints
    public String getEpersonsEndpoint() {
        return buildEndpoint("api/eperson/epersons");
    }

    public String getGroupsEndpoint() {
        return buildEndpoint("api/eperson/groups");
    }

    public String getItemsEndpoint() {
        return buildEndpoint("api/core/items");
    }
}
