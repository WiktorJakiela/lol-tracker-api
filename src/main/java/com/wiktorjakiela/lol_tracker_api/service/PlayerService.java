package com.wiktorjakiela.lol_tracker_api.service;


import org.springframework.stereotype.Service;

import com.wiktorjakiela.lol_tracker_api.client.RiotApiClient;
import com.wiktorjakiela.lol_tracker_api.response.PlayerResponse;
import com.wiktorjakiela.lol_tracker_api.response.SummonerV4Response;
import com.wiktorjakiela.lol_tracker_api.response.AccountV1Response;

/**
 * Zawiera logikę pobierania i przygotowywania danych graczy.
 */
@Service
public class PlayerService {
    private final RiotApiClient riotApiClient;

    public PlayerService(RiotApiClient riotApiClient) {
        this.riotApiClient = riotApiClient;
    }

    public PlayerResponse getPlayerByRiotId(String gameName, String tagLine,String region) {
        String platform = getPlatform(region);
        String regionalRoute = getRegionalRoute(platform);

        AccountV1Response account = riotApiClient.getAccountByRiotId(gameName, tagLine, regionalRoute);
        SummonerV4Response summoner = riotApiClient.getSummonerByPuuid(account.puuid(), platform);

        return new PlayerResponse(account.gameName(),account.tagLine(),region, summoner.summonerLevel());
    }

    private String getRegionalRoute(String platform) {
        return switch (platform) {
            case "eun1", "euw1" -> "europe";
            case "kr", "jp1" -> "asia";
            case "na1", "br1", "la1", "la2" -> "americas";
            default -> throw new IllegalArgumentException("Nieobsługiwana platforma:" + platform);
        };
    }
    private String getPlatform(String region) {
    return switch (region.toUpperCase()) {
        case "EUNE" -> "eun1";
        case "EUW" -> "euw1";
        case "KR" -> "kr";
        case "NA" -> "na1";
        default -> throw new IllegalArgumentException(
                "Nieobsługiwany region: " + region
        );
    };
}
}
