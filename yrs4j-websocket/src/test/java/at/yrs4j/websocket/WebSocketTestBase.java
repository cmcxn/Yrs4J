package at.yrs4j.websocket;

import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for WebSocket tests
 * Note: Tests that require Yrs4J initialization should handle it individually
 */
public class WebSocketTestBase {
    
    @BeforeAll
    public static void setupTestEnvironment() {
        // Basic test setup - individual tests handle Yrs4J initialization if needed
        System.out.println("WebSocket test environment setup");
    }
}