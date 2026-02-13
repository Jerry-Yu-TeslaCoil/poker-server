package com.game.pokerserver;

import com.game.pokerserver.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebMvcTest(JwtUtil.class)
@Slf4j
public class JwtUtilTest {

    @Autowired
    JwtUtil jwtUtil;

    @Test
    public void generateToken() {
        String s = jwtUtil.generateToken("Jerry");
        log.info("{}", s);
    }

    @Test
    public void generateTokenWithClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "Jerry");
        String s = jwtUtil.generateTokenWithClaims(claims, "Jerry");
        log.info("{}", s);
    }

    @Test
    public void getUsernameFromToken() {
        String s = jwtUtil.generateToken("Jerry");
        log.info("{}", s);
        String usernameFromToken = jwtUtil.getUsernameFromToken(s);
        log.info("{}", usernameFromToken);
        assert usernameFromToken.equals("Jerry");
    }

    @Test
    public void validateToken() {
        String s = jwtUtil.generateToken("Jerry");
        log.info("{}", s);
        boolean result = jwtUtil.validateToken(s, "Jerry");
        log.info("{}", result);
        assert result;
        boolean result2 = jwtUtil.validateToken(s, "Tom");
        log.info("{}", result2);
        assert !result2;
    }

    @Test
    public void refreshToken() throws InterruptedException {
        String s = jwtUtil.generateToken("Jerry");
        log.info("{}", s);
        Date expirationDateFromToken = jwtUtil.getExpirationDateFromToken(s);
        log.info("{}", expirationDateFromToken);
        Thread.sleep(2000);
        String refreshedToken = jwtUtil.refreshToken(s);
        log.info("{}", refreshedToken);
        Date expirationDateFromToken2 = jwtUtil.getExpirationDateFromToken(refreshedToken);
        log.info("{}", expirationDateFromToken2);
        assert expirationDateFromToken.before(expirationDateFromToken2);
    }
}
