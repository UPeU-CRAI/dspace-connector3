package com.upeu.connector.util;

import com.upeu.connector.schema.EPersonSchema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

/**
 * Utility class for registering schemas in the DSpace connector.
 */
public class SchemaRegistry {

    /**
     * Registers all available schemas in the connector.
     *
     * @param schemaBuilder SchemaBuilder instance to register schemas.
     */
    public static void registerSchemas(SchemaBuilder schemaBuilder) {
        if (schemaBuilder == null) {
            throw new IllegalArgumentException("SchemaBuilder cannot be null.");
        }

        // Register schema for EPerson
        EPersonSchema.define(schemaBuilder);

        // Placeholder for future schema registrations
        // Example:
        // GroupSchema.define(schemaBuilder);
        // ItemSchema.define(schemaBuilder);
    }
}
