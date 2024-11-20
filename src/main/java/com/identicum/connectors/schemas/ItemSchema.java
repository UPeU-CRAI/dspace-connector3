package com.identicum.connectors.schemas;

import com.identicum.connectors.DSpaceConnector;

import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines the schema for Item objects in DSpace-CRIS.
 * Specifies attributes and their properties.
 */
public class ItemSchema {

    /**
     * Builds and returns the schema for Item.
     *
     * @return A SchemaBuilder for the Item object class.
     */
    public static SchemaBuilder getSchema() {
        SchemaBuilder builder = new SchemaBuilder(DSpaceConnector.class);

        // Create attributes
        Set<AttributeInfo> attributes = new HashSet<>();

        attributes.add(
                AttributeInfoBuilder.define("__UID__")
                        .setRequired(true)
                        .setCreateable(false)
                        .setUpdateable(false)
                        .setReadable(true)
                        .build()
        );

        attributes.add(
                AttributeInfoBuilder.define("__NAME__")
                        .setRequired(true)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        attributes.add(
                AttributeInfoBuilder.define("title")
                        .setRequired(true)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        attributes.add(
                AttributeInfoBuilder.define("handle")
                        .setRequired(false)
                        .setCreateable(true)
                        .setUpdateable(false)
                        .setReadable(true)
                        .build()
        );

        attributes.add(
                AttributeInfoBuilder.define("metadata")
                        .setRequired(false)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .setType(String.class)
                        .setMultiValued(true)
                        .build()
        );

        attributes.add(
                AttributeInfoBuilder.define("lastModified")
                        .setRequired(false)
                        .setCreateable(false)
                        .setUpdateable(false)
                        .setReadable(true)
                        .setType(String.class)
                        .build()
        );

        // Use SchemaBuilder to define ObjectClassInfo
        builder.defineObjectClass("Item", attributes);

        return builder;
    }
}
