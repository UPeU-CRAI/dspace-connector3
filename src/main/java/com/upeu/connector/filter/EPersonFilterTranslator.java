package com.upeu.connector.filter;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.filter.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates Midpoint filters into query strings for the DSpace API.
 */
public class EPersonFilterTranslator implements FilterTranslator<String> {

    @Override
    public List<String> translate(Filter filter) {
        List<String> queries = new ArrayList<>();

        if (filter instanceof EqualsFilter) {
            Attribute attribute = ((EqualsFilter) filter).getAttribute();
            if ("email".equals(attribute.getName())) {
                String email = (String) attribute.getValue().get(0);
                queries.add("?email=" + email);
            }
        }

        // Agrega más condiciones según sea necesario
        return queries;
    }
}
