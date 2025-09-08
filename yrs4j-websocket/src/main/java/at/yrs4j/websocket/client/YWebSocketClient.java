package at.yrs4j.websocket.client;

import at.yrs4j.wrapper.interfaces.YDoc;
import at.yrs4j.wrapper.interfaces.YTransaction;
import at.yrs4j.websocket.protocol.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Y-WebSocket client implementation
 */
public class YWebSocketClient extends WebSocketClient {
    private final YDoc document;
    private final String roomName;
    private Consumer<YWebSocketMessage> messageHandler;
    private Consumer<Exception> errorHandler;
    private Runnable connectHandler;
    private Runnable disconnectHandler;
    private CompletableFuture<Void> syncComplete = new CompletableFuture<>();
    
    public YWebSocketClient(URI serverURI, YDoc document, String roomName) {
        super(serverURI);
        this.document = document;
        this.roomName = roomName;
    }
    
    @Override
    public void onOpen(ServerHandshake handshake) {
        // Request to join room and start initial sync
        byte[] stateVector = getStateVector();
        YWebSocketMessage syncRequest = SyncMessageBuilder.createSyncRequest(stateVector);
        send(syncRequest.encode());
        
        if (connectHandler != null) {
            connectHandler.run();
        }
    }
    
    @Override
    public void onMessage(String message) {
        // Text messages are not supported in y-websocket protocol
        if (errorHandler != null) {
            errorHandler.accept(new UnsupportedOperationException("Text messages not supported"));
        }
    }
    
    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            byte[] data = new byte[bytes.remaining()];
            bytes.get(data);
            
            YWebSocketMessage message = YWebSocketMessage.decode(data);
            processMessage(message);
            
            if (messageHandler != null) {
                messageHandler.accept(message);
            }
            
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(e);
            }
        }
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (disconnectHandler != null) {
            disconnectHandler.run();
        }
    }
    
    @Override
    public void onError(Exception ex) {
        if (errorHandler != null) {
            errorHandler.accept(ex);
        }
    }
    
    /**
     * Send a Y-CRDT update to the server
     */
    public void sendUpdate(byte[] update) {
        YWebSocketMessage message = SyncMessageBuilder.createUpdate(update);
        send(message.encode());
    }
    
    /**
     * Send awareness information to the server
     */
    public void sendAwareness(byte[] awarenessUpdate) {
        YWebSocketMessage message = SyncMessageBuilder.createAwareness(awarenessUpdate);
        send(message.encode());
    }
    
    /**
     * Set message handler
     */
    public void setMessageHandler(Consumer<YWebSocketMessage> handler) {
        this.messageHandler = handler;
    }
    
    /**
     * Set error handler
     */
    public void setErrorHandler(Consumer<Exception> handler) {
        this.errorHandler = handler;
    }
    
    /**
     * Set connect handler
     */
    public void setConnectHandler(Runnable handler) {
        this.connectHandler = handler;
    }
    
    /**
     * Set disconnect handler
     */
    public void setDisconnectHandler(Runnable handler) {
        this.disconnectHandler = handler;
    }
    
    /**
     * Wait for initial sync to complete
     */
    public CompletableFuture<Void> waitForSync() {
        return syncComplete;
    }
    
    /**
     * Get the room name this client is connected to
     */
    public String getRoomName() {
        return roomName;
    }
    
    /**
     * Get the document this client is synchronizing
     */
    public YDoc getDocument() {
        return document;
    }
    
    private void processMessage(YWebSocketMessage message) {
        switch (message.getType()) {
            case SYNC:
                processSyncMessage(message);
                break;
            case AWARENESS:
                // Awareness messages are handled by the message handler
                break;
            default:
                // Ignore other message types
                break;
        }
    }
    
    private void processSyncMessage(YWebSocketMessage message) {
        try {
            SyncMessageBuilder.SyncMessage syncMsg = SyncMessageBuilder.parseSyncMessage(message.getPayload());
            
            switch (syncMsg.getType()) {
                case SYNC_REQUEST:
                    // Send our state diff to the server
                    byte[] stateDiff = getStateDiff(syncMsg.getData());
                    if (stateDiff.length > 0) {
                        YWebSocketMessage response = SyncMessageBuilder.createSyncResponse(stateDiff);
                        send(response.encode());
                    }
                    break;
                    
                case SYNC_RESPONSE:
                case UPDATE:
                    // Apply the update to our document
                    applyUpdate(syncMsg.getData());
                    if (!syncComplete.isDone()) {
                        syncComplete.complete(null);
                    }
                    break;
            }
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(e);
            }
        }
    }
    
    private byte[] getStateVector() {
        YTransaction txn = document.readTransaction();
        try {
            return txn.stateVectorV1();
        } catch (Exception e) {
            throw new RuntimeException("Error getting state vector", e);
        }
    }
    
    private byte[] getStateDiff(byte[] stateVector) {
        YTransaction txn = document.readTransaction();
        try {
            return txn.stateDiffV1(stateVector);
        } catch (Exception e) {
            throw new RuntimeException("Error getting state diff", e);
        }
    }
    
    private void applyUpdate(byte[] update) {
        YTransaction txn = document.writeTransaction();
        try {
            int result = txn.apply(update);
            if (result != 0) {
                throw new RuntimeException("Failed to apply update: " + result);
            }
            txn.commit();
        } catch (Exception e) {
            throw new RuntimeException("Error applying update", e);
        }
    }
}