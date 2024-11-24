package com.upeu.connector.schema;

import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines the schema for ePerson entities in DSpace-CRIS.
 */
public class EPersonSchema {

    public static void define(SchemaBuilder schemaBuilder) {
        // Define attributes for ePerson
        AttributeInfo idAttribute = AttributeInfoBuilder.define("id")
                .setRequired(true)
                .setCreateable(false)
                .setUpdateable(false)
                .setReadable(true)
                .build();

        AttributeInfo emailAttribute = AttributeInfoBuilder.define("email")
                .setRequired(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();

        AttributeInfo firstnameAttribute = AttributeInfoBuilder.define("firstname")
                .setRequired(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();

        AttributeInfo lastnameAttribute = AttributeInfoBuilder.define("lastname")
                .setRequired(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();

        AttributeInfo canLogInAttribute = AttributeInfoBuilder.define("canLogIn")
                .setType(Boolean.class)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();

        AttributeInfo metadataAttribute = AttributeInfoBuilder.define("metadata")
                .setType(String.class)
                .setMultiValued(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();

        // Collect all attributes into a set
        Set<AttributeInfo> attributes = new HashSet<>();
        attributes.add(idAttribute);
        attributes.add(emailAttribute);
        attributes.add(firstnameAttribute);
        attributes.add(lastnameAttribute);
        attributes.add(canLogInAttribute);
        attributes.add(metadataAttribute);

        // Define ObjectClass for ePerson
        ObjectClassInfo epersonObjectClass = new ObjectClassInfo(
                "eperson",     // Type
                attributes,    // Attribute Info
                false,         // isContainer
                false,         // isAuxiliary
                false          // isEmbedded
        );

        // Add ObjectClass to SchemaBuilder
        schemaBuilder.defineObjectClass(epersonObjectClass);
    }
}
