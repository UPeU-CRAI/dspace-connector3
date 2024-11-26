package com.upeu.connector;

import com.upeu.connector.auth.AuthManager;
import org.identityconnectors.framework.common.objects.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DSpaceConnectorTest {

    private DSpaceConnector connector;

    @Mock
    private DSpaceClient mockClient; // Mock para DSpaceClient

    @Mock
    private AuthManager mockAuthManager; // Mock para AuthManager

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        DSpaceConfiguration config = new DSpaceConfiguration();
        config.setBaseUrl("http://localhost:8080");
        config.setUsername("admin");
        config.setPassword("password");

        connector = new DSpaceConnector();
        connector.init(config); // Inicializa el conector

        // Inyectar mocks
        connector.setAuthManager(mockAuthManager);
        connector.setClient(mockClient);

        // Mockear métodos de AuthManager
        when(mockAuthManager.getJwtToken()).thenReturn("mocked-jwt-token");
        when(mockAuthManager.isAuthenticated()).thenReturn(true);
    }

    @Test
    public void testInitialization() {
        assertNotNull(connector, "Connector debería estar inicializado");
    }

    @Test
    public void testConnectionValidation() {
        // Simula comportamiento del método validate()
        assertDoesNotThrow(() -> connector.validate(), "La validación no debería lanzar excepción");
        verify(mockAuthManager, times(1)).isAuthenticated();
    }

    @Test
    public void testSchemaDefinition() {
        // Simula comportamiento del esquema
        Schema schema = connector.schema();

        assertNotNull(schema, "El esquema no debería ser nulo");
        // Más validaciones si es necesario
    }

    @Test
    public void testMockedGetRequest() throws Exception {
        String endpoint = "/test-endpoint";
        String mockResponse = "{\"key\": \"value\"}";

        // Simular respuesta del cliente
        when(mockClient.get(endpoint)).thenReturn(mockResponse);

        // Llamada al método get()
        String response = connector.getClient().get(endpoint);

        assertEquals(mockResponse, response, "La respuesta debería coincidir con la simulación");
        verify(mockClient, times(1)).get(endpoint);
    }
}
