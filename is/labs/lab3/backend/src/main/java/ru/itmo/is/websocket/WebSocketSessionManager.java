package ru.itmo.is.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.websocket.Session;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class WebSocketSessionManager {
    private final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    public void addSession(Session session) {
        sessions.add(session);
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public void broadcast(WebSocketMessageType type) {
        String message = Json.createObjectBuilder()
                .add("type", type.name())
                .build()
                .toString();

        sessions.removeIf(session -> {
            try {
                if (!session.isOpen()) {
                    return true;
                }

                session.getBasicRemote().sendText(message);
                return false;
            } catch (IOException e) {
                return true;
            }
        });
    }
}
