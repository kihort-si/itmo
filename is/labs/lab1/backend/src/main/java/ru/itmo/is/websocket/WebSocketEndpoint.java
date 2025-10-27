package ru.itmo.is.websocket;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class WebSocketEndpoint {
    private WebSocketSessionManager sessionManager;

    @OnOpen
    public void onOpen(Session session) {
        getSessionManager().addSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        getSessionManager().removeSession(session);
    }

    private WebSocketSessionManager getSessionManager() {
        if (sessionManager == null) {
            sessionManager = CDI.current().select(WebSocketSessionManager.class).get();
        }
        return sessionManager;
    }
}