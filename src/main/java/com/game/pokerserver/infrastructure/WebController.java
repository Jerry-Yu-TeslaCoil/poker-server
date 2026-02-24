package com.game.pokerserver.infrastructure;

import com.game.pokerserver.util.DataJsonUtil;
import control.player.controller.PlayerController;
import exception.PlayerLeftException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import table.rule.decision.DecisionRequest;
import table.rule.decision.PlayerDecision;
import table.rule.decision.impl.FoldDecision;
import table.vo.privateinfo.PlayerPrivateVO;
import table.vo.publicinfo.PublicVO;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class WebController implements PlayerController {

    @Setter
    private WebSocketSession session;

    private CompletableFuture<String> messageFuture = new CompletableFuture<>();

    private boolean switchToRobot = false;
    private boolean throwException = false;

    private final long timeout;
    private final PlayerDecision defaultDecision;

    public WebController(long timeout, PlayerDecision defaultDecision) {
        this.timeout = timeout;
        this.defaultDecision = defaultDecision;
    }

    public void getWebMessage(String message) {
        messageFuture.complete(message);
    }

    public void sessionClosed() {
        this.switchToRobot = true;
        this.throwException = true;
        getWebMessage(DataJsonUtil.FOLD_DECISION_JSON);
    }

    @Override
    public void updatePublicInfo(PublicVO publicVO) {
        if (switchToRobot) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(DataJsonUtil.convertToJson(publicVO)));
        } catch (IOException e) {
            log.error("update public info failed", e);
        }
    }

    @Override
    public void updatePrivateInfo(PlayerPrivateVO playerPrivateVO) {
        if (switchToRobot) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(DataJsonUtil.convertToJson(playerPrivateVO)));
        } catch (IOException e) {
            log.error("update private info failed", e);
        }
    }

    @Override
    public PlayerDecision getPlayerDecision(DecisionRequest decisionRequest) throws PlayerLeftException {
        if (throwException) {
            throw new PlayerLeftException("Player session closed");
        }
        if (switchToRobot) {
            return defaultDecision;
        }
        try {
            session.sendMessage(new TextMessage(DataJsonUtil.convertToJson(decisionRequest)));

            try {
                String message = messageFuture.orTimeout(timeout, TimeUnit.MILLISECONDS)
                        .join();
                messageFuture = new CompletableFuture<>();
                return DataJsonUtil.convertToPlayerDecision(message);
            } catch (CompletionException e) {
                if (e.getCause() instanceof TimeoutException) {
                    log.warn("Decision request timed out after {} ms, using default decision", timeout);
                    return defaultDecision;
                } else {
                    throw e;
                }
            }

        } catch (IOException e) {
            log.error("get decision failed", e);
            this.switchToRobot = true;
            return new FoldDecision();
        }
    }

    @Override
    public String toString() {
        return "WebController";
    }
}
