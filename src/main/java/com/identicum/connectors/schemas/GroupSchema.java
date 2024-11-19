package com.identicum.connectors.schemas;

import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

import java.util.Set;

/**
 * Defines the schema for Group objects in DSpace-CRIS.
 * Specifies attributes and their properties.
 */
public class GroupSchema {

    // Define the ObjectClass for Group
    public static final ObjectClass GROUP_OBJECT_CLASS = new ObjectClass("Group");

    /**
     * Builds and returns the schema for Group.
     *
     * @return A set of AttributeInfo representing the schema for Group.
     */
    public static Set<AttributeInfo> getSchema() {
        SchemaBuilder builder = new SchemaBuilder(GroupSchema.class);

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

        // Group attributes
        builder.defineAttribute(
                AttributeInfoBuilder.define("name")
                        .setRequired(true)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("description")
                        .setRequired(false)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("members")
                        .setRequired(false)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .setMultiValued(true)
                        .setType(String.class)
                        .build()
        );

        return builder.build().getSupportedObjectClassesByType().get(GROUP_OBJECT_CLASS);
    }
}
