package com.wiktorjakiela.lol_tracker_api.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.wiktorjakiela.lol_tracker_api.response.AccountV1Response;
import com.wiktorjakiela.lol_tracker_api.response.SummonerV4Response;

import org.springframework.beans.factory.annotation.Value;
/**
 * Odpowiada za komunikację z Riot API, wysyłanie requestów HTTP
 * i odbieranie odpowiedzi.
 */
@Component 
public class RiotApiClient {
    private final RestClient restClient;
    private final String apiKey;
     
    // konstruktor
    public RiotApiClient(RestClient.Builder restClientBuilder, 
        @Value("${riot.api.key}") String apiKey) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
    }
    public AccountV1Response getAccountByRiotId(String gameName, String tagLine, String regionalRoute) {
        return restClient.get().uri(
            "https://" + regionalRoute + ".api.riotgames.com/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
            .header("X-Riot-Token",apiKey).retrieve()
            .body(AccountV1Response.class);
    }
    public SummonerV4Response getSummonerByPuuid(String puuid, String platform) {
        return restClient.get().uri(
            "https://" + platform + ".api.riotgames.com/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid)
            .header("X-Riot-Token",apiKey).retrieve()
            .body(SummonerV4Response.class);
    }

}
