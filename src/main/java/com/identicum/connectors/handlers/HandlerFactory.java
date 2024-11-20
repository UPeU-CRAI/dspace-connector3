package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;

public class HandlerFactory {

    private final AuthenticationHandler authenticationHandler;

    /**
     * Constructor de la fábrica.
     *
     * @param baseUrl El endpoint base del API REST.
     * @param username El nombre de usuario para la autenticación.
     * @param password La contraseña para la autenticación.
     */
    public HandlerFactory(String baseUrl, String username, String password) {
        // Valida los parámetros antes de crear el AuthenticationHandler
        this.authenticationHandler = new AuthenticationHandler(baseUrl, username, password);
    }

    /**
     * Crea y devuelve una instancia de ItemHandler.
     *
     * @return ItemHandler configurado.
     */
    public ItemHandler createItemHandler() {
        ItemHandler itemHandler = new ItemHandler(authenticationHandler);
        itemHandler.setBaseUrl(authenticationHandler.getBaseUrl()); // Configura el baseUrl
        return itemHandler;
    }

    /**
     * Crea y devuelve una instancia de EPersonHandler.
     *
     * @return EPersonHandler configurado.
     */
    public EPersonHandler createEPersonHandler() {
        EPersonHandler ePersonHandler = new EPersonHandler(authenticationHandler);
        ePersonHandler.setBaseUrl(authenticationHandler.getBaseUrl()); // Configura el baseUrl
        return ePersonHandler;
    }

    /**
     * Crea y devuelve una instancia de GroupHandler.
     *
     * @return GroupHandler configurado.
     */
    public GroupHandler createGroupHandler() {
        GroupHandler groupHandler = new GroupHandler(authenticationHandler);
        groupHandler.setBaseUrl(authenticationHandler.getBaseUrl()); // Configura el baseUrl
        return groupHandler;
    }
}
