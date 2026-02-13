package com.game.pokerserver.handler;

import com.game.pokerserver.infrastructure.WebGamePlayer;
import com.game.pokerserver.service.GamingService;
import com.game.pokerserver.util.DataJsonUtil;
import com.game.pokerserver.util.JwtUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebPlayerMessageHandler implements WebSocketHandler {

    private final JwtUtil jwtUtil;

    private final GamingService gamingService;

    @Autowired
    public WebPlayerMessageHandler(JwtUtil jwtUtil, GamingService gamingService) {
        this.jwtUtil = jwtUtil;
        this.gamingService = gamingService;
    }

    private final ConcurrentHashMap<String, WebSocketSession> idToSession = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, WebGamePlayer> sessionToPlayer =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebGamePlayer> registeringMap = new ConcurrentHashMap<>();

    public synchronized String registerPlayer(WebGamePlayer player) {
        this.registeringMap.put(player.playerIdentifier().getPlayerPersonalVO().getInfo().playerId(), player);
        log.info("Registering player {}", player);
        return jwtUtil.generateToken(player.playerIdentifier().getPlayerPersonalVO().getInfo().playerId());
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
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

    private void registerSession(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        String token = message.getPayload().toString();
        String playerId = jwtUtil.getUsernameFromToken(token);
        log.info("Player {} have sent message to applied for connection", playerId);
        WebSocketSession playerSession = idToSession.getOrDefault(playerId, null);
        if (playerSession != null) {
            log.warn("Player {} already registered", playerId);
            session.sendMessage(new TextMessage(DataJsonUtil.convertToJson("WARNING", "玩家已经连接")));
            session.close();
        }
        WebGamePlayer correspondPlayer = registeringMap.get(playerId);
        if (correspondPlayer == null) {
            throw new RuntimeException("No player with id " + playerId + " found");
        } else {
            correspondPlayer.playerController().setSession(session);
            idToSession.put(playerId, session);
            sessionToPlayer.put(session, correspondPlayer);
        }
        int playerPos = gamingService.play(correspondPlayer);
        session.sendMessage(new TextMessage(
                DataJsonUtil.convertToJson("PLAYER_POS",  playerPos)));
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        WebGamePlayer correspondPlayer = sessionToPlayer.get(session);
        if (correspondPlayer != null) {
            //TODO: Call game service to shutdown player or give a default action.
            log.info("Connection {} closed", correspondPlayer
                    .playerIdentifier().getPlayerPersonalVO().getInfo().playerId());
            idToSession.remove(correspondPlayer.playerIdentifier().getPlayerPersonalVO().getInfo().playerId());
            sessionToPlayer.remove(session);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}