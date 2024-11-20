package com.identicum.connectors.schemas;

import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

import java.util.Set;

/**
 * Defines the schema for EPerson objects in DSpace-CRIS.
 * Specifies attributes and their properties.
 */
public class EPersonSchema {

    // Define the ObjectClass for EPerson
    public static final ObjectClass EPERSON_OBJECT_CLASS = new ObjectClass("EPerson");

    /**
     * Builds and returns the schema for EPerson.
     *
     * @return A set of AttributeInfo representing the schema for EPerson.
     */
    public static Set<AttributeInfo> getSchema() {
        // Instantiate the SchemaBuilder with the correct connector class
        SchemaBuilder builder = new SchemaBuilder(EPersonSchema.class);

        // Define attributes for EPerson
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

        builder.defineAttribute(
                AttributeInfoBuilder.define("firstName")
                        .setRequired(true)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("lastName")
                        .setRequired(true)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("email")
                        .setRequired(true)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("canLogIn")
                        .setRequired(false)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .setType(Boolean.class)
                        .build()
        );

        builder.defineAttribute(
                AttributeInfoBuilder.define("requireCertificate")
                        .setRequired(false)
                        .setCreateable(true)
                        .setUpdateable(true)
                        .setReadable(true)
                        .setType(Boolean.class)
                        .build()
        );

        // Return the schema attributes
        return builder.build().getObjectClassInfo(EPERSON_OBJECT_CLASS).getAttributeInfo();
    }
}
