package com.upeu.connector.filter;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.filter.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates MidPoint filters into query strings for the DSpace EPerson API.
 */
public class EPersonFilterTranslator implements FilterTranslator<String> {

    private static final String UNSUPPORTED_FILTER_TYPE = "Unsupported filter type: ";
    private static final String UNSUPPORTED_ATTRIBUTE = "Unsupported attribute for filter: ";
    private static final String BY_EMAIL_ENDPOINT = "/api/eperson/epersons/search/byEmail";
    private static final String BY_METADATA_ENDPOINT = "/api/eperson/epersons/search/byMetadata";

    /**
     * Translates a MidPoint filter into a list of query parameters for the DSpace EPerson API.
     *
     * @param filter The MidPoint filter.
     * @return A list of query strings for the API.
     */
    @Override
    public List<String> translate(Filter filter) {
        if (filter == null) {
            // Si no hay filtro, devolver una lista vacía (no se aplica ningún filtro).
            return List.of();
        }

        if (filter instanceof EqualsFilter) {
            Attribute attribute = ((EqualsFilter) filter).getAttribute();
            return List.of(attribute.getName() + "=" + attribute.getValue().get(0).toString());
        }
        // Manejar otros tipos de filtros según sea necesario.
        throw new UnsupportedOperationException("Unsupported filter type: " + filter.getClass().getSimpleName());
    }

    /**
     * Handles translation of EqualsFilter for email-specific searches.
     *
     * @param filter The EqualsFilter instance.
     * @param queries The list of query parameters to append to.
     */
    private void translateEqualsFilter(EqualsFilter filter, List<String> queries) {
        Attribute attribute = extractAttribute(filter);
        String attributeName = attribute.getName();
        String value = extractAttributeValue(attribute);

        if ("email".equalsIgnoreCase(attributeName)) {
            // Use the specific endpoint for email-based searches
            queries.add(BY_EMAIL_ENDPOINT + "?email=" + encode(value));
        } else {
            // For all other attributes, fallback to metadata-based search
            queries.add(BY_METADATA_ENDPOINT + "?query=" + encode(value));
        }
    }

    /**
     * Handles generic filters for metadata-based searches.
     *
     * @param filter The filter instance.
     * @param queries The list of query parameters to append to.
     */
    private void translateGenericFilter(Filter filter, List<String> queries) {
        Attribute attribute = extractAttribute(filter);
        String attributeName = attribute.getName();
        String value = extractAttributeValue(attribute);

        // Metadata-based searches support first name, last name, email, and UUID
        switch (attributeName.toLowerCase()) {
            case "email":
            case "firstname":
            case "lastname":
            case "uuid":
                queries.add(BY_METADATA_ENDPOINT + "?query=" + encode(value));
                break;
            default:
                throw new UnsupportedOperationException(UNSUPPORTED_ATTRIBUTE + attributeName);
        }
    }

    /**
     * Extracts the attribute from a filter.
     *
     * @param filter The filter instance.
     * @return The attribute from the filter.
     */
    private Attribute extractAttribute(Filter filter) {
        if (filter instanceof AttributeFilter) {
            return ((AttributeFilter) filter).getAttribute();
        }
        throw new IllegalArgumentException("Filter does not contain an attribute: " + filter.getClass().getSimpleName());
    }

    /**
     * Extracts the value from an attribute.
     *
     * @param attribute The attribute instance.
     * @return The first value of the attribute.
     */
    private String extractAttributeValue(Attribute attribute) {
        if (attribute.getValue() == null || attribute.getValue().isEmpty()) {
            throw new IllegalArgumentException("Attribute value cannot be null or empty for: " + attribute.getName());
        }
        Object value = attribute.getValue().get(0);
        return value != null ? value.toString() : "";
    }

    /**
     * Encodes a value for safe usage in a URL query parameter.
     *
     * @param value The value to encode.
     * @return The URL-encoded value.
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
