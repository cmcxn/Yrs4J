package at.yrs4j.websocket.protocol;

/**
 * Y-WebSocket message types following the y-websocket protocol
 */
public enum MessageType {
    SYNC(0),
    AWARENESS(1),
    AUTH(2),
    QUERY_AWARENESS(3),
    ROOM_LIST(4);
    
    private final int value;
    
    MessageType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static MessageType fromValue(int value) {
        for (MessageType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + value);
    }
}