package com.game.pokerserver.infrastructure;

import control.player.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
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
