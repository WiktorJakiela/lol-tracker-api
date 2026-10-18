package com.wiktorjakiela.lol_tracker_api.response;

/**
 * Dane konta z Riot Account-V1: PUUID oraz nazwa i tag tworzące Riot ID.
 */
public record AccountV1Response (
    String puuid,
    String gameName,
    String tagLine
){

}
