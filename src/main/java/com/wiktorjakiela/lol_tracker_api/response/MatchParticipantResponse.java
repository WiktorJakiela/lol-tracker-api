package com.wiktorjakiela.lol_tracker_api.response;

import java.util.List;

//Dane uczestnika meczu dla frontendu
public record MatchParticipantResponse (
    // Tożsamość
    String puuid,
    String gameName,
    String tagLine,

    // Drużyna i wynik
    int teamId,
    boolean win,

    // Champion
    int championId,
    String championName,
    int championLevel,

    // pozycja
    String role,

    // KDA
    int kills,
    int deaths,
    int assists,
    Double kda,
    Double killParticipation,

    // Farma i Złoto
    int cs,
    Double csPerMinute,
    int goldEarned,

    // Wizja
    int visionScore,

    // Itemy
    List<Integer> itemIds,

    // Summonerki
    int summoner1Id,
    int summoner2Id,

    // Obrażenia
    int damageToChampions,
    int damageTaken,

    // runy
    MatchRunesResponse runes
) {

}
