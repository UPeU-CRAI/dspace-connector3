package com.upeu.connector;

import org.identityconnectors.framework.common.objects.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    void testConnectionValidation() {
        DSpaceConfiguration mockConfig = Mockito.mock(DSpaceConfiguration.class);
        when(mockConfig.getBaseUrl()).thenReturn("http://localhost:8080");
        when(mockConfig.getUsername()).thenReturn("admin");
        when(mockConfig.getPassword()).thenReturn("password");
        when(mockConfig.isInitialized()).thenReturn(true); // Simula que la configuración está inicializada

        connector.init(mockConfig); // Inicializa el conector con la configuración simulada
        Mockito.doNothing().when(mockClient).authenticate(); // Simula la autenticación del cliente

        assertDoesNotThrow(() -> connector.validate(), "La validación no debería lanzar una excepción");
    }

    @Test
    void testSchemaDefinition() {
        Schema schema = connector.schema(); // Obtén el esquema del conector

        // Verifica que la clase de objeto "eperson" contiene el atributo "Name"
        boolean nameAttributeExists = schema.getObjectClassInfo("eperson")
                .getAttributeInfo()
                .stream()
                .peek(attr -> System.out.println("Atributo encontrado: " + attr.getName())) // Log de depuración
                .anyMatch(attr -> "Name".equals(attr.getName()));

        assertTrue(nameAttributeExists, "El atributo 'Name' no está definido en el esquema");
    }

    @Test
    public void testMockedGetRequest() {
        String endpoint = "/test-endpoint";
        String mockResponse = "{\"key\": \"value\"}";

        // Simula la respuesta del cliente
        when(mockClient.get(endpoint)).thenReturn(mockResponse);

        // Verifica que el método get() no lanza excepciones y devuelve la respuesta correcta
        assertDoesNotThrow(() -> {
            String response = mockClient.get(endpoint);
            assertNotNull(response, "La respuesta no debería ser nula");
            assertEquals(mockResponse, response, "La respuesta debería coincidir con el valor simulado");
        });
    }
}
