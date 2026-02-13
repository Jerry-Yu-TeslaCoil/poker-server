package com.game.pokerserver.controller;

import com.game.pokerserver.domain.PlayerIdentity;
import com.game.pokerserver.handler.WebPlayerMessageHandler;
import com.game.pokerserver.infrastructure.WebGamePlayer;
import com.game.pokerserver.infrastructure.WebIdentifier;
import com.game.pokerserver.infrastructure.WebController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.Objects;

@Slf4j
@Controller
@SessionAttributes({"userIdentityBuilder", "gamePlayer", "jwtToken"})
@RequestMapping("/game")
public class GameController {

    private final WebPlayerMessageHandler messageHandler;

    @Autowired
    public GameController(WebPlayerMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @ModelAttribute("gamePlayer")
    public WebGamePlayer createGamePlayer(
            @ModelAttribute("userIdentityBuilder") PlayerIdentity.PlayerIdentityBuilder userIdentityBuilder) {
        /*
          About synchronizing messageHandler and gamePlayer:
          - Currently we have playerIdentity, which is a temporary identification from client;
          - Generate JWT Token using playerId;
          - Send gamePlayer with playerId to messageHandler as register;
          - Send JWT Token to web client page;
          - client side applies for a websocket connection,
          and use JWT Token to identify oneself as a corresponding gamePlayer;
          - messageHandler parse playerId from token, find gamePlayer of same playerId;
          - set <gamePlayer, session> pair to map;
          - connection established.
         */
        WebController controller = new WebController();
        return new WebGamePlayer(controller, new WebIdentifier(userIdentityBuilder.build()));
    }

    @GetMapping
    public String game(@ModelAttribute("gamePlayer") WebGamePlayer gamePlayer, Model model) {
        log.info("Handling game player {}", gamePlayer);
        String jwt = messageHandler.registerPlayer(gamePlayer);
        model.addAttribute("jwtToken", jwt);
        return "game";
    }
}
