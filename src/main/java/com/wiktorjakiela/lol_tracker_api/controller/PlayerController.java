package com.wiktorjakiela.lol_tracker_api.controller;

import org.springframework.web.bind.annotation.RestController;

import com.wiktorjakiela.lol_tracker_api.response.PlayerResponse;
import com.wiktorjakiela.lol_tracker_api.service.PlayerService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Odbiera requesty dotyczące graczy, odczytuje ich parametry
 * i przekazuje obsługę do PlayerService.
 */
@RestController 
public class PlayerController {
    private final PlayerService playerService;
    
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/api/players/by-riot-id")
    public PlayerResponse getPlayerByRiotId(
        @RequestParam String gameName,
        @RequestParam String tagLine,
        @RequestParam String region
    ){
        return playerService.getPlayerByRiotId(gameName, tagLine, region);
    }
    
}
