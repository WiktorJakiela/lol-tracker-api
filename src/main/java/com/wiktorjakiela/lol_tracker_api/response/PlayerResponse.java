package com.wiktorjakiela.lol_tracker_api.response;

import java.util.List;

/**
 * Profil zwracany przez nasze API, łączący dane konta, profil LoL i podsumowania rankingowe.
 */
public record PlayerResponse(
    String puuid,
    String gameName,
    String tagLine,
    String region,
    long  level,
    int profileIconId,
    List<RankedEntryResponse> rankedEntries
) {
}
