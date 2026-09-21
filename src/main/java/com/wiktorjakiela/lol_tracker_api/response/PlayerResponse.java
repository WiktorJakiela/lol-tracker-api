package com.wiktorjakiela.lol_tracker_api.response;

/**
 * Określa dane gracza zwracane przez backend w formacie JSON.
 */
public record PlayerResponse(
    String gameName,
    String tagLine,
    String region,
    long  level
) {
}
