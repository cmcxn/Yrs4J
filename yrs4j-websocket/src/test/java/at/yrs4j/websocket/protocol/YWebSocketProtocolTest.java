package at.yrs4j.websocket.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Y-WebSocket protocol message handling
 */
public class YWebSocketProtocolTest {
    
    @Test
    public void testMessageTypeValues() {
        assertEquals(0, MessageType.SYNC.getValue());
        assertEquals(1, MessageType.AWARENESS.getValue());
        assertEquals(2, MessageType.AUTH.getValue());
        assertEquals(3, MessageType.QUERY_AWARENESS.getValue());
        assertEquals(4, MessageType.ROOM_LIST.getValue());
    }
    
    @Test
    public void testMessageTypeFromValue() {
        assertEquals(MessageType.SYNC, MessageType.fromValue(0));
        assertEquals(MessageType.AWARENESS, MessageType.fromValue(1));
        assertEquals(MessageType.AUTH, MessageType.fromValue(2));
        assertEquals(MessageType.QUERY_AWARENESS, MessageType.fromValue(3));
        assertEquals(MessageType.ROOM_LIST, MessageType.fromValue(4));
    }
    
    @Test
    public void testInvalidMessageType() {
        assertThrows(IllegalArgumentException.class, () -> MessageType.fromValue(99));
    }
    
    @Test
    public void testSyncMessageTypeValues() {
        assertEquals(0, SyncMessageType.SYNC_REQUEST.getValue());
        assertEquals(1, SyncMessageType.SYNC_RESPONSE.getValue());
        assertEquals(2, SyncMessageType.UPDATE.getValue());
    }
    
    @Test
    public void testSyncMessageTypeFromValue() {
        assertEquals(SyncMessageType.SYNC_REQUEST, SyncMessageType.fromValue(0));
        assertEquals(SyncMessageType.SYNC_RESPONSE, SyncMessageType.fromValue(1));
        assertEquals(SyncMessageType.UPDATE, SyncMessageType.fromValue(2));
    }
    
    @Test
    public void testInvalidSyncMessageType() {
        assertThrows(IllegalArgumentException.class, () -> SyncMessageType.fromValue(99));
    }
    
    @Test
    public void testMessageEncodeDecode() {
        byte[] payload = {1, 2, 3, 4, 5};
        YWebSocketMessage original = new YWebSocketMessage(MessageType.SYNC, payload);
        
        byte[] encoded = original.encode();
        assertEquals(6, encoded.length); // 1 byte for type + 5 bytes payload
        assertEquals(0, encoded[0]); // SYNC type value
        
        YWebSocketMessage decoded = YWebSocketMessage.decode(encoded);
        assertEquals(MessageType.SYNC, decoded.getType());
        assertArrayEquals(payload, decoded.getPayload());
    }
    
    @Test
    public void testEmptyPayload() {
        YWebSocketMessage message = new YWebSocketMessage(MessageType.AWARENESS, null);
        byte[] encoded = message.encode();
        assertEquals(1, encoded.length);
        assertEquals(1, encoded[0]); // AWARENESS type value
        
        YWebSocketMessage decoded = YWebSocketMessage.decode(encoded);
        assertEquals(MessageType.AWARENESS, decoded.getType());
        assertEquals(0, decoded.getPayload().length);
    }
    
    @Test
    public void testInvalidDecodeData() {
        assertThrows(IllegalArgumentException.class, () -> YWebSocketMessage.decode(null));
        assertThrows(IllegalArgumentException.class, () -> YWebSocketMessage.decode(new byte[0]));
    }
    
    @Test
    public void testSyncMessageBuilder() {
        byte[] stateVector = {1, 2, 3};
        YWebSocketMessage syncRequest = SyncMessageBuilder.createSyncRequest(stateVector);
        
        assertEquals(MessageType.SYNC, syncRequest.getType());
        byte[] payload = syncRequest.getPayload();
        assertEquals(4, payload.length); // 1 byte for sync type + 3 bytes data
        assertEquals(0, payload[0]); // SYNC_REQUEST value
        
        // Parse the sync message
        SyncMessageBuilder.SyncMessage parsed = SyncMessageBuilder.parseSyncMessage(payload);
        assertEquals(SyncMessageType.SYNC_REQUEST, parsed.getType());
        assertArrayEquals(stateVector, parsed.getData());
    }
    
    @Test
    public void testCreateSyncResponse() {
        byte[] update = {10, 20, 30};
        YWebSocketMessage syncResponse = SyncMessageBuilder.createSyncResponse(update);
        
        assertEquals(MessageType.SYNC, syncResponse.getType());
        SyncMessageBuilder.SyncMessage parsed = SyncMessageBuilder.parseSyncMessage(syncResponse.getPayload());
        assertEquals(SyncMessageType.SYNC_RESPONSE, parsed.getType());
        assertArrayEquals(update, parsed.getData());
    }
    
    @Test
    public void testCreateUpdate() {
        byte[] update = {100, (byte) 200};
        YWebSocketMessage updateMessage = SyncMessageBuilder.createUpdate(update);
        
        assertEquals(MessageType.SYNC, updateMessage.getType());
        SyncMessageBuilder.SyncMessage parsed = SyncMessageBuilder.parseSyncMessage(updateMessage.getPayload());
        assertEquals(SyncMessageType.UPDATE, parsed.getType());
        assertArrayEquals(update, parsed.getData());
    }
    
    @Test
    public void testCreateAwareness() {
        byte[] awarenessData = {50, 60, 70};
        YWebSocketMessage awarenessMessage = SyncMessageBuilder.createAwareness(awarenessData);
        
        assertEquals(MessageType.AWARENESS, awarenessMessage.getType());
        assertArrayEquals(awarenessData, awarenessMessage.getPayload());
    }
    
    @Test
    public void testParseSyncMessageInvalid() {
        assertThrows(IllegalArgumentException.class, () -> 
            SyncMessageBuilder.parseSyncMessage(new byte[0]));
    }
}