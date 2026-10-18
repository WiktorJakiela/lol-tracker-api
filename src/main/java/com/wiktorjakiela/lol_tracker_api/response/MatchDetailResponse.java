package com.wiktorjakiela.lol_tracker_api.response;

import java.util.List;

/**
 * Dane jednego meczu zwracane przez API: informacje o grze, uczestnicy i drużyny.
 */
public record MatchDetailResponse(
    String matchId,
    long gameStartTimestamp,
    long gameDuration,
    int queueId,
    String gameVersion,
    List<MatchParticipantResponse> participants,
    List<MatchTeamResponse> teams
) {

}
