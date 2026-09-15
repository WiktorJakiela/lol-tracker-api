package com.wiktorjakiela.lol_tracker_api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
    @GetMapping("/api/players/by-riot-id")
    public PlayerResponse getPlayerByRiotId(
        @RequestParam String gameName,
        @RequestParam String tagLine,
        @RequestParam String region
    ){
        return playerService.getPlayerByRiotId(gameName, tagLine, region);
    }
    
}
