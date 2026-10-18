package com.wiktorjakiela.lol_tracker_api.response;

import java.util.List;


/**
 * Wybrane pola odpowiedzi Riot Match-V5,
 * To dane wejściowe do mapowania na odpowiedzi naszego API.
 */
public record MatchV5Response (
    Metadata metadata,
    Info info
) {
    // Identyfikator meczu, oddzielony w JSON od właściwych danych rozgrywki.
    public record Metadata(
        String matchId
    ) {

    }
    //Informacje o rozgrywce oraz listy jej uczestników i drużyn.
    public record Info(
        // Informacje o Grze
        long gameDuration,
        int queueId,
        long gameStartTimestamp,
        String gameVersion,
        List<Participant> participants,
        List<Team> teams
    ) {
    }
    // Dane jednego uczestnika otrzymane z Riot, przed obliczeniami i uproszczeniem pól.
    public record Participant(
        // Tożsamość gracza
        String puuid,
        String riotIdGameName,
        String riotIdTagline,
        int participantId,
        int teamId,

        // Champion i pozycja
        int championId,
        String championName,
        int champLevel,
        String teamPosition,
        String individualPosition,

        // Wynik
        int kills,
        int deaths,
        int assists,
        boolean win,

        // Farma i ekonomia
        int totalMinionsKilled,
        int neutralMinionsKilled,
        int goldEarned,
        int goldSpent,

        // Wizja
        int visionScore,
        int wardsPlaced,
        int wardsKilled,
        int detectorWardsPlaced,

        // Przedmioty
        int item0,
        int item1,
        int item2,
        int item3,
        int item4,
        int item5,
        int item6,

        // Summoner spelle
        int summoner1Id,
        int summoner2Id,

        // Obrażenia
        int totalDamageDealtToChampions,
        int totalDamageTaken,
        int damageSelfMitigated,
        int totalHeal,

        // Cele mapy
        int damageDealtToObjectives,
        int damageDealtToTurrets,
        int turretTakedowns,
        int inhibitorTakedowns,

        // Multikille
        int doubleKills,
        int tripleKills,
        int quadraKills,
        int pentaKills,
        boolean firstBloodKill,

        // Runy
        Perks perks

    ) {

    }
    // Wynik drużyny, jej bany i statystyki celów mapy.
    public record Team (
        int teamId,
        boolean win,
        List<Ban> bans,
        Objectives objectives
    ){

    }
    // Zbanowany champion i kolejność bana w fazie wyboru postaci.
    public record Ban(
        int championId,
        int pickTurn
    ){

    }
    // Statystyki drużyny pogrupowane według rodzaju celu, w tym zabójstwa championów.
    public record Objectives(
        Objective baron,
        Objective champion,
        Objective dragon,
        Objective horde, // void grubsy
        Objective inhibitor,
        Objective riftHerald,
        Objective tower
    ) {
    }
    // Liczba zdobytych celów danego rodzaju oraz informacja o zdobyciu pierwszego.
    public record Objective(
        boolean first,
        int kills
    ) {
    }
    // Runy konkretnego uczestnika: odłamki statystyk i wybrane drzewka.
    public record Perks(
        PerkStats statPerks,
        List<PerkStyle> styles
    ){

    }
    // Identyfikatory trzech odłamków statystyk, a nie wartości przyznanych bonusów.
    public record PerkStats(
        int defense,
        int flex,
        int offense
    ){

    }
    // Drzewko run z jego ID, rolą (główne lub poboczne) i listą wybranych run.
    public record PerkStyle(
        String description,
        List<PerkSelection> selections,
        int style
    ){
    }
    // ID wybranej runy i jej statystyki z meczu; znaczenie var1-var3 zależy od runy.
    public record PerkSelection(
        int perk,
        int var1,
        int var2,
        int var3
    ){

    }
}
