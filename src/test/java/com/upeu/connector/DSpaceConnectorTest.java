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

        // Configuración ficticia
        DSpaceConfiguration config = new DSpaceConfiguration();
        config.setBaseUrl("http://localhost:8080");
        config.setUsername("admin");
        config.setPassword("password");

        // Inicializa el conector
        connector = new DSpaceConnector();
        connector.init(config);

        // Inyecta los mocks
        connector.setAuthManager(mockAuthManager);
        connector.setClient(mockClient);

        // Habilita el modo de prueba en AuthManager
        when(mockAuthManager.getJwtToken()).thenReturn("mocked-jwt-token");
        when(mockAuthManager.isAuthenticated()).thenReturn(true);
        doNothing().when(mockAuthManager).obtainCsrfToken();
    }

    @Test
    public void testInitialization() {
        // Verifica que el conector esté correctamente inicializado
        assertNotNull(connector, "El conector debería estar inicializado");
    }

    @Test
    public void testConnectionValidation() {
        // Simula el comportamiento del método validate()
        assertDoesNotThrow(() -> connector.validate(), "La validación no debería lanzar excepción");
        verify(mockAuthManager, times(1)).isAuthenticated(); // Verifica que se haya llamado a isAuthenticated()
    }

    @Test
    public void testSchemaDefinition() {
        // Simula el comportamiento del método schema()
        Schema schema = connector.schema();

        // Valida que el esquema no sea nulo
        assertNotNull(schema, "El esquema no debería ser nulo");
        // Puedes agregar más validaciones según la estructura del esquema esperado
    }

    @Test
    public void testMockedGetRequest() throws Exception {
        String endpoint = "/test-endpoint";
        String mockResponse = "{\"key\": \"value\"}";

        // Simula una respuesta del cliente para una solicitud GET
        when(mockClient.get(endpoint)).thenReturn(mockResponse);

        // Llama al método get() del cliente a través del conector
        String response = connector.getClient().get(endpoint);

        // Valida que la respuesta coincida con la simulación
        assertEquals(mockResponse, response, "La respuesta debería coincidir con la simulación");

        // Verifica que el método get() se haya llamado exactamente una vez con el endpoint proporcionado
        verify(mockClient, times(1)).get(endpoint);
    }
}
