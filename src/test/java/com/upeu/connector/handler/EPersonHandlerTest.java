package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import com.upeu.connector.DSpaceConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for EPersonHandler.
 */
public class EPersonHandlerTest {

    private EPersonHandler handler;
    private DSpaceClient mockClient;

    @BeforeEach
    public void setUp() {
        mockClient = mock(DSpaceClient.class); // Mock the DSpaceClient
        handler = new EPersonHandler(mockClient);
    }

    @Test
    public void testCreateEPerson() {
        // Mock behavior
        String testPayload = "{\"email\":\"test@example.com\"}";
        when(mockClient.post("/eperson/epersons", testPayload))
                .thenReturn("{\"id\":\"12345\",\"email\":\"test@example.com\"}");

        // Execute method
        String result = handler.createEPerson(testPayload);

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals("{\"id\":\"12345\",\"email\":\"test@example.com\"}", result, "Response should match mock result");
        verify(mockClient, times(1)).post("/eperson/epersons", testPayload);
    }

    @Test
    public void testGetEPerson() {
        // Mock behavior
        String testId = "12345";
        when(mockClient.get("/eperson/epersons/12345"))
                .thenReturn("{\"id\":\"12345\",\"email\":\"test@example.com\"}");

        // Execute method
        String result = handler.getEPerson(testId);

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals("{\"id\":\"12345\",\"email\":\"test@example.com\"}", result, "Response should match mock result");
        verify(mockClient, times(1)).get("/eperson/epersons/12345");
    }

    @Test
    public void testUpdateEPerson() {
        // Mock behavior
        String testId = "12345";
        String testPayload = "{\"email\":\"updated@example.com\"}";
        when(mockClient.put("/eperson/epersons/12345", testPayload))
                .thenReturn("{\"id\":\"12345\",\"email\":\"updated@example.com\"}");

        // Execute method
        String result = handler.updateEPerson(testId, testPayload);

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertEquals("{\"id\":\"12345\",\"email\":\"updated@example.com\"}", result, "Response should match mock result");
        verify(mockClient, times(1)).put("/eperson/epersons/12345", testPayload);
    }

    @Test
    public void testDeleteEPerson() {
        // Mock behavior
        String testId = "12345";
        doNothing().when(mockClient).delete("/eperson/epersons/12345");

        // Execute method
        assertDoesNotThrow(() -> handler.deleteEPerson(testId), "Deletion should not throw any exception");

        // Verify client interaction
        verify(mockClient, times(1)).delete("/eperson/epersons/12345");
    }
}
