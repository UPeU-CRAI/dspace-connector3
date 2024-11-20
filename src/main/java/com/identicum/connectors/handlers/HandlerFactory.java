package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;
import com.identicum.connectors.Endpoints;

/**
 * Factory for creating handler instances for different DSpace operations.
 */
public class HandlerFactory {

    private final AuthenticationHandler authenticationHandler;
    private final Endpoints endpoints;

    /**
     * Constructor for HandlerFactory.
     *
     * @param authenticationHandler the AuthenticationHandler instance
     * @param endpoints the Endpoints instance for URL management
     */
    public HandlerFactory(AuthenticationHandler authenticationHandler, Endpoints endpoints) {
        this.authenticationHandler = authenticationHandler;
        this.endpoints = endpoints;
    }

    /**
     * Creates an EPersonHandler instance.
     *
     * @return an instance of EPersonHandler
     */
    public EPersonHandler createEPersonHandler() {
        return new EPersonHandler(authenticationHandler, endpoints);
    }

    /**
     * Creates a GroupHandler instance.
     *
     * @return an instance of GroupHandler
     */
    public GroupHandler createGroupHandler() {
        return new GroupHandler(authenticationHandler, endpoints);
    }

    /**
     * Creates an ItemHandler instance.
     *
     * @return an instance of ItemHandler
     */
    public ItemHandler createItemHandler() {
        return new ItemHandler(authenticationHandler, endpoints);
    }
}
