package com.upeu.connector.util;

import com.upeu.connector.auth.AuthManager;
import com.upeu.connector.DSpaceClient;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public class TestUtil {

    public static void validateConnection(DSpaceClient client) {
        try {
            String response = client.get("/server/api/status");
            if (response == null || response.isEmpty()) {
                throw new ConnectorException("No se obtuvo respuesta del endpoint de estado.");
            }
        } catch (Exception e) {
            throw new ConnectorException("Error al validar conexión con DSpace: " + e.getMessage(), e);
        }
    }

    public static void validateAuthentication(AuthManager authManager) {
        try {
            String token = authManager.getJwtToken();
            if (token == null || token.isEmpty()) {
                throw new ConnectorException("Autenticación fallida: No se obtuvo un token válido.");
            }
        } catch (Exception e) {
            throw new ConnectorException("Error en la autenticación: " + e.getMessage(), e);
        }
    }
}
