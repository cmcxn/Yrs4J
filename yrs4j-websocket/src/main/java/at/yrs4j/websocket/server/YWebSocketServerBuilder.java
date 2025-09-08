package at.yrs4j.websocket.server;

import at.yrs4j.websocket.YDocumentManager;
import at.yrs4j.websocket.YWebSocketHandler;

import java.net.InetSocketAddress;

/**
 * Builder for Y-WebSocket server
 */
public class YWebSocketServerBuilder {
    private int port = 8080;
    private String host = "localhost";
    private YDocumentManager documentManager;
    private YWebSocketHandler handler;
    
    public static YWebSocketServerBuilder create() {
        return new YWebSocketServerBuilder();
    }
    
    public YWebSocketServerBuilder port(int port) {
        this.port = port;
        return this;
    }
    
    public YWebSocketServerBuilder host(String host) {
        this.host = host;
        return this;
    }
    
    public YWebSocketServerBuilder documentManager(YDocumentManager documentManager) {
        this.documentManager = documentManager;
        return this;
    }
    
    public YWebSocketServerBuilder handler(YWebSocketHandler handler) {
        this.handler = handler;
        return this;
    }
    
    public YWebSocketServerImpl build() {
        if (documentManager == null) {
            documentManager = new YDocumentManager();
        }
        
        if (handler == null) {
            handler = new DefaultYWebSocketHandler();
        }
        
        InetSocketAddress address = new InetSocketAddress(host, port);
        return new YWebSocketServerImpl(address, documentManager, handler);
    }
    
    /**
     * Default handler implementation
     */
    private static class DefaultYWebSocketHandler implements YWebSocketHandler {
        @Override
        public void onConnect(String clientId) {
            System.out.println("Client connected: " + clientId);
        }
        
        @Override
        public void onDisconnect(String clientId) {
            System.out.println("Client disconnected: " + clientId);
        }
        
        @Override
        public void onMessage(String clientId, at.yrs4j.websocket.protocol.YWebSocketMessage message) {
            System.out.println("Message from " + clientId + ": " + message);
        }
        
        @Override
        public void onError(String clientId, Throwable error) {
            System.err.println("Error for client " + clientId + ": " + error.getMessage());
            error.printStackTrace();
        }
    }
}