package at.yrs4j.websocket.server;

import at.yrs4j.websocket.YDocumentManager;
import at.yrs4j.websocket.YWebSocketHandler;
import at.yrs4j.websocket.protocol.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Y-WebSocket server implementation
 */
public class YWebSocketServerImpl extends WebSocketServer {
    private final YDocumentManager documentManager;
    private final YWebSocketHandler handler;
    private final Map<WebSocket, String> connectionIds = new ConcurrentHashMap<>();
    private final Map<String, WebSocket> idToConnection = new ConcurrentHashMap<>();
    
    public YWebSocketServerImpl(int port, YDocumentManager documentManager, YWebSocketHandler handler) {
        super(new InetSocketAddress(port));
        this.documentManager = documentManager;
        this.handler = handler;
    }
    
    public YWebSocketServerImpl(InetSocketAddress address, YDocumentManager documentManager, YWebSocketHandler handler) {
        super(address);
        this.documentManager = documentManager;
        this.handler = handler;
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String clientId = generateClientId(conn);
        connectionIds.put(conn, clientId);
        idToConnection.put(clientId, conn);
        
        try {
            handler.onConnect(clientId);
        } catch (Exception e) {
            handler.onError(clientId, e);
        }
    }
    
    @Override
    public void onStart() {
        System.out.println("Y-WebSocket server started on " + getAddress());
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientId = connectionIds.remove(conn);
        if (clientId != null) {
            idToConnection.remove(clientId);
            documentManager.leaveRoom(clientId);
            
            try {
                handler.onDisconnect(clientId);
            } catch (Exception e) {
                handler.onError(clientId, e);
            }
        }
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        // Text messages are not supported in y-websocket protocol
        String clientId = connectionIds.get(conn);
        if (clientId != null) {
            handler.onError(clientId, new UnsupportedOperationException("Text messages not supported"));
        }
    }
    
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        String clientId = connectionIds.get(conn);
        if (clientId == null) {
            return;
        }
        
        try {
            byte[] data = new byte[message.remaining()];
            message.get(data);
            
            YWebSocketMessage wsMessage = YWebSocketMessage.decode(data);
            
            processMessage(clientId, wsMessage);
            
            handler.onMessage(clientId, wsMessage);
            
        } catch (Exception e) {
            handler.onError(clientId, e);
        }
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        String clientId = connectionIds.get(conn);
        if (clientId != null) {
            handler.onError(clientId, ex);
        }
    }
    
    /**
     * Send a message to a specific client
     */
    public void sendMessage(String clientId, YWebSocketMessage message) {
        WebSocket conn = idToConnection.get(clientId);
        if (conn != null && conn.isOpen()) {
            conn.send(message.encode());
        }
    }
    
    /**
     * Broadcast a message to all clients in a room
     */
    public void broadcastToRoom(String roomName, YWebSocketMessage message, String excludeClientId) {
        Map<String, String> clients = documentManager.getClientsInRoom(roomName);
        for (String clientId : clients.keySet()) {
            if (!clientId.equals(excludeClientId)) {
                sendMessage(clientId, message);
            }
        }
    }
    
    /**
     * Join a client to a room
     */
    public void joinRoom(String clientId, String roomName) {
        documentManager.joinRoom(clientId, roomName);
        
        // Send initial sync
        byte[] stateVector = documentManager.getStateVector(roomName);
        YWebSocketMessage syncRequest = SyncMessageBuilder.createSyncRequest(stateVector);
        sendMessage(clientId, syncRequest);
    }
    
    /**
     * Get connected client IDs for testing
     */
    public java.util.Set<String> getConnectedClientIds() {
        return java.util.Collections.unmodifiableSet(idToConnection.keySet());
    }
    
    private void processMessage(String clientId, YWebSocketMessage message) {
        String roomName = documentManager.getClientRoom(clientId);
        
        switch (message.getType()) {
            case SYNC:
                processSyncMessage(clientId, roomName, message);
                break;
            case AWARENESS:
                processAwarenessMessage(clientId, roomName, message);
                break;
            case AUTH:
                // TODO: Implement authentication
                break;
            case QUERY_AWARENESS:
                // TODO: Implement awareness query
                break;
            default:
                // Ignore unknown message types
                break;
        }
    }
    
    private void processSyncMessage(String clientId, String roomName, YWebSocketMessage message) {
        if (roomName == null) {
            return; // Client not in a room
        }
        
        try {
            SyncMessageBuilder.SyncMessage syncMsg = SyncMessageBuilder.parseSyncMessage(message.getPayload());
            
            switch (syncMsg.getType()) {
                case SYNC_REQUEST:
                    // Send our state diff to the client
                    byte[] stateDiff = documentManager.getStateDiff(roomName, syncMsg.getData());
                    if (stateDiff.length > 0) {
                        YWebSocketMessage response = SyncMessageBuilder.createSyncResponse(stateDiff);
                        sendMessage(clientId, response);
                    }
                    
                    // Send our state vector to get their updates
                    byte[] ourStateVector = documentManager.getStateVector(roomName);
                    YWebSocketMessage request = SyncMessageBuilder.createSyncRequest(ourStateVector);
                    sendMessage(clientId, request);
                    break;
                    
                case SYNC_RESPONSE:
                case UPDATE:
                    // Apply the update and broadcast to other clients
                    byte[] update = documentManager.applyUpdate(roomName, syncMsg.getData());
                    YWebSocketMessage broadcastMsg = SyncMessageBuilder.createUpdate(update);
                    broadcastToRoom(roomName, broadcastMsg, clientId);
                    break;
            }
        } catch (Exception e) {
            handler.onError(clientId, e);
        }
    }
    
    private void processAwarenessMessage(String clientId, String roomName, YWebSocketMessage message) {
        if (roomName == null) {
            return; // Client not in a room
        }
        
        try {
            // Update awareness and broadcast to other clients
            documentManager.updateAwareness(clientId, roomName, message.getPayload());
            broadcastToRoom(roomName, message, clientId);
        } catch (Exception e) {
            handler.onError(clientId, e);
        }
    }
    
    private String generateClientId(WebSocket conn) {
        return "client_" + conn.getRemoteSocketAddress().toString() + "_" + System.currentTimeMillis();
    }
}