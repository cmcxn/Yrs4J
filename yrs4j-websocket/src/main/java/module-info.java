module yrs4j.websocket {
    requires yrs4j.bindings;
    requires org.java_websocket;
    requires com.google.gson;
    
    exports at.yrs4j.websocket;
    exports at.yrs4j.websocket.server;
    exports at.yrs4j.websocket.client;
    exports at.yrs4j.websocket.protocol;
}