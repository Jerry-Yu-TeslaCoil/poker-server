package com.game.pokerserver.domain;

import lombok.Setter;
import lombok.ToString;

public record PlayerIdentity(String playerId) {
    @Setter
    @ToString
    public static class PlayerIdentityBuilder {
        private String playerId;

        public PlayerIdentity build() {
            return new PlayerIdentity(playerId);
        }

    }
}
