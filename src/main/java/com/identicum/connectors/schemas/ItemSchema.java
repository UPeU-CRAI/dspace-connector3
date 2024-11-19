package com.identicum.connectors.schemas;

import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

import java.util.Set;

/**
 * Defines the schema for Item objects in DSpace-CRIS.
 * Specifies attributes and their properties.
 */
public class ItemSchema {

    // Define the ObjectClass for Item
    public static final ObjectClass ITEM_OBJECT_CLASS = new ObjectClass("Item");

    /**
     * Builds and returns the schema for Item.
     *
     * @return A set of AttributeInfo representing the schema for Item.
     */
    public static Set<AttributeInfo> getSchema() {
        SchemaBuilder builder = new SchemaBuilder(ItemSchema.class);

        // Required attributes
        builder.defineAttribute(
                AttributeInfoBuilder.define("__UID__")
                        .setRequired(true)
                        .setCreateable(false)
                        .setUpdateable(false)
                        .setReadable(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("__NAME__")
                        .setRequired(true)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        // Item attributes
        builder.defineAttribute(
                AttributeInfoBuilder.define("title")
                        .setRequired(true)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("handle")
                        .setRequired(false)
                        .setCreateable(true)
                        .setUpdateable(false)
                        .setReadable(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("metadata")
                        .setRequired(false)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .setType(String.class)
                        .setMultiValued(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("lastModified")
                        .setRequired(false)
                        .setCreateable(false)
                        .setUpdateable(false)
                        .setReadable(true)
                        .setType(String.class)
                        .build()
        );

        return builder.build().getSupportedObjectClassesByType().get(ITEM_OBJECT_CLASS);
    }
}
