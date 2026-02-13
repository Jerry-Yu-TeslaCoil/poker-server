package com.game.pokerserver.controller;

import com.game.pokerserver.domain.PlayerIdentity;
import com.game.pokerserver.infrastructure.WebController;
import com.game.pokerserver.infrastructure.WebGamePlayer;
import com.game.pokerserver.infrastructure.WebIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;

@RequestMapping("/identity")
@Controller
@SessionAttributes("gamePlayer")
@Slf4j
public class IdentityController {

    @GetMapping
    public String identify() {
        return "identity";
    }

    @ModelAttribute("gamePlayer")
    public WebGamePlayer userIdentityBuilder() {
        return new WebGamePlayer();
    }

    @PostMapping
    public String handleIdentity(@RequestParam String username,
                                 @ModelAttribute("gamePlayer")
                                 WebGamePlayer gamePlayer) {
        username = username + " " + Calendar.getInstance().getTime().toInstant();
        gamePlayer.setController(new WebController());
        gamePlayer.setPlayerIdentifier(new WebIdentifier(new PlayerIdentity(username)));
        log.info("user identity:{ {} } applied for connection", username);
        return "redirect:/game";
    }
}
