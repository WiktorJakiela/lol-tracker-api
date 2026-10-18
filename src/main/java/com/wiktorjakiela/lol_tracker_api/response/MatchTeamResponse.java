package com.wiktorjakiela.lol_tracker_api.response;

import java.util.List;

/**
 * Podsumowanie drużyny dla frontendu: wynik, zabójstwa, bany i zdobyte cele mapy.
 */
public record MatchTeamResponse (
    int teamId,
    boolean win,
    int kills,
    List<Integer> bannedChampionIds,
    int baronKills,
    int dragonKills,
    int hordeKills, // void Grubsy
    int riftHeraldKills,
    int towerKills,
    int inhibitorKills
    ){
}
