package com.game.pokerserver.infrastructure;

import control.player.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class WebGamePlayer implements GamePlayer {

    private WebController controller;
    private WebIdentifier playerIdentifier;

    @Override
    public WebController playerController() {
        return controller;
    }

    @Override
    public WebIdentifier playerIdentifier() {
        return playerIdentifier;
    }
}
