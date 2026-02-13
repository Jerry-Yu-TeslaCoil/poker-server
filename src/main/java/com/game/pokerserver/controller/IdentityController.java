package com.game.pokerserver.controller;

import com.game.pokerserver.domain.PlayerIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;

@RequestMapping("/identity")
@Controller
@SessionAttributes("userIdentityBuilder")
@Slf4j
public class IdentityController {

    @GetMapping
    public String identify() {
        return "identity";
    }

    @ModelAttribute("userIdentityBuilder")
    public PlayerIdentity.PlayerIdentityBuilder userIdentityBuilder() {
        return new PlayerIdentity.PlayerIdentityBuilder();
    }

    @PostMapping
    public String handleIdentity(@RequestParam String username,
                                 @ModelAttribute("userIdentityBuilder")
                                 PlayerIdentity.PlayerIdentityBuilder userIdentityBuilder) {
        username = username + " " + Calendar.getInstance().getTime().toInstant();
        userIdentityBuilder.setPlayerId(username);
        log.info("user identity:{ {} } applied for connection", userIdentityBuilder.build().playerId());
        return "redirect:/game";
    }
}
