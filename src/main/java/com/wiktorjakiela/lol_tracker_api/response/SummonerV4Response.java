package com.wiktorjakiela.lol_tracker_api.response;

/**
 * Dane profilu LoL z Riot Summoner-V4, pobierane po PUUID na konkretnym serwerze gry.
 */
public record SummonerV4Response (
    String puuid,
    int profileIconId,
    long revisionDate,
    long summonerLevel
) {

}

