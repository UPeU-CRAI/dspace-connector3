package com.upeu.connector.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointRegistry {

    private static final Map<String, String> endpointMap = new ConcurrentHashMap<>();

    static {
        endpointMap.put("epersons", EndpointConstants.EPERSONS);
        endpointMap.put("login", EndpointConstants.LOGIN);
        endpointMap.put("authStatus", EndpointConstants.AUTH_STATUS);
        // Agregar otros mapeos aquí
    }

    public static String getEndpoint(String key) {
        if (!endpointMap.containsKey(key)) {
            throw new IllegalArgumentException("Endpoint no encontrado para la clave: " + key);
        }
        return endpointMap.get(key);
    }
}
