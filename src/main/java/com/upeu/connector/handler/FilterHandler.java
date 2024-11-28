package com.upeu.connector.handler;

import com.upeu.connector.filter.EPersonFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler para gestionar operaciones de filtrado en el conector DSpace.
 */
public class FilterHandler {

    private final EPersonFilterTranslator ePersonFilterTranslator;

    /**
     * Constructor de FilterHandler.
     */
    public FilterHandler() {
        this.ePersonFilterTranslator = new EPersonFilterTranslator(); // Traductor para filtros de EPerson
    }

    /**
     * Traduce un filtro en una lista de parámetros de consulta.
     *
     * @param filter El filtro a traducir. Puede ser nulo.
     * @return Una lista de cadenas representando parámetros de consulta.
     */
    public List<String> translateFilter(Filter filter) {
        if (filter == null) {
            // Si el filtro es nulo, devolver una lista vacía (sin parámetros)
            return new ArrayList<>();
        }

        // Delegar la traducción de filtros a EPersonFilterTranslator
        return ePersonFilterTranslator.translate(filter);
    }

    /**
     * Valida el filtro proporcionado.
     *
     * @param filter El filtro a validar.
     * @throws IllegalArgumentException si el filtro es inválido.
     */
    public void validateFilter(Filter filter) {
        if (filter == null) {
            // No es obligatorio que el filtro esté presente; manejarlo como válido
            return;
        }

        // Validar si el filtro es una instancia de los tipos soportados
        if (!isSupportedFilter(filter)) {
            throw new IllegalArgumentException("Tipo de filtro no soportado: " + filter.getClass().getSimpleName());
        }
    }

    /**
     * Verifica si un filtro es de un tipo soportado.
     *
     * @param filter El filtro a verificar.
     * @return Verdadero si el filtro es soportado; falso de lo contrario.
     */
    private boolean isSupportedFilter(Filter filter) {
        return filter instanceof EqualsFilter ||
                filter instanceof ContainsFilter ||
                filter instanceof StartsWithFilter ||
                filter instanceof EndsWithFilter;
    }
}
