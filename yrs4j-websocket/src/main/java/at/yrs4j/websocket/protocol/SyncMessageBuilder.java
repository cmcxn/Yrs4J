package at.yrs4j.websocket.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Builder for Y-WebSocket sync messages
 */
public class SyncMessageBuilder {
    
    /**
     * Create a sync request message with state vector
     */
    public static YWebSocketMessage createSyncRequest(byte[] stateVector) {
        byte[] payload = new byte[1 + stateVector.length];
        payload[0] = (byte) SyncMessageType.SYNC_REQUEST.getValue();
        System.arraycopy(stateVector, 0, payload, 1, stateVector.length);
        
        return new YWebSocketMessage(MessageType.SYNC, payload);
    }
    
    /**
     * Create a sync response message with update data
     */
    public static YWebSocketMessage createSyncResponse(byte[] update) {
        byte[] payload = new byte[1 + update.length];
        payload[0] = (byte) SyncMessageType.SYNC_RESPONSE.getValue();
        System.arraycopy(update, 0, payload, 1, update.length);
        
        return new YWebSocketMessage(MessageType.SYNC, payload);
    }
    
    /**
     * Create an update message with Y-CRDT update data
     */
    public static YWebSocketMessage createUpdate(byte[] update) {
        byte[] payload = new byte[1 + update.length];
        payload[0] = (byte) SyncMessageType.UPDATE.getValue();
        System.arraycopy(update, 0, payload, 1, update.length);
        
        return new YWebSocketMessage(MessageType.SYNC, payload);
    }
    
    /**
     * Create an awareness message
     */
    public static YWebSocketMessage createAwareness(byte[] awarenessUpdate) {
        return new YWebSocketMessage(MessageType.AWARENESS, awarenessUpdate);
    }
    
    /**
     * Parse sync message payload to get sync type and data
     */
    public static SyncMessage parseSyncMessage(byte[] payload) {
        if (payload.length < 1) {
            throw new IllegalArgumentException("Invalid sync message payload");
        }
        
        SyncMessageType syncType = SyncMessageType.fromValue(payload[0] & 0xFF);
        byte[] data = new byte[payload.length - 1];
        System.arraycopy(payload, 1, data, 0, data.length);
        
        return new SyncMessage(syncType, data);
    }
    
    /**
     * Represents a parsed sync message
     */
    public static class SyncMessage {
        private final SyncMessageType type;
        private final byte[] data;
        
        public SyncMessage(SyncMessageType type, byte[] data) {
            this.type = type;
            this.data = Arrays.copyOf(data, data.length);
        }
        
        public SyncMessageType getType() {
            return type;
        }
        
        public byte[] getData() {
            return Arrays.copyOf(data, data.length);
        }
    }
}