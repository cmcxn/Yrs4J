package at.yrs4j.websocket.protocol;

/**
 * Y-WebSocket sync message subtypes
 */
public enum SyncMessageType {
    SYNC_REQUEST(0),
    SYNC_RESPONSE(1),
    UPDATE(2);
    
    private final int value;
    
    SyncMessageType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static SyncMessageType fromValue(int value) {
        for (SyncMessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown sync message type: " + value);
    }
}