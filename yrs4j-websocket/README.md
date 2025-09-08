# Yrs4J WebSocket Module

This module provides WebSocket-based synchronization for Yrs4J, implementing the y-websocket protocol for real-time collaborative editing.

## Features

- **Y-WebSocket Protocol**: Full implementation of the y-websocket protocol for compatibility with other Y implementations
- **Server & Client Support**: Both WebSocket server and client implementations
- **Document Management**: Automatic Y-CRDT document management and synchronization
- **Room-based Collaboration**: Support for multiple rooms with isolated document spaces
- **Awareness Protocol**: Real-time awareness information sharing between clients
- **Comprehensive Testing**: Full test suite with protocol, integration, and unit tests

## Components

### Core Classes

- `YWebSocketServerImpl`: WebSocket server for hosting collaborative sessions
- `YWebSocketClient`: WebSocket client for connecting to collaborative sessions
- `YDocumentManager`: Manages Y-CRDT documents and client sessions
- `YWebSocketServerBuilder`: Builder pattern for easy server configuration

### Protocol Support

- `YWebSocketMessage`: Core message structure for y-websocket protocol
- `MessageType`: Enum for different message types (SYNC, AWARENESS, AUTH, etc.)
- `SyncMessageBuilder`: Helper for creating sync protocol messages
- `SyncMessageType`: Enum for sync message subtypes (REQUEST, RESPONSE, UPDATE)

## Quick Start

### Server Setup

```java
import at.yrs4j.api.Yrs4J;
import at.yrs4j.libnative.linux.LinuxLibLoader;
import at.yrs4j.websocket.server.YWebSocketServerBuilder;

// Initialize Yrs4J
Yrs4J.init(LinuxLibLoader.create());

// Create and start server
YWebSocketServerImpl server = YWebSocketServerBuilder.create()
    .port(8080)
    .host("localhost")
    .build();

server.start();
System.out.println("Server running on ws://localhost:8080");
```

### Client Connection

```java
import at.yrs4j.wrapper.interfaces.YDoc;
import at.yrs4j.websocket.client.YWebSocketClient;

// Create document and client
YDoc document = YDoc.create();
YWebSocketClient client = new YWebSocketClient(
    new URI("ws://localhost:8080"), 
    document, 
    "my-room"
);

// Set up handlers
client.setConnectHandler(() -> System.out.println("Connected!"));
client.setMessageHandler(message -> {
    // Handle received messages
});

// Connect
client.connect();
```

## Message Protocol

The module implements the standard y-websocket protocol:

### Message Types
- `SYNC (0)`: Document synchronization messages
- `AWARENESS (1)`: Client awareness information
- `AUTH (2)`: Authentication (future extension)
- `QUERY_AWARENESS (3)`: Request awareness info
- `ROOM_LIST (4)`: Room management

### Sync Message Types
- `SYNC_REQUEST (0)`: Request updates with state vector
- `SYNC_RESPONSE (1)`: Response with document updates
- `UPDATE (2)`: Real-time document updates

## Examples

Complete working examples are available in the `yrs4j-websocket-examples` module:

- `YWebSocketServerExample`: Standalone server with basic document setup
- `YWebSocketClientExample`: Interactive client for testing collaboration

Run the server:
```bash
./gradlew :yrs4j-websocket-examples:run
```

Run a client:
```bash
./gradlew :yrs4j-websocket-examples:runClient
```

## Testing

The module includes comprehensive tests:

```bash
# Run all WebSocket tests
./gradlew :yrs4j-websocket:test

# Run specific test categories
./gradlew :yrs4j-websocket:test --tests "*ProtocolTest"
./gradlew :yrs4j-websocket:test --tests "*DocumentManagerTest"
```

### Test Coverage
- **Protocol Tests**: Message encoding/decoding, protocol compliance
- **Document Manager Tests**: Room management, awareness handling
- **Integration Tests**: End-to-end server/client communication (requires native libraries)

## Architecture

```
┌─────────────────┐    WebSocket     ┌─────────────────┐
│   YWebSocketClient  │◄──────────────►│ YWebSocketServer │
└─────────────────┘                  └─────────────────┘
         │                                     │
         ▼                                     ▼
┌─────────────────┐                  ┌─────────────────┐
│     YDoc        │                  │ YDocumentManager │
│   (Y-CRDT)      │                  │   (Multi-room)   │
└─────────────────┘                  └─────────────────┘
```

## Dependencies

- **yrs4j-bindings**: Core Y-CRDT functionality
- **Java-WebSocket**: WebSocket client/server implementation  
- **Gson**: JSON serialization for message handling

## Compatibility

This implementation follows the standard y-websocket protocol and is compatible with:
- [y-websocket (JavaScript)](https://github.com/yjs/y-websocket)
- [y-websocket (Python)](https://github.com/y-crdt/ypy-websocket)
- Other Y-CRDT WebSocket implementations

## Future Enhancements

- Authentication and authorization
- Room-based permissions
- Persistence backend integration
- Metrics and monitoring
- SSL/TLS support configuration