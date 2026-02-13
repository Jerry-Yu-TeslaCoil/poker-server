package com.game.pokerserver;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Random;

@Slf4j
public class TokenRandomizerTest {

    @Test
    public void randomizeToken() {
        Random rand = new Random();
        StringBuilder builder = new StringBuilder();
        rand.setSeed(System.currentTimeMillis());
        for (int i = 0; i < 32; i++) {
            int token = rand.nextInt(256);
            builder.append(" ").append(token);
        }
        log.info("{}", builder);
    }
}
