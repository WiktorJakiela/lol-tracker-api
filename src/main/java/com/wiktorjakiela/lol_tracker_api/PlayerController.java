package com.wiktorjakiela.lol_tracker_api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController 
public class PlayerController {
    private final PlayerService playerService;
    
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    @GetMapping("/api/players/demo")
    public PlayerResponse getDemoPlayer() {
        return playerService.getDemoPlayer();
    }
    
}
