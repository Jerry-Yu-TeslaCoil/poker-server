package com.game.pokerserver.handler;

import com.game.pokerserver.infrastructure.WebGamePlayer;
import com.game.pokerserver.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import table.CardTable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebPlayerMessageHandler implements WebSocketHandler {

    JwtUtil jwtUtil;

    @Autowired
    public WebPlayerMessageHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private final ConcurrentHashMap<String, WebSocketSession> idToSession = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebGamePlayer, WebSocketSession> playerToSession =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, WebGamePlayer> sessionToPlayer =
            new ConcurrentHashMap<>();
    private final List<WebGamePlayer> registering = new LinkedList<>();

    public synchronized String registerPlayer(WebGamePlayer player) {
        this.registering.add(player);
        log.info("Registering player {}", player);
        return jwtUtil.generateToken(player.playerIdentifier().getPlayerPersonalVO().getInfo().playerId());
    }

    private synchronized void removePlayer(WebGamePlayer player) {
        this.registering.remove(player);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (sessionToPlayer.containsKey(session)) {
            handlePayload(session, message);
        } else {
            registerSession(session, message);
        }
    }

    private void handlePayload(WebSocketSession session, WebSocketMessage<?> message) {
        WebGamePlayer player = sessionToPlayer.get(session);
        log.info("Existed, handling message: {}", message.getPayload());
        player.playerController().getWebMessage(message.getPayload().toString());
    }

    private void registerSession(WebSocketSession session, WebSocketMessage<?> message) {
        String token = message.getPayload().toString();
        String playerId = jwtUtil.getUsernameFromToken(token);
        log.info("Player {} have sent message to applied for connection", playerId);
        WebGamePlayer correspondPlayer = null;
        for (WebGamePlayer player : registering) {
            log.info("Checking player {} for registration", player.playerIdentifier().getPlayerPersonalVO().getInfo().playerId());
            if (player.playerIdentifier().getPlayerPersonalVO().getInfo().playerId().trim().equals(playerId)) {
                correspondPlayer = player;
                playerToSession.put(correspondPlayer, session);
                sessionToPlayer.put(session, correspondPlayer);
                break;
            }
        }
        if (correspondPlayer == null) {
            throw new RuntimeException("No player with id " + playerId + " found");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        WebGamePlayer correspondPlayer = sessionToPlayer.get(session);
        log.info("Connection {} closed", correspondPlayer
                .playerIdentifier().getPlayerPersonalVO().getInfo().playerId());
        sessionToPlayer.remove(session);
        WebSocketSession remove = playerToSession.remove(correspondPlayer);
        remove.close();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}