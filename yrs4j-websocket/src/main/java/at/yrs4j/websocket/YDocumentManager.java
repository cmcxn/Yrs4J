package at.yrs4j.websocket;

import at.yrs4j.wrapper.interfaces.YDoc;
import at.yrs4j.wrapper.interfaces.YTransaction;
import at.yrs4j.websocket.protocol.YWebSocketMessage;
import at.yrs4j.websocket.protocol.SyncMessageBuilder;
import at.yrs4j.websocket.protocol.MessageType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Y-CRDT document synchronization for WebSocket connections
 */
public class YDocumentManager {
    private final Map<String, YDoc> documents = new ConcurrentHashMap<>();
    private final Map<String, String> clientToRoom = new ConcurrentHashMap<>();
    private final Map<String, Map<String, byte[]>> roomAwareness = new ConcurrentHashMap<>();
    
    /**
     * Get or create a document for a room
     */
    public YDoc getOrCreateDocument(String roomName) {
        return documents.computeIfAbsent(roomName, k -> YDoc.create());
    }
    
    /**
     * Join a client to a room
     */
    public void joinRoom(String clientId, String roomName) {
        clientToRoom.put(clientId, roomName);
        roomAwareness.computeIfAbsent(roomName, k -> new ConcurrentHashMap<>());
    }
    
    /**
     * Remove a client from their room
     */
    public void leaveRoom(String clientId) {
        String roomName = clientToRoom.remove(clientId);
        if (roomName != null) {
            Map<String, byte[]> awareness = roomAwareness.get(roomName);
            if (awareness != null) {
                awareness.remove(clientId);
            }
        }
    }
    
    /**
     * Get the room name for a client
     */
    public String getClientRoom(String clientId) {
        return clientToRoom.get(clientId);
    }
    
    /**
     * Apply an update to a document and return the update for broadcasting
     */
    public byte[] applyUpdate(String roomName, byte[] update) {
        YDoc doc = getOrCreateDocument(roomName);
        YTransaction txn = doc.writeTransaction();
        try {
            int result = txn.apply(update);
            if (result != 0) {
                throw new RuntimeException("Failed to apply update: " + result);
            }
            txn.commit();
            return update; // Return the same update for broadcasting
        } catch (Exception e) {
            throw new RuntimeException("Error applying update", e);
        }
    }
    
    /**
     * Get state vector for a document
     */
    public byte[] getStateVector(String roomName) {
        YDoc doc = getOrCreateDocument(roomName);
        YTransaction txn = doc.readTransaction();
        try {
            return txn.stateVectorV1();
        } catch (Exception e) {
            throw new RuntimeException("Error getting state vector", e);
        }
    }
    
    /**
     * Get state diff for synchronization
     */
    public byte[] getStateDiff(String roomName, byte[] stateVector) {
        YDoc doc = getOrCreateDocument(roomName);
        YTransaction txn = doc.readTransaction();
        try {
            return txn.stateDiffV1(stateVector);
        } catch (Exception e) {
            throw new RuntimeException("Error getting state diff", e);
        }
    }
    
    /**
     * Update awareness information for a client
     */
    public void updateAwareness(String clientId, String roomName, byte[] awarenessUpdate) {
        Map<String, byte[]> awareness = roomAwareness.computeIfAbsent(roomName, k -> new ConcurrentHashMap<>());
        awareness.put(clientId, awarenessUpdate);
    }
    
    /**
     * Get all awareness updates for a room
     */
    public Map<String, byte[]> getRoomAwareness(String roomName) {
        return roomAwareness.getOrDefault(roomName, new ConcurrentHashMap<>());
    }
    
    /**
     * Get all clients in a room
     */
    public Map<String, String> getClientsInRoom(String roomName) {
        Map<String, String> clients = new ConcurrentHashMap<>();
        clientToRoom.entrySet().stream()
                .filter(entry -> roomName.equals(entry.getValue()))
                .forEach(entry -> clients.put(entry.getKey(), entry.getValue()));
        return clients;
    }
    
    /**
     * Cleanup all resources
     */
    public void cleanup() {
        documents.values().forEach(doc -> {
            try {
                doc.destroy();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        });
        documents.clear();
        clientToRoom.clear();
        roomAwareness.clear();
    }
}