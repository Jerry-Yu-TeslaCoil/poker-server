package com.game.pokerserver.service;

import com.game.pokerserver.infrastructure.WebGamePlayer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import table.CardTable;
import table.impl.ClassicTable;

@Slf4j
@Service
public class GamingService {

    private int playerCount = 0;

    private CardTable cardTable = new ClassicTable();

    private final Object lock = new Object();

    public int play(WebGamePlayer player) {
        int playerId = playerCount + 1;
        synchronized (lock) {
            playerCount++;
            cardTable.playerJoin(player);
            log.info("Player {} joined the game", player);

            final int MAX_COUNT = 2;
            if (playerCount == MAX_COUNT) {
                playerCount = 0;
                final CardTable currentTable = cardTable;
                new Thread(currentTable::startRounds).start();
                cardTable = new ClassicTable();
            }
        }
        return playerId;
    }
}