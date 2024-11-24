package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    public void testGetAllEPersons() throws Exception {
        // Mock behavior
        String mockResponse = """
                {
                  "_embedded": {
                    "epersons": [
                      {
                        "id": "1",
                        "email": "user1@example.com",
                        "metadata": {
                          "eperson.firstname": [{"value": "User1"}],
                          "eperson.lastname": [{"value": "One"}]
                        },
                        "canLogIn": true
                      },
                      {
                        "id": "2",
                        "email": "user2@example.com",
                        "metadata": {
                          "eperson.firstname": [{"value": "User2"}],
                          "eperson.lastname": [{"value": "Two"}]
                        },
                        "canLogIn": false
                      }
                    ]
                  }
                }""";

        when(mockClient.get("/server/api/eperson/epersons")).thenReturn(mockResponse);

        // Execute method
        List<EPerson> ePersons = handler.getAllEPersons();

        // Verify results
        assertNotNull(ePersons, "ePersons list should not be null");
        assertEquals(2, ePersons.size(), "ePersons list should contain 2 entries");

        EPerson firstPerson = ePersons.get(0);
        assertEquals("1", firstPerson.getId());
        assertEquals("user1@example.com", firstPerson.getEmail());
        assertEquals("User1", firstPerson.getFirstName());
        assertEquals("One", firstPerson.getLastName());
        assertTrue(firstPerson.canLogIn());

        verify(mockClient, times(1)).get("/server/api/eperson/epersons");
    }

    @Test
    public void testGetEPersonById() throws Exception {
        // Mock behavior
        String testId = "12345";
        String mockResponse = """
                {
                  "id": "12345",
                  "email": "test@example.com",
                  "metadata": {
                    "eperson.firstname": [{"value": "Test"}],
                    "eperson.lastname": [{"value": "User"}]
                  },
                  "canLogIn": true
                }""";

        when(mockClient.get("/server/api/eperson/epersons/" + testId)).thenReturn(mockResponse);

        // Execute method
        EPerson ePerson = handler.getEPersonById(testId);

        // Verify results
        assertNotNull(ePerson, "ePerson should not be null");
        assertEquals("12345", ePerson.getId());
        assertEquals("test@example.com", ePerson.getEmail());
        assertEquals("Test", ePerson.getFirstName());
        assertEquals("User", ePerson.getLastName());
        assertTrue(ePerson.canLogIn());

        verify(mockClient, times(1)).get("/server/api/eperson/epersons/" + testId);
    }

    @Test
    public void testCreateEPerson() throws Exception {
        // Mock behavior
        JSONObject requestBody = new JSONObject();
        requestBody.put("metadata", new JSONObject()
                .put("eperson.firstname", List.of(new JSONObject().put("value", "Test")))
                .put("eperson.lastname", List.of(new JSONObject().put("value", "User")))
                .put("eperson.email", List.of(new JSONObject().put("value", "test@example.com")))
                .put("eperson.active", List.of(new JSONObject().put("value", true))));

        String mockResponse = """
                {
                  "id": "12345",
                  "email": "test@example.com",
                  "metadata": {
                    "eperson.firstname": [{"value": "Test"}],
                    "eperson.lastname": [{"value": "User"}]
                  },
                  "canLogIn": true
                }""";

        when(mockClient.post("/server/api/eperson/epersons", requestBody.toString())).thenReturn(mockResponse);

        // Execute method
        EPerson ePerson = handler.createEPerson("Test", "User", "test@example.com", true);

        // Verify results
        assertNotNull(ePerson, "ePerson should not be null");
        assertEquals("12345", ePerson.getId());
        assertEquals("test@example.com", ePerson.getEmail());
        assertEquals("Test", ePerson.getFirstName());
        assertEquals("User", ePerson.getLastName());
        assertTrue(ePerson.canLogIn());

        verify(mockClient, times(1)).post(eq("/server/api/eperson/epersons"), anyString());
    }

    @Test
    public void testUpdateEPerson() throws Exception {
        String testId = "12345";
        JSONObject updates = new JSONObject();
        updates.put("eperson.firstname", List.of(new JSONObject().put("value", "Updated")));

        when(mockClient.put("/server/api/eperson/epersons/" + testId, updates.toString()))
                .thenReturn("""
            {
                "id": "12345",
                "email": "test@example.com",
                "metadata": {
                    "eperson.firstname": [{"value": "Updated"}],
                    "eperson.lastname": [{"value": "User"}]
                },
                "canLogIn": true
            }
        """);

        EPerson ePerson = handler.updateEPerson(testId, updates);

        assertNotNull(ePerson, "ePerson should not be null");
        assertEquals("12345", ePerson.getId());
        assertEquals("Updated", ePerson.getFirstName());
        verify(mockClient, times(1)).put(eq("/server/api/eperson/epersons/" + testId), anyString());
    }

    @Test
    public void testDeleteEPerson() throws Exception {
        // Mock behavior
        String testId = "12345";
        doNothing().when(mockClient).delete("/server/api/eperson/epersons/" + testId);

        // Execute method
        assertDoesNotThrow(() -> handler.deleteEPerson(testId), "Deletion should not throw any exception");

        // Verify client interaction
        verify(mockClient, times(1)).delete("/server/api/eperson/epersons/" + testId);
    }
}
