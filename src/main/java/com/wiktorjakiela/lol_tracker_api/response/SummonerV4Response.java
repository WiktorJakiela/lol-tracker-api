package com.wiktorjakiela.lol_tracker_api.response;

/**
 * Odwzorowuje JSON otrzymany z endpointu Summoner-V4.
 */
public record SummonerV4Response (
    String puuid,
    int profileIconId,
    long revisionDate,
    long summonerLevel
) {
    
}
    
