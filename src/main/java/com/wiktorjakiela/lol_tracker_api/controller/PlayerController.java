package com.wiktorjakiela.lol_tracker_api.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wiktorjakiela.lol_tracker_api.response.PlayerResponse;
import com.wiktorjakiela.lol_tracker_api.service.PlayerService;
import com.wiktorjakiela.lol_tracker_api.response.MatchDetailResponse;

import java.util.List;



/**
 * Klasa odbierająca requesty dotyczące graczy, odczytuje ich parametry i przekazuje obsługę do PlayerService.
 */
@RestController
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    // Zwraca wszystkie dane o graczu
    @GetMapping("/api/players/by-riot-id")
    public PlayerResponse getPlayerByRiotId(
        @RequestParam String gameName,
        @RequestParam String tagLine,
        @RequestParam String region
    ){
        return playerService.getPlayerByRiotId(gameName, tagLine, region);
    }

    // Zwraca stronę historii gracza, domyślnie pięć ostatnich meczów.
    @GetMapping("/api/players/matches")
    public List<MatchDetailResponse> getPlayerMatchHistory(
        @RequestParam String gameName,
        @RequestParam String tagLine,
        @RequestParam String region,
        @RequestParam(defaultValue = "0") int start,
        @RequestParam(defaultValue = "5") int count
    ){
        return playerService.getMatchHistoryByRiotId(gameName, tagLine, region,start,count);
    }

}
