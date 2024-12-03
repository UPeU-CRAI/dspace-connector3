package com.upeu.connector.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointRegistry.class);

    private static final Map<String, String> endpointMap = new ConcurrentHashMap<>();

    static {
        LOG.info("Inicializando EndpointRegistry con valores predeterminados.");
        endpointMap.put("epersons", "server/api/eperson/epersons");
        endpointMap.put("login", "server/api/authn/login");
        endpointMap.put("authStatus", "server/api/authn/status");
        // Agregar otros endpoints aquí
        LOG.debug("Endpoints inicializados: {}", endpointMap);
    }

    /**
     * Obtiene el endpoint asociado con una clave específica.
     *
     * @param key La clave del endpoint.
     * @return La URL del endpoint.
     * @throws IllegalArgumentException Si la clave no se encuentra en el registro.
     */
    public static String getEndpoint(String key) {
        LOG.debug("Buscando endpoint para la clave: {}", key);
        return endpointMap.computeIfAbsent(key, k -> {
            LOG.error("Error: Endpoint no encontrado para la clave: {}", k);
            throw new IllegalArgumentException("Endpoint no encontrado para la clave: " + k);
        });
    }

    /**
     * Agrega un nuevo endpoint al registro.
     *
     * @param key      La clave del endpoint.
     * @param endpoint La URL del endpoint.
     */
    public static void addEndpoint(String key, String endpoint) {
        LOG.debug("Agregando nuevo endpoint al registro. Clave: {}, URL: {}", key, endpoint);
        if (endpointMap.containsKey(key)) {
            LOG.warn("El endpoint con clave '{}' ya existe y será sobrescrito. Valor previo: {}", key, endpointMap.get(key));
        }
        endpointMap.put(key, endpoint);
        LOG.info("Endpoint agregado/actualizado correctamente. Clave: {}, URL: {}", key, endpoint);
    }

    /**
     * Elimina un endpoint del registro.
     *
     * @param key La clave del endpoint a eliminar.
     * @return La URL del endpoint eliminado, o null si no existía.
     */
    public static String removeEndpoint(String key) {
        LOG.debug("Eliminando endpoint del registro para la clave: {}", key);
        String removedEndpoint = endpointMap.remove(key);
        if (removedEndpoint == null) {
            LOG.warn("Intento de eliminar un endpoint que no existe para la clave: {}", key);
        } else {
            LOG.info("Endpoint eliminado correctamente. Clave: {}, URL: {}", key, removedEndpoint);
        }
        return removedEndpoint;
    }

    /**
     * Lista todos los endpoints registrados.
     */
    public static void listAllEndpoints() {
        LOG.info("Lista actual de endpoints registrados:");
        endpointMap.forEach((key, value) -> LOG.info("Clave: {}, URL: {}", key, value));
    }
}
