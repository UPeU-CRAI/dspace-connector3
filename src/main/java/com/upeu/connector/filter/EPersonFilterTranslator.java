package com.upeu.connector.filter;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.filter.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates Midpoint filters into query strings for the DSpace API.
 */
public class EPersonFilterTranslator implements FilterTranslator<String> {

    /**
     * Translates a Midpoint filter into a list of query parameters for the DSpace API.
     *
     * @param filter The Midpoint filter.
     * @return A list of query strings for the API.
     */
    @Override
    public List<String> translate(Filter filter) {
        List<String> queries = new ArrayList<>();

        if (filter instanceof EqualsFilter) {
            translateEqualsFilter((EqualsFilter) filter, queries);
        } else if (filter instanceof ContainsFilter) {
            translateContainsFilter((ContainsFilter) filter, queries);
        } else if (filter instanceof StartsWithFilter) {
            translateStartsWithFilter((StartsWithFilter) filter, queries);
        } else if (filter instanceof EndsWithFilter) {
            translateEndsWithFilter((EndsWithFilter) filter, queries);
        } else {
            throw new UnsupportedOperationException("Unsupported filter type: " + filter.getClass().getSimpleName());
        }

        return queries;
    }

    /**
     * Handles translation of EqualsFilter into query parameters.
     *
     * @param filter The EqualsFilter instance.
     * @param queries The list of query parameters to append to.
     */
    private void translateEqualsFilter(EqualsFilter filter, List<String> queries) {
        Attribute attribute = filter.getAttribute();
        String attributeName = attribute.getName();
        Object value = attribute.getValue().get(0);

        if ("email".equalsIgnoreCase(attributeName)) {
            queries.add("?email=" + encode(value.toString()));
        } else if ("firstname".equalsIgnoreCase(attributeName)) {
            queries.add("?firstname=" + encode(value.toString()));
        } else if ("lastname".equalsIgnoreCase(attributeName)) {
            queries.add("?lastname=" + encode(value.toString()));
        } else {
            throw new UnsupportedOperationException("Unsupported EqualsFilter attribute: " + attributeName);
        }
    }

    /**
     * Handles translation of ContainsFilter into query parameters.
     *
     * @param filter The ContainsFilter instance.
     * @param queries The list of query parameters to append to.
     */
    private void translateContainsFilter(ContainsFilter filter, List<String> queries) {
        Attribute attribute = filter.getAttribute();
        String attributeName = attribute.getName();
        Object value = attribute.getValue().get(0);

        if ("email".equalsIgnoreCase(attributeName)) {
            queries.add("?email_contains=" + encode(value.toString()));
        } else if ("firstname".equalsIgnoreCase(attributeName)) {
            queries.add("?firstname_contains=" + encode(value.toString()));
        } else if ("lastname".equalsIgnoreCase(attributeName)) {
            queries.add("?lastname_contains=" + encode(value.toString()));
        } else {
            throw new UnsupportedOperationException("Unsupported ContainsFilter attribute: " + attributeName);
        }
    }

    /**
     * Handles translation of StartsWithFilter into query parameters.
     *
     * @param filter The StartsWithFilter instance.
     * @param queries The list of query parameters to append to.
     */
    private void translateStartsWithFilter(StartsWithFilter filter, List<String> queries) {
        Attribute attribute = filter.getAttribute();
        String attributeName = attribute.getName();
        Object value = attribute.getValue().get(0);

        if ("email".equalsIgnoreCase(attributeName)) {
            queries.add("?email_starts=" + encode(value.toString()));
        } else if ("firstname".equalsIgnoreCase(attributeName)) {
            queries.add("?firstname_starts=" + encode(value.toString()));
        } else if ("lastname".equalsIgnoreCase(attributeName)) {
            queries.add("?lastname_starts=" + encode(value.toString()));
        } else {
            throw new UnsupportedOperationException("Unsupported StartsWithFilter attribute: " + attributeName);
        }
    }

    /**
     * Handles translation of EndsWithFilter into query parameters.
     *
     * @param filter The EndsWithFilter instance.
     * @param queries The list of query parameters to append to.
     */
    private void translateEndsWithFilter(EndsWithFilter filter, List<String> queries) {
        Attribute attribute = filter.getAttribute();
        String attributeName = attribute.getName();
        Object value = attribute.getValue().get(0);

        if ("email".equalsIgnoreCase(attributeName)) {
            queries.add("?email_ends=" + encode(value.toString()));
        } else if ("firstname".equalsIgnoreCase(attributeName)) {
            queries.add("?firstname_ends=" + encode(value.toString()));
        } else if ("lastname".equalsIgnoreCase(attributeName)) {
            queries.add("?lastname_ends=" + encode(value.toString()));
        } else {
            throw new UnsupportedOperationException("Unsupported EndsWithFilter attribute: " + attributeName);
        }
    }

    /**
     * Encodes a value for safe usage in a URL query parameter.
     *
     * @param value The value to encode.
     * @return The URL-encoded value.
     */
    private String encode(String value) {
        return value.replace(" ", "%20"); // Simplified encoding (use a library like URLEncoder for production).
    }
}
