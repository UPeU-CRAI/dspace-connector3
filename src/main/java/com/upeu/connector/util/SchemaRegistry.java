package com.upeu.connector.util;

import com.upeu.connector.schema.EPersonSchema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

public class SchemaRegistry {

    /**
     * Registra todos los esquemas disponibles en el conector.
     *
     * @param schemaBuilder Instancia de SchemaBuilder para registrar los esquemas.
     */
    public static void registerSchemas(SchemaBuilder schemaBuilder) {
        // Registrar esquema de EPerson
        EPersonSchema.define(schemaBuilder);

        // Futuro: Registrar esquemas de otros objetos como Group e Item
        // GroupSchema.define(schemaBuilder);
        // ItemSchema.define(schemaBuilder);
    }

}
