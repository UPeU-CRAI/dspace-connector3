package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.exceptions.HandlerCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fábrica para crear instancias de handlers configurados.
 */
public class HandlerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HandlerFactory.class);

    private final AuthenticationHandler authenticationHandler;

    /**
     * Constructor de la fábrica.
     *
     * @param baseUrl  El endpoint base del API REST.
     * @param username El nombre de usuario para la autenticación.
     * @param password La contraseña para la autenticación.
     * @throws IllegalArgumentException si los parámetros son inválidos.
     */
    public HandlerFactory(String baseUrl, String username, String password) {
        validateParameters(baseUrl, username, password);
        this.authenticationHandler = new AuthenticationHandler(baseUrl, username, password);
        LOG.info("HandlerFactory inicializada con baseUrl: {}", baseUrl);
    }

    /**
     * Método genérico para crear handlers configurados.
     *
     * @param handlerClass Clase del handler que se desea crear.
     * @param <T>          Tipo del handler.
     * @return Instancia del handler configurada.
     * @throws HandlerCreationException si ocurre un error al crear el handler.
     */
    public <T extends AbstractHandler> T createHandler(Class<T> handlerClass) {
        LOG.info("Creando instancia de handler: {}", handlerClass.getSimpleName());
        try {
            return handlerClass.getDeclaredConstructor(AuthenticationHandler.class)
                    .newInstance(authenticationHandler);
        } catch (Exception e) {
            LOG.error("Error al crear el handler: {}", handlerClass.getSimpleName(), e);
            throw new HandlerCreationException(
                    "Error al crear el handler: " + handlerClass.getSimpleName(), e
            );
        }
    }

    /**
     * Valida los parámetros del constructor.
     *
     * @param baseUrl  El endpoint base del API REST.
     * @param username El nombre de usuario para la autenticación.
     * @param password La contraseña para la autenticación.
     * @throws IllegalArgumentException si alguno de los parámetros es inválido.
     */
    private void validateParameters(String baseUrl, String username, String password) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            LOG.error("El baseUrl no puede ser nulo o vacío.");
            throw new IllegalArgumentException("El baseUrl no puede ser nulo o vacío.");
        }
        if (username == null || username.isEmpty()) {
            LOG.error("El nombre de usuario no puede ser nulo o vacío.");
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo o vacío.");
        }
        if (password == null || password.isEmpty()) {
            LOG.error("La contraseña no puede ser nula o vacía.");
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía.");
        }
    }
}
