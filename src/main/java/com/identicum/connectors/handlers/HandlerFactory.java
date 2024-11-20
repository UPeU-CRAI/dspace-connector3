package com.identicum.connectors.handlers;

import com.identicum.connectors.AuthenticationHandler;

/**
 * Factory for creating handler instances for different DSpace operations.
 */
public class HandlerFactory {

    private final AuthenticationHandler authenticationHandler;

    public HandlerFactory(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    /**
     * Creates an EPersonHandler instance.
     *
     * @return an instance of EPersonHandler
     */
    public EPersonHandler createEPersonHandler() {
        return new EPersonHandler(authenticationHandler);
    }

    /**
     * Creates a GroupHandler instance.
     *
     * @return an instance of GroupHandler
     */
    public GroupHandler createGroupHandler() {
        return new GroupHandler(authenticationHandler);
    }

    /**
     * Creates an ItemHandler instance.
     *
     * @return an instance of ItemHandler
     */
    public ItemHandler createItemHandler() {
        return new ItemHandler(authenticationHandler);
    }
}
