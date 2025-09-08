package at.yrs4j.websocket.protocol;

import java.util.Arrays;

/**
 * Represents a Y-WebSocket protocol message
 */
public class YWebSocketMessage {
    private final MessageType type;
    private final byte[] payload;
    
    public YWebSocketMessage(MessageType type, byte[] payload) {
        this.type = type;
        this.payload = payload != null ? Arrays.copyOf(payload, payload.length) : new byte[0];
    }
    
    public MessageType getType() {
        return type;
    }
    
    public byte[] getPayload() {
        return Arrays.copyOf(payload, payload.length);
    }
    
    /**
     * Encode message to binary format for WebSocket transmission
     */
    public byte[] encode() {
        byte[] encoded = new byte[1 + payload.length];
        encoded[0] = (byte) type.getValue();
        System.arraycopy(payload, 0, encoded, 1, payload.length);
        return encoded;
    }
    
    /**
     * Decode binary data to YWebSocketMessage
     */
    public static YWebSocketMessage decode(byte[] data) {
        if (data == null || data.length < 1) {
            throw new IllegalArgumentException("Invalid message data");
        }
        
        MessageType type = MessageType.fromValue(data[0] & 0xFF);
        byte[] payload = new byte[data.length - 1];
        System.arraycopy(data, 1, payload, 0, payload.length);
        
        return new YWebSocketMessage(type, payload);
    }
    
    @Override
    public String toString() {
        return "YWebSocketMessage{" +
                "type=" + type +
                ", payloadLength=" + payload.length +
                '}';
    }
}