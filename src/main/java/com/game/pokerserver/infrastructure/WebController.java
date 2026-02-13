package com.game.pokerserver.infrastructure;

import com.game.pokerserver.util.DataJsonUtil;
import control.player.controller.PlayerController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import table.rule.decision.DecisionRequest;
import table.rule.decision.PlayerDecision;
import table.vo.privateinfo.PlayerPrivateVO;
import table.vo.publicinfo.PublicVO;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class WebController implements PlayerController {

    private WebSocketSession session;

    private final CompletableFuture<String> messageFuture = new CompletableFuture<>();

    public WebController() {
    }

    @Override
    public void updatePublicInfo(PublicVO publicVO) {
        try {
            session.sendMessage(new TextMessage(DataJsonUtil.convertToJson(publicVO)));
        } catch (IOException e) {
            log.error("update public info failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePrivateInfo(PlayerPrivateVO playerPrivateVO) {
        try {
            session.sendMessage(new TextMessage(DataJsonUtil.convertToJson(playerPrivateVO)));
        } catch (IOException e) {
            log.error("update private info failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlayerDecision getPlayerDecision(DecisionRequest decisionRequest) {
        String message = messageFuture.join();
        return DataJsonUtil.convertToPlayerDecision(message);
    }

    public void getWebMessage(String message) {
        messageFuture.complete(message);
    }
}
