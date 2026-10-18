package com.wiktorjakiela.lol_tracker_api.response;

import java.util.List;

/**
 * Uproszczony zestaw run uczestnika: identyfikatory drzewek, wybranych run i odłamków statystyk.
 */
public record MatchRunesResponse(
    int primaryStyleId,
    int secondaryStyleId,
    List<Integer> primaryRuneIds,
    List<Integer> secondaryRuneIds,
    int offenseShardId,
    int flexShardId,
    int defenseShardId
) {

}
