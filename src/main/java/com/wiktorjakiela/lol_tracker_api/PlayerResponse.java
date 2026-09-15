package com.wiktorjakiela.lol_tracker_api;

public record PlayerResponse(
    String gameName,
    String tagLine,
    String region,
    int level
) {
}
