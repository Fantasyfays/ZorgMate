package com.example.zorgmate.websocket;

import com.example.zorgmate.security.JwtUtil;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InvoiceWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final JwtUtil jwtUtil;

    public InvoiceWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        String token = null;

        if (query != null && query.startsWith("token=")) {
            token = query.substring("token=".length());

            try {
                if (!jwtUtil.validateToken(token)) {
                    System.out.println("Ongeldig token ontvangen via WebSocket.");
                    session.close();
                    return;
                }

                String username = jwtUtil.extractUsername(token);
                System.out.println("WebSocket verbinding geauthenticeerd voor gebruiker: " + username);
                userSessions.put(username, session);

            } catch (Exception e) {
                System.out.println("Token validatie of extractie mislukt: " + e.getMessage());
                session.close();
                return;
            }

        } else {
            System.out.println("Geen token gevonden in WebSocket request.");
            session.close();
            return;
        }

        System.out.println("WebSocket verbonden: " + session.getId());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        session.sendMessage(new TextMessage("Ontvangen: " + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        userSessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
        System.out.println("Verbinding gesloten: " + session.getId());
    }

    public void sendToUser(String creatorUsername, String receiverEmail, String message) {
        sendToSingleUser(creatorUsername, message);
        if (receiverEmail != null && !receiverEmail.equals(creatorUsername)) {
            sendToSingleUser(receiverEmail, message);
        }
    }

    private void sendToSingleUser(String username, String message) {
        WebSocketSession session = userSessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                System.err.println("Fout bij verzenden naar " + username + ": " + e.getMessage());
            }
        }
    }
}
