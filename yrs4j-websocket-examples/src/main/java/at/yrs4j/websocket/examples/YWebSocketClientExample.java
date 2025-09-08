package at.yrs4j.websocket.examples;

import at.yrs4j.api.Yrs4J;
import at.yrs4j.libnative.linux.LinuxLibLoader;
import at.yrs4j.libnative.windows.WindowsLibLoader;
import at.yrs4j.wrapper.interfaces.YDoc;
import at.yrs4j.wrapper.interfaces.YText;
import at.yrs4j.wrapper.interfaces.YTransaction;
import at.yrs4j.websocket.client.YWebSocketClient;
import at.yrs4j.websocket.protocol.MessageType;

import java.net.URI;
import java.util.Scanner;

/**
 * Example Y-WebSocket client
 */
public class YWebSocketClientExample {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java YWebSocketClientExample <server-url> [room-name]");
            System.out.println("Example: java YWebSocketClientExample ws://localhost:8080 my-room");
            System.exit(1);
        }
        
        String serverUrl = args[0];
        String roomName = args.length > 1 ? args[1] : "default-room";
        
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
        
        // Create a Y-CRDT document
        YDoc document = YDoc.create();
        YText sharedText = YText.createFromDoc(document, "shared-text");
        
        try {
            // Create WebSocket client
            YWebSocketClient client = new YWebSocketClient(new URI(serverUrl), document, roomName);
            
            // Set up event handlers
            client.setConnectHandler(() -> {
                System.out.println("Connected to server: " + serverUrl);
                System.out.println("Joined room: " + roomName);
                System.out.println("Type messages to add to the shared document, or 'quit' to exit:");
            });
            
            client.setDisconnectHandler(() -> {
                System.out.println("Disconnected from server");
            });
            
            client.setErrorHandler(error -> {
                System.err.println("WebSocket error: " + error.getMessage());
            });
            
            client.setMessageHandler(message -> {
                if (message.getType() == MessageType.SYNC) {
                    // Document was updated, show current content
                    YTransaction readTxn = document.readTransaction();
                    String content = sharedText.string(readTxn);
                    System.out.println("\n[Document updated] Current content: \"" + content + "\"");
                    System.out.print("> ");
                }
            });
            
            // Connect to server
            client.connect();
            
            // Wait for initial sync
            client.waitForSync().join();
            
            // Show initial content
            YTransaction readTxn = document.readTransaction();
            String initialContent = sharedText.string(readTxn);
            System.out.println("Initial document content: \"" + initialContent + "\"");
            
            // Interactive loop
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                
                if ("quit".equalsIgnoreCase(input)) {
                    break;
                }
                
                if (!input.isEmpty()) {
                    // Add text to the document
                    YTransaction txn = document.writeTransaction();
                    int currentLen = sharedText.len(txn);
                    
                    // Add a space if document is not empty
                    String textToAdd = currentLen > 0 ? " " + input : input;
                    sharedText.insert(txn, currentLen, textToAdd, null);
                    
                    // Get the update and send it
                    byte[] stateVector = txn.stateVectorV1();
                    byte[] update = txn.stateDiffV1(new byte[0]);
                    txn.commit();
                    
                    if (update.length > 0) {
                        client.sendUpdate(update);
                    }
                    
                    // Show updated content
                    YTransaction newReadTxn = document.readTransaction();
                    String newContent = sharedText.string(newReadTxn);
                    System.out.println("Updated content: \"" + newContent + "\"");
                }
            }
            
            client.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            document.destroy();
            Yrs4J.cleanup();
        }
    }
}