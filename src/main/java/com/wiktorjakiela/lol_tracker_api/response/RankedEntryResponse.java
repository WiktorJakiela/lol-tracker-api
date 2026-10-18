package com.wiktorjakiela.lol_tracker_api.response;

/**
 * Wpis rankingowy dla frontendu, rozszerzony o liczbę gier i winrate.
 */
public record RankedEntryResponse (
    String queueType,
    String tier,
    String rank,
    int leaguePoints,
    int wins,
    int losses,
    int gamesPlayed,
    Double winrate
){

}
