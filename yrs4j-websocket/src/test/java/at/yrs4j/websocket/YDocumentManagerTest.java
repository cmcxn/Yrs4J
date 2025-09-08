package at.yrs4j.websocket;

import at.yrs4j.wrapper.interfaces.YDoc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for YDocumentManager
 * Note: Some tests are disabled if Yrs4J native libraries are not available
 */
public class YDocumentManagerTest extends WebSocketTestBase {
    
    private YDocumentManager manager;
    
    @BeforeEach
    public void setUp() {
        manager = new YDocumentManager();
    }
    
    @Test
    public void testBasicRoomManagement() {
        manager.joinRoom("client1", "room1");
        assertEquals("room1", manager.getClientRoom("client1"));
        
        manager.joinRoom("client2", "room1");
        assertEquals("room1", manager.getClientRoom("client2"));
        
        manager.leaveRoom("client1");
        assertNull(manager.getClientRoom("client1"));
        assertEquals("room1", manager.getClientRoom("client2")); // client2 should still be in room
    }
    
    @Test
    public void testGetClientsInRoom() {
        manager.joinRoom("client1", "room1");
        manager.joinRoom("client2", "room1");
        manager.joinRoom("client3", "room2");
        
        var room1Clients = manager.getClientsInRoom("room1");
        assertEquals(2, room1Clients.size());
        assertTrue(room1Clients.containsKey("client1"));
        assertTrue(room1Clients.containsKey("client2"));
        
        var room2Clients = manager.getClientsInRoom("room2");
        assertEquals(1, room2Clients.size());
        assertTrue(room2Clients.containsKey("client3"));
    }
    
    @Test
    public void testAwarenessManagement() {
        byte[] awarenessData = "client1 awareness".getBytes();
        manager.updateAwareness("client1", "room1", awarenessData);
        
        var roomAwareness = manager.getRoomAwareness("room1");
        assertEquals(1, roomAwareness.size());
        assertArrayEquals(awarenessData, roomAwareness.get("client1"));
        
        // Update awareness for another client
        byte[] awarenessData2 = "client2 awareness".getBytes();
        manager.updateAwareness("client2", "room1", awarenessData2);
        
        roomAwareness = manager.getRoomAwareness("room1");
        assertEquals(2, roomAwareness.size());
        assertArrayEquals(awarenessData, roomAwareness.get("client1"));
        assertArrayEquals(awarenessData2, roomAwareness.get("client2"));
    }
    
    @Test
    public void testAwarenessRemovedOnLeaveRoom() {
        manager.joinRoom("client1", "room1");
        byte[] awarenessData = "test awareness".getBytes();
        manager.updateAwareness("client1", "room1", awarenessData);
        
        var roomAwareness = manager.getRoomAwareness("room1");
        assertEquals(1, roomAwareness.size());
        
        manager.leaveRoom("client1");
        roomAwareness = manager.getRoomAwareness("room1");
        assertEquals(0, roomAwareness.size());
    }
    
    @Test
    public void testCleanup() {
        manager.joinRoom("client1", "room1");
        manager.updateAwareness("client1", "room1", "test".getBytes());
        
        manager.cleanup();
        
        // After cleanup, everything should be cleared
        assertNull(manager.getClientRoom("client1"));
        assertEquals(0, manager.getRoomAwareness("room1").size());
        assertEquals(0, manager.getClientsInRoom("room1").size());
    }
    
    // Tests that require native libraries are disabled for now
    // They can be enabled when running with proper native library setup
    
    @Test
    @org.junit.jupiter.api.Disabled("Requires native library initialization")
    public void testGetOrCreateDocument() {
        YDoc doc1 = manager.getOrCreateDocument("room1");
        assertNotNull(doc1);
        
        YDoc doc2 = manager.getOrCreateDocument("room1");
        assertSame(doc1, doc2); // Should return same instance
        
        YDoc doc3 = manager.getOrCreateDocument("room2");
        assertNotSame(doc1, doc3); // Different room should have different doc
    }
    
    // Additional Y-CRDT integration tests would go here when native libraries are available
}