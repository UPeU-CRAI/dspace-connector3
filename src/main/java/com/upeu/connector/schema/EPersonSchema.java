package com.upeu.connector.schema;

import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines the schema for ePerson entities in DSpace-CRIS.
 */
public class EPersonSchema {

    public static void define(SchemaBuilder schemaBuilder) {
        // Define attributes for ePerson using a helper method
        Set<AttributeInfo> attributes = new HashSet<>();
        attributes.add(createAttribute("Name", true, true, true, true, null));
        attributes.add(createAttribute("id", true, false, false, true, null));
        attributes.add(createAttribute("email", true, true, true, true, null));
        attributes.add(createAttribute("firstname", true, true, true, true, null));
        attributes.add(createAttribute("lastname", true, true, true, true, null));
        attributes.add(createAttribute("canLogIn", false, true, true, true, Boolean.class));
        attributes.add(createAttribute("metadata", false, true, true, true, String.class, true));

        // Verify the "Name" attribute is present
        if (attributes.stream().noneMatch(attr -> "Name".equals(attr.getName()))) {
            throw new IllegalArgumentException("El atributo 'Name' no fue agregado correctamente al esquema.");
        }

        // Define ObjectClass for ePerson using ObjectClassInfoBuilder
        ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
        objectClassBuilder.setType("eperson"); // Nombre del objeto
        objectClassBuilder.addAllAttributeInfo(attributes); // Agregar todos los atributos

        System.out.println("Objeto 'eperson' definido con éxito en el esquema."); // Log para confirmación

        // Add ObjectClass to SchemaBuilder
        schemaBuilder.defineObjectClass(objectClassBuilder.build());
    }

    /**
     * Helper method to create attributes with common properties.
     *
     * @param name          The name of the attribute.
     * @param required      Whether the attribute is required.
     * @param createable    Whether the attribute can be created.
     * @param updateable    Whether the attribute can be updated.
     * @param readable      Whether the attribute can be read.
     * @param type          The type of the attribute (can be null for default).
     * @param isMultiValued Whether the attribute is multi-valued (default false).
     * @return The built AttributeInfo object.
     */
    private static AttributeInfo createAttribute(String name, boolean required, boolean createable, boolean updateable,
                                                 boolean readable, Class<?> type, boolean isMultiValued) {
        AttributeInfoBuilder builder = AttributeInfoBuilder.define(name)
                .setRequired(required)
                .setCreateable(createable)
                .setUpdateable(updateable)
                .setReadable(readable);

        if (type != null) {
            builder.setType(type);
        }

        if (isMultiValued) {
            builder.setMultiValued(true);
        }

        AttributeInfo attributeInfo = builder.build();
        logAttributeInfo(attributeInfo); // Log attribute info for debugging
        return attributeInfo;
    }

    /**
     * Overloaded helper method for single-valued attributes.
     */
    private static AttributeInfo createAttribute(String name, boolean required, boolean createable, boolean updateable,
                                                 boolean readable, Class<?> type) {
        return createAttribute(name, required, createable, updateable, readable, type, false);
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
