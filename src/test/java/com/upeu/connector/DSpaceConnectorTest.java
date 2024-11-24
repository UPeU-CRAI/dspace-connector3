package com.upeu.connector;

import org.identityconnectors.framework.common.objects.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for DSpaceConnector using a mocked DSpaceClient.
 */
public class DSpaceConnectorTest {

    private DSpaceConnector connector;

    @Mock
    private DSpaceClient mockClient; // Mock del cliente DSpace

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        DSpaceConfiguration config = new DSpaceConfiguration();
        config.setBaseUrl("http://localhost:8080");
        config.setUsername("admin");
        config.setPassword("password");

        connector = new DSpaceConnector();
        connector.setClient(mockClient); // Usa el mock en lugar de inicializar el cliente real
    }

    @Test
    public void testInitialization() {
        assertNotNull(connector, "Connector should be initialized");
    }

    @Test
    public void testConnectionValidation() {
        assertDoesNotThrow(() -> {
            doNothing().when(mockClient).authenticate(); // Simula éxito
            connector.validate();
        });
    }

    @Test
    public void testSchemaDefinition() {
        assertDoesNotThrow(() -> {
            Schema schema = connector.schema();
            boolean nameAttributeExists = schema.getObjectClassInfo().stream()
                    .flatMap(objectClassInfo -> objectClassInfo.getAttributeInfo().stream())
                    .anyMatch(attr -> "Name".equals(attr.getName()));
            assertTrue(nameAttributeExists, "Name attribute is missing in schema");
        });
    }

    @Test
    public void testMockedGetRequest() {
        String endpoint = "/test-endpoint";
        String mockResponse = "{\"key\": \"value\"}";

        try {
            when(mockClient.get(endpoint)).thenReturn(mockResponse); // Simular respuesta

            assertDoesNotThrow(() -> {
                String response = mockClient.get(endpoint);
                assertNotNull(response, "Response should not be null");
                assertEquals(mockResponse, response, "Response should match the mocked value");
            });
        } catch (Exception e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }
}
