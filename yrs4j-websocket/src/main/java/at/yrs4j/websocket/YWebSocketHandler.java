package at.yrs4j.websocket;

import at.yrs4j.websocket.protocol.YWebSocketMessage;

/**
 * Interface for handling Y-WebSocket events
 */
public interface YWebSocketHandler {
    
    /**
     * Called when a WebSocket connection is established
     */
    void onConnect(String clientId);
    
    /**
     * Called when a WebSocket connection is closed
     */
    void onDisconnect(String clientId);
    
    /**
     * Called when a Y-WebSocket message is received
     */
    void onMessage(String clientId, YWebSocketMessage message);
    
    /**
     * Called when an error occurs
     */
    void onError(String clientId, Throwable error);
}