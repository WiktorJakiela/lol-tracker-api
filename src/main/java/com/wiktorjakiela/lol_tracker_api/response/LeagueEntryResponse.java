package com.wiktorjakiela.lol_tracker_api.response;

/**
 * Pojedynczy wpis rankingowy z Riot League-V4 dla jednego rodzaju kolejki gracza.
 */
public record LeagueEntryResponse(
        String queueType,
        String tier,
        String rank,
        int leaguePoints,
        int wins,
        int losses
) {
}
