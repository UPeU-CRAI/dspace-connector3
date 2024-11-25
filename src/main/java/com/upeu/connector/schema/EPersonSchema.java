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
        AttributeInfo nameAttribute = AttributeInfoBuilder.define("Name")
                .setRequired(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();
        logAttributeInfo(nameAttribute); // Log attribute info for debugging

        AttributeInfo idAttribute = AttributeInfoBuilder.define("id")
                .setRequired(true)
                .setCreateable(false)
                .setUpdateable(false)
                .setReadable(true)
                .build();
        logAttributeInfo(idAttribute);

        AttributeInfo emailAttribute = AttributeInfoBuilder.define("email")
                .setRequired(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();
        logAttributeInfo(emailAttribute);

        AttributeInfo firstnameAttribute = AttributeInfoBuilder.define("firstname")
                .setRequired(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();
        logAttributeInfo(firstnameAttribute);

        AttributeInfo lastnameAttribute = AttributeInfoBuilder.define("lastname")
                .setRequired(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();
        logAttributeInfo(lastnameAttribute);

        AttributeInfo canLogInAttribute = AttributeInfoBuilder.define("canLogIn")
                .setType(Boolean.class)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();
        logAttributeInfo(canLogInAttribute);

        AttributeInfo metadataAttribute = AttributeInfoBuilder.define("metadata")
                .setType(String.class)
                .setMultiValued(true)
                .setCreateable(true)
                .setUpdateable(true)
                .setReadable(true)
                .build();
        logAttributeInfo(metadataAttribute);

        // Collect all attributes into a set
        Set<AttributeInfo> attributes = new HashSet<>();
        attributes.add(nameAttribute);
        attributes.add(idAttribute);
        attributes.add(emailAttribute);
        attributes.add(firstnameAttribute);
        attributes.add(lastnameAttribute);
        attributes.add(canLogInAttribute);
        attributes.add(metadataAttribute);

        // Verify the "Name" attribute is present
        if (attributes.stream().noneMatch(attr -> "Name".equals(attr.getName()))) {
            throw new IllegalArgumentException("El atributo 'Name' no fue agregado correctamente al esquema.");
        }

        // Define ObjectClass for ePerson
        ObjectClassInfo epersonObjectClass = new ObjectClassInfo(
                "eperson",     // Type
                attributes,    // Attribute Info
                false,         // isContainer
                false,         // isAuxiliary
                false          // isEmbedded
        );

        System.out.println("Objeto 'eperson' definido con éxito en el esquema."); // Log para confirmación

        // Add ObjectClass to SchemaBuilder
        schemaBuilder.defineObjectClass(epersonObjectClass);
    }

    /**
     * Logs the information of an attribute for debugging.
     *
     * @param attributeInfo The AttributeInfo object to log.
     */
    private static void logAttributeInfo(AttributeInfo attributeInfo) {
        System.out.printf("Atributo definido: %s (Requerido: %b, Creable: %b, Actualizable: %b, Legible: %b)%n",
                attributeInfo.getName(),
                attributeInfo.isRequired(),
                attributeInfo.isCreateable(),
                attributeInfo.isUpdateable(),
                attributeInfo.isReadable());
    }
}
