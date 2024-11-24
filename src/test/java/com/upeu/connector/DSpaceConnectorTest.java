package com.upeu.connector;

import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DSpaceConnector.
 */
public class DSpaceConnectorTest {

    private DSpaceConnector connector;

    @BeforeEach
    public void setUp() {
        connector = new DSpaceConnector();
        // Add setup logic here if necessary
    }

    @Test
    public void testInitialization() {
        assertNotNull(connector, "Connector should be initialized");
    }

    @Test
    public void testConnectionValidation() {
        // Example: Replace with actual logic when connector is implemented
        DSpaceConfiguration config = new DSpaceConfiguration();
        config.setBaseUrl("http://localhost:8080");
        config.setUsername("admin");
        config.setPassword("password");

        connector.init(config);
        assertDoesNotThrow(() -> connector.validate(), "Validation should not throw an exception");
    }

    @Test
    public void testSchemaDefinition() {
        assertDoesNotThrow(() -> {
            Schema schema = connector.schema(); // Asegúrate de usar el tipo Schema aquí
            boolean nameAttributeExists = schema.getObjectClassInfo().stream()
                    .flatMap(objectClassInfo -> objectClassInfo.getAttributeInfo().stream())
                    .anyMatch(attr -> "Name".equals(attr.getName()));
            assertTrue(nameAttributeExists, "Name attribute is missing in schema");
        });
    }
}
