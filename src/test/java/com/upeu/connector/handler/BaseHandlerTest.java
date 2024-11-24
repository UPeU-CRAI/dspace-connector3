package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for BaseHandler.
 */
public class BaseHandlerTest {

    private BaseHandler handler;
    private DSpaceClient mockClient;

    @BeforeEach
    public void setUp() {
        mockClient = mock(DSpaceClient.class); // Mock the DSpaceClient
        handler = new BaseHandler(mockClient) {
            // Providing a concrete implementation for testing
        };
    }

    @Test
    public void testGetClient() {
        // Verify that the client is correctly set
        assertNotNull(handler.getClient(), "Client should not be null");
        assertEquals(mockClient, handler.getClient(), "Client should match the mocked instance");
    }

    @Test
    public void testValidateJson() {
        // Valid JSON
        String validJson = "{\"key\":\"value\"}";
        assertDoesNotThrow(() -> handler.validateJson(validJson), "Valid JSON should not throw exception");

        // Invalid JSON
        String invalidJson = "{key:value}";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> handler.validateJson(invalidJson));
        assertEquals("Invalid JSON format", exception.getMessage(), "Exception message should match");
    }

    @Test
    public void testLogMessage() {
        // Verify that logging does not throw any exceptions
        assertDoesNotThrow(() -> handler.logMessage("INFO", "Test log message"), "Logging should not throw exception");
    }
}
