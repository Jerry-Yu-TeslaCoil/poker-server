package com.game.pokerserver.infrastructure;

import com.game.pokerserver.domain.PlayerIdentity;
import control.player.identifier.PlayerIdentifier;
import control.vo.PlayerPersonalVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class WebIdentifier implements PlayerIdentifier<PlayerIdentity> {
    private PlayerIdentity userId;

    @Override
    public PlayerPersonalVO<PlayerIdentity> getPlayerPersonalVO() {
        return new PlayerPersonalVO<>(userId);
    }
}
