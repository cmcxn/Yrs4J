package at.yrs4j.websocket.examples;

import at.yrs4j.api.Yrs4J;
import at.yrs4j.libnative.linux.LinuxLibLoader;
import at.yrs4j.libnative.windows.WindowsLibLoader;
import at.yrs4j.wrapper.interfaces.YDoc;
import at.yrs4j.wrapper.interfaces.YText;
import at.yrs4j.wrapper.interfaces.YTransaction;
import at.yrs4j.websocket.server.YWebSocketServerBuilder;
import at.yrs4j.websocket.server.YWebSocketServerImpl;

/**
 * Example Y-WebSocket server
 */
public class YWebSocketServerExample {
    
    public static void main(String[] args) {
        // Initialize Yrs4J
        try {
            try {
                Yrs4J.init(LinuxLibLoader.create());
                System.out.println("Initialized with Linux native library");
            } catch (Exception e) {
                Yrs4J.init(WindowsLibLoader.create());
                System.out.println("Initialized with Windows native library");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize Yrs4J: " + e.getMessage());
            System.exit(1);
        }
        
        // Create and start the WebSocket server
        YWebSocketServerImpl server = YWebSocketServerBuilder.create()
                .port(8080)
                .host("localhost")
                .build();
        
        System.out.println("Starting Y-WebSocket server on localhost:8080");
        server.start();
        
        // Create a sample document and add some content
        YDoc doc = YDoc.create();
        YText text = YText.createFromDoc(doc, "demo-text");
        YTransaction txn = doc.writeTransaction();
        text.insert(txn, 0, "Welcome to Y-WebSocket for Java!", null);
        txn.commit();
        
        System.out.println("Sample document created with content: " + text.string(doc.readTransaction()));
        System.out.println("Server is running. Connect clients to ws://localhost:8080");
        System.out.println("Press Ctrl+C to stop the server.");
        
        // Keep the server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            try {
                server.stop();
                doc.destroy();
                Yrs4J.cleanup();
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}