package com.upeu.connector.schema;

import com.upeu.connector.util.EndpointRegistry;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines the schema for ePerson entities in DSpace-CRIS.
 */
public class EPersonSchema {

    private static final Logger LOGGER = LoggerFactory.getLogger(EPersonSchema.class);

    // Constantes para los nombres de atributos
    private static final String ATTR_ID = "id";
    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_FIRSTNAME = "firstname";
    private static final String ATTR_LASTNAME = "lastname";
    private static final String ATTR_CAN_LOGIN = "canLogIn";
    private static final String ATTR_NETID = "netid";
    private static final String ATTR_REQUIRE_CERTIFICATE = "requireCertificate";
    private static final String ATTR_CERTIFICATE = "certificate";
    private static final String ATTR_METADATA = "metadata";

    // Atributos esenciales
    private static final Set<String> ESSENTIAL_ATTRIBUTES = Set.of(ATTR_ID, ATTR_EMAIL, ATTR_FIRSTNAME, ATTR_LASTNAME);

    /**
     * Defines the ePerson schema.
     *
     * @param schemaBuilder SchemaBuilder instance.
     */
    public static void define(SchemaBuilder schemaBuilder) {
        // Define attributes for ePerson
        Set<AttributeInfo> attributes = new HashSet<>();

        attributes.add(createAttribute(ATTR_ID, true, false, false, true, String.class));
        attributes.add(createAttribute(ATTR_EMAIL, true, true, true, true, String.class));
        attributes.add(createAttribute(ATTR_FIRSTNAME, true, true, true, true, String.class));
        attributes.add(createAttribute(ATTR_LASTNAME, true, true, true, true, String.class));
        attributes.add(createAttribute(ATTR_CAN_LOGIN, false, true, true, true, Boolean.class));
        attributes.add(createAttribute(ATTR_NETID, false, true, true, true, String.class));
        attributes.add(createAttribute(ATTR_REQUIRE_CERTIFICATE, false, true, true, true, Boolean.class));
        attributes.add(createAttribute(ATTR_CERTIFICATE, false, true, true, true, String.class));
        attributes.add(createAttribute(ATTR_METADATA, false, true, true, true, String.class, true)); // Multi-valued

        // Validate essential attributes
        validateAttributes(attributes, ESSENTIAL_ATTRIBUTES);

        // Define ObjectClass for ePerson
        ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
        objectClassBuilder.setType("eperson");
        objectClassBuilder.addAllAttributeInfo(attributes);

        // Log the endpoint for this object type
        String endpoint = EndpointRegistry.getEndpoint("epersons");
        LOGGER.info("Endpoint for 'eperson': {}", endpoint);

        // Add ObjectClass to SchemaBuilder
        schemaBuilder.defineObjectClass(objectClassBuilder.build());

        // Log confirmation
        LOGGER.info("Esquema 'eperson' definido correctamente.");
    }

    /**
     * Helper method to create attributes with common properties.
     *
     * @param name          Attribute name.
     * @param required      Whether the attribute is required.
     * @param createable    Whether the attribute can be created.
     * @param updateable    Whether the attribute can be updated.
     * @param readable      Whether the attribute can be read.
     * @param type          The type of the attribute.
     * @param isMultiValued Whether the attribute is multi-valued.
     * @return Built AttributeInfo object.
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
     * @param attributeInfo AttributeInfo object to log.
     */
    private static void logAttributeInfo(AttributeInfo attributeInfo) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Atributo definido: {} (Requerido: {}, Creable: {}, Actualizable: {}, Legible: {})",
                    attributeInfo.getName(),
                    attributeInfo.isRequired(),
                    attributeInfo.isCreateable(),
                    attributeInfo.isUpdateable(),
                    attributeInfo.isReadable());
        }
    }

    /**
     * Validates that essential attributes are present in the schema.
     *
     * @param attributes          Set of attributes to validate.
     * @param essentialAttributes Set of essential attribute names.
     */
    private static void validateAttributes(Set<AttributeInfo> attributes, Set<String> essentialAttributes) {
        Set<String> definedAttributes = attributes.stream()
                .map(AttributeInfo::getName)
                .collect(Collectors.toSet());

        for (String attr : essentialAttributes) {
            if (!definedAttributes.contains(attr)) {
                String errorMessage = "El atributo esencial '" + attr + "' no está definido en el esquema.";
                LOGGER.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }
}
