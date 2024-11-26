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
        // Define attributes for ePerson
        Set<AttributeInfo> attributes = new HashSet<>();

        attributes.add(createAttribute("id", true, false, false, true, String.class));
        attributes.add(createAttribute("email", true, true, true, true, String.class));
        attributes.add(createAttribute("firstname", true, true, true, true, String.class));
        attributes.add(createAttribute("lastname", true, true, true, true, String.class));
        attributes.add(createAttribute("canLogIn", false, true, true, true, Boolean.class));
        attributes.add(createAttribute("netid", false, true, true, true, String.class));
        attributes.add(createAttribute("requireCertificate", false, true, true, true, Boolean.class));
        attributes.add(createAttribute("certificate", false, true, true, true, String.class));
        attributes.add(createAttribute("metadata", false, true, true, true, String.class, true)); // Multi-valued

        // Validate essential attributes
        validateAttributes(attributes, Set.of("id", "email", "firstname", "lastname"));

        // Define ObjectClass for ePerson
        ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
        objectClassBuilder.setType("eperson");
        objectClassBuilder.addAllAttributeInfo(attributes);

        // Add ObjectClass to SchemaBuilder
        schemaBuilder.defineObjectClass(objectClassBuilder.build());

        // Log confirmation
        System.out.println("Esquema 'eperson' definido correctamente.");
    }

    /**
     * Helper method to create attributes with common properties.
     *
     * @param name          The name of the attribute.
     * @param required      Whether the attribute is required.
     * @param createable    Whether the attribute can be created.
     * @param updateable    Whether the attribute can be updated.
     * @param readable      Whether the attribute can be read.
     * @param type          The type of the attribute.
     * @param isMultiValued Whether the attribute is multi-valued.
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
        logAttributeInfo(attributeInfo);
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
     * Logs the information of an attribute for debugging purposes.
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

    /**
     * Validates that essential attributes are present in the schema.
     *
     * @param attributes          The set of attributes to validate.
     * @param essentialAttributes The set of essential attribute names.
     */
    private static void validateAttributes(Set<AttributeInfo> attributes, Set<String> essentialAttributes) {
        for (String attr : essentialAttributes) {
            if (attributes.stream().noneMatch(attribute -> attr.equals(attribute.getName()))) {
                throw new IllegalArgumentException("El atributo esencial '" + attr + "' no está definido en el esquema.");
            }
        }
    }
}
