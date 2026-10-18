package com.wiktorjakiela.lol_tracker_api;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.wiktorjakiela.lol_tracker_api.client.RiotApiClient;
import com.wiktorjakiela.lol_tracker_api.response.AccountV1Response;
import com.wiktorjakiela.lol_tracker_api.response.LeagueEntryResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchV5Response;
import com.wiktorjakiela.lol_tracker_api.response.MatchDetailResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchParticipantResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchRunesResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchTeamResponse;
import com.wiktorjakiela.lol_tracker_api.response.PlayerResponse;
import com.wiktorjakiela.lol_tracker_api.response.RankedEntryResponse;
import com.wiktorjakiela.lol_tracker_api.response.SummonerV4Response;
import com.wiktorjakiela.lol_tracker_api.service.PlayerService;

/**
 * Sprawdza logikę PlayerService bez uruchamiania serwera
 * i kontekstu Springa.
 */
class PlayerServiceTest {
    @ParameterizedTest
    @MethodSource("invalidPlayerInputs")
    void playerLookupRejectsInvalidInputBeforeCallingRiot(
            String gameName, String tagLine, String region, String message) {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);

        assertThatThrownBy(() -> service.getPlayerByRiotId(gameName, tagLine, region))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
        verifyNoInteractions(client);
    }

    @ParameterizedTest
    @MethodSource("invalidPlayerInputs")
    void matchHistoryRejectsInvalidInputBeforeCallingRiot(
            String gameName, String tagLine, String region, String message) {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);

        assertThatThrownBy(() -> service.getMatchHistoryByRiotId(gameName, tagLine, region, 0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
        verifyNoInteractions(client);
    }

    private static Stream<Arguments> invalidPlayerInputs() {
        return Stream.of(null, "", "   ", "\t\n").flatMap(value -> Stream.of(
                Arguments.of(value, "KR1", "KR", "Player name must not be blank."),
                Arguments.of("Faker", value, "KR", "Tag must not be blank."),
                Arguments.of("Faker", "KR1", value, "Region must not be blank.")
        ));
    }

    @Test
    void playerLookupPreservesSpacesInsideGameName() {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);
        when(client.getAccountByRiotId("Hide on bush", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Hide on bush", "KR1"));
        when(client.getSummonerByPuuid("puuid-123", "kr"))
                .thenReturn(new SummonerV4Response("puuid-123", 1234, 123456789L, 636L));
        when(client.getLeagueEntriesByPuuid("puuid-123", "kr"))
                .thenReturn(List.of());

        PlayerResponse player = service.getPlayerByRiotId("Hide on bush", "KR1", "KR");

        assertThat(player.gameName()).isEqualTo("Hide on bush");
        assertThat(player.puuid()).isEqualTo("puuid-123");
        assertThat(player.rankedEntries()).isEmpty();
        verify(client).getAccountByRiotId("Hide on bush", "KR1", "asia");
    }

    @Test
    void matchHistoryPreservesSpacesInsideGameName() {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);
        when(client.getAccountByRiotId("Hide on bush", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Hide on bush", "KR1"));
        when(client.getMatchIdsByPuuid("puuid-123", "asia", 0, 5))
                .thenReturn(List.of());

        assertThat(service.getMatchHistoryByRiotId("Hide on bush", "KR1", "KR", 0, 5))
                .isEmpty();
        verify(client).getAccountByRiotId("Hide on bush", "KR1", "asia");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    void matchHistoryRejectsNegativeStartBeforeCallingRiot(int start) {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);

        assertThatThrownBy(() -> service.getMatchHistoryByRiotId("Faker", "KR1", "KR", start, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The start parameter must not be negative.");
        verifyNoInteractions(client);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 11, Integer.MAX_VALUE})
    void matchHistoryRejectsInvalidCountBeforeCallingRiot(int count) {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);

        assertThatThrownBy(() -> service.getMatchHistoryByRiotId("Faker", "KR1", "KR", 0, count))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The count parameter must be between 1 and 10.");
        verifyNoInteractions(client);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void matchHistoryAcceptsValidCountIncludingBoundaries(int count) {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);
        when(client.getAccountByRiotId("Faker", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Faker", "KR1"));
        when(client.getMatchIdsByPuuid("puuid-123", "asia", 0, count))
                .thenReturn(List.of());

        assertThat(service.getMatchHistoryByRiotId("Faker", "KR1", "KR", 0, count)).isEmpty();
        verify(client).getMatchIdsByPuuid("puuid-123", "asia", 0, count);
    }

    @Test
    void getPlayerByRiotIdCombinesRiotApiResponses() {
        RiotApiClient riotApiClient = mock(RiotApiClient.class);
        PlayerService playerService = new PlayerService(riotApiClient);

        when(riotApiClient.getAccountByRiotId("Faker", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Faker", "KR1"));
        when(riotApiClient.getSummonerByPuuid("puuid-123", "kr"))
                .thenReturn(new SummonerV4Response("puuid-123", 1234, 123456789L, 636L));
        LeagueEntryResponse soloQueue = new LeagueEntryResponse(
                "RANKED_SOLO_5x5", "CHALLENGER", "I", 1000, 100, 50
        );
        LeagueEntryResponse flexQueue = new LeagueEntryResponse(
                "RANKED_FLEX_SR", "DIAMOND", "II", 75, 30, 20
        );
        when(riotApiClient.getLeagueEntriesByPuuid("puuid-123", "kr"))
                .thenReturn(List.of(soloQueue, flexQueue));

        PlayerResponse player = playerService.getPlayerByRiotId("Faker", "KR1", "KR");

        assertThat(player.gameName()).isEqualTo("Faker");
        assertThat(player.puuid()).isEqualTo("puuid-123");
        assertThat(player.tagLine()).isEqualTo("KR1");
        assertThat(player.region()).isEqualTo("KR");
        assertThat(player.level()).isEqualTo(636L);
        assertThat(player.profileIconId()).isEqualTo(1234);
        assertThat(player.rankedEntries()).containsExactly(
                new RankedEntryResponse("RANKED_SOLO_5x5", "CHALLENGER", "I", 1000,
                        100, 50, 150, 66.67),
                new RankedEntryResponse("RANKED_FLEX_SR", "DIAMOND", "II", 75,
                        30, 20, 50, 60.0));

        verify(riotApiClient).getAccountByRiotId("Faker", "KR1", "asia");
        verify(riotApiClient).getSummonerByPuuid("puuid-123", "kr");
        verify(riotApiClient).getLeagueEntriesByPuuid("puuid-123", "kr");
    }

    @ParameterizedTest
    @MethodSource("rankedStatistics")
    void playerLookupCalculatesRankedGamesPlayedAndWinRate(int wins, int losses, double expectedWinRate) {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);
        when(client.getAccountByRiotId("Faker", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Faker", "KR1"));
        when(client.getSummonerByPuuid("puuid-123", "kr"))
                .thenReturn(new SummonerV4Response("puuid-123", 1234, 123456789L, 636L));
        when(client.getLeagueEntriesByPuuid("puuid-123", "kr"))
                .thenReturn(List.of(new LeagueEntryResponse(
                        "RANKED_SOLO_5x5", "DIAMOND", "II", 75, wins, losses)));

        PlayerResponse player = service.getPlayerByRiotId("Faker", "KR1", "KR");

        assertThat(player.rankedEntries()).containsExactly(new RankedEntryResponse(
                "RANKED_SOLO_5x5", "DIAMOND", "II", 75, wins, losses, wins + losses, expectedWinRate));
    }

    private static Stream<Arguments> rankedStatistics() {
        return Stream.of(
                Arguments.of(30, 20, 60.0),
                Arguments.of(10, 0, 100.0),
                Arguments.of(0, 10, 0.0),
                Arguments.of(1, 2, 33.33),
                Arguments.of(0, 0, 0.0));
    }

    @Test
    void getMatchHistoryByRiotIdReturnsMatchesInIdOrder() {
        RiotApiClient riotApiClient = mock(RiotApiClient.class);
        PlayerService playerService = new PlayerService(riotApiClient);

        when(riotApiClient.getAccountByRiotId("Faker", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Faker", "KR1"));
        when(riotApiClient.getMatchIdsByPuuid("puuid-123", "asia", 0, 2))
                .thenReturn(List.of("KR_111", "KR_222"));

        MatchV5Response firstMatch = new MatchV5Response(
                new MatchV5Response.Metadata("KR_111"),
                new MatchV5Response.Info(
                        1800L, 420, 123456789L, "16.18.1", List.of(), List.of()
                )
        );
        MatchV5Response secondMatch = new MatchV5Response(
                new MatchV5Response.Metadata("KR_222"),
                new MatchV5Response.Info(
                        1500L, 440, 123456999L, "16.18.1", List.of(), List.of()
                )
        );

        when(riotApiClient.getMatchById("KR_111", "asia"))
                .thenReturn(firstMatch);
        when(riotApiClient.getMatchById("KR_222", "asia"))
                .thenReturn(secondMatch);

        List<MatchDetailResponse> matches = playerService.getMatchHistoryByRiotId(
                "Faker", "KR1", "KR", 0, 2
        );

        assertThat(matches).containsExactly(
                new MatchDetailResponse("KR_111", 123456789L, 1800L, 420,
                        "16.18.1", List.of(), List.of()),
                new MatchDetailResponse("KR_222", 123456999L, 1500L, 440,
                        "16.18.1", List.of(), List.of())
        );

        verify(riotApiClient).getAccountByRiotId("Faker", "KR1", "asia");
        verify(riotApiClient).getMatchIdsByPuuid("puuid-123", "asia", 0, 2);
        verify(riotApiClient).getMatchById("KR_111", "asia");
        verify(riotApiClient).getMatchById("KR_222", "asia");
    }

    @Test
    void matchHistoryUsesZeroForMissingMapObjectives() {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);
        MatchV5Response.Team team = new MatchV5Response.Team(
                100, true, List.of(),
                new MatchV5Response.Objectives(
                        null, new MatchV5Response.Objective(true, 23),
                        null, null, null, null, null
                )
        );
        when(client.getAccountByRiotId("Faker", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Faker", "KR1"));
        when(client.getMatchIdsByPuuid("puuid-123", "asia", 0, 1))
                .thenReturn(List.of("KR_111"));
        when(client.getMatchById("KR_111", "asia"))
                .thenReturn(new MatchV5Response(new MatchV5Response.Metadata("KR_111"),
                        new MatchV5Response.Info(1800L, 420, 123456789L, "16.18.1",
                                List.of(), List.of(team))));

        List<MatchDetailResponse> matches = service.getMatchHistoryByRiotId(
                "Faker", "KR1", "KR", 0, 1);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().teams()).containsExactly(
                new MatchTeamResponse(100, true, 23, List.of(), 0, 0, 0, 0, 0, 0));
    }

    @Test
    void matchHistoryMapsParticipantAndTeamStatistics() {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);
        MatchV5Response.Participant participant = participantWithPerks(null);
        MatchV5Response.Team team = new MatchV5Response.Team(
                100, true,
                List.of(new MatchV5Response.Ban(51, 1), new MatchV5Response.Ban(-1, 2)),
                new MatchV5Response.Objectives(
                        new MatchV5Response.Objective(true, 1),
                        new MatchV5Response.Objective(true, 42),
                        new MatchV5Response.Objective(true, 3),
                        new MatchV5Response.Objective(true, 6),
                        new MatchV5Response.Objective(true, 2),
                        new MatchV5Response.Objective(false, 0),
                        new MatchV5Response.Objective(true, 7)
                )
        );
        when(client.getAccountByRiotId("Faker", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Faker", "KR1"));
        when(client.getMatchIdsByPuuid("puuid-123", "asia", 5, 1))
                .thenReturn(List.of("KR_111"));
        when(client.getMatchById("KR_111", "asia"))
                .thenReturn(new MatchV5Response(new MatchV5Response.Metadata("KR_111"),
                        new MatchV5Response.Info(1800L, 420, 123456789L, "16.18.1",
                                List.of(participant), List.of(team))));

        List<MatchDetailResponse> matches = service.getMatchHistoryByRiotId(
                "Faker", "KR1", "KR", 5, 1);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().participants()).containsExactly(
                new MatchParticipantResponse("puuid-123", "Faker", "KR1", 100, true,
                        103, "Ahri", 17, "MIDDLE", 10, 4, 11, 5.25, 50.0, 215, 7.17, 15346, 37,
                        List.of(3157, 0, 3171, 3089, 4633, 1082, 3340),
                        4, 12, 23708, 40051, null));
        assertThat(matches.getFirst().teams()).containsExactly(
                new MatchTeamResponse(100, true, 42, List.of(51, -1), 1, 3, 6, 0, 7, 2));
        verify(client).getMatchIdsByPuuid("puuid-123", "asia", 5, 1);
    }

    @Test
    void matchHistoryMapsRunesByDescriptionRegardlessOfStyleOrder() {
        MatchV5Response.Perks perks = new MatchV5Response.Perks(
                new MatchV5Response.PerkStats(5001, 5008, 5005),
                List.of(
                        new MatchV5Response.PerkStyle("subStyle", List.of(
                                new MatchV5Response.PerkSelection(8345, 10, 20, 30),
                                new MatchV5Response.PerkSelection(8347, 40, 50, 60)), 8300),
                        new MatchV5Response.PerkStyle("primaryStyle", List.of(
                                new MatchV5Response.PerkSelection(8112, 100, 200, 300),
                                new MatchV5Response.PerkSelection(8126, 1, 2, 3),
                                new MatchV5Response.PerkSelection(8143, 4, 5, 6),
                                new MatchV5Response.PerkSelection(8135, 7, 8, 9)), 8100)));

        assertThat(mappedParticipant(perks).runes()).isEqualTo(new MatchRunesResponse(
                8100, 8300, List.of(8112, 8126, 8143, 8135), List.of(8345, 8347),
                5005, 5008, 5001));
    }

    @ParameterizedTest
    @MethodSource("participantStatistics")
    void matchHistoryCalculatesParticipantStatistics(
            int kills, int deaths, int assists, int laneCs, int jungleCs,
            long duration, int teamKills, Double expectedKda, double expectedKp, Double expectedCsPerMinute) {
        MatchV5Response.Participant participant = participantWithPerks(
                null, kills, deaths, assists, laneCs, jungleCs);

        MatchParticipantResponse result = mappedParticipant(
                participant, duration, List.of(teamWithKills(100, teamKills)));

        assertThat(result.kda()).isEqualTo(expectedKda);
        assertThat(result.killParticipation()).isEqualTo(expectedKp);
        assertThat(result.cs()).isEqualTo(laneCs + jungleCs);
        assertThat(result.csPerMinute()).isEqualTo(expectedCsPerMinute);
    }

    private static Stream<Arguments> participantStatistics() {
        return Stream.of(
                Arguments.of(5, 3, 10, 170, 10, 1800L, 30, 5.0, 50.0, 6.0),
                Arguments.of(1, 3, 1, 8, 1, 90L, 3, 0.67, 66.67, 6.0),
                Arguments.of(0, 2, 0, 0, 0, 90L, 0, 0.0, 0.0, 0.0),
                Arguments.of(5, 0, 10, 180, 0, 1800L, 30, null, 50.0, 6.0),
                Arguments.of(5, 3, 10, 180, 0, 0L, 30, 5.0, 50.0, null));
    }

    @Test
    void matchHistoryUsesParticipantTeamKillsRegardlessOfTeamOrder() {
        MatchParticipantResponse result = mappedParticipant(
                participantWithPerks(null), 1800L,
                List.of(teamWithKills(200, 30), teamWithKills(100, 42)));

        assertThat(result.killParticipation()).isEqualTo(50.0);
    }

    @Test
    void matchHistoryRejectsParticipantWithoutMatchingTeam() {
        assertThatThrownBy(() -> mappedParticipant(
                participantWithPerks(null), 1800L, List.of(teamWithKills(200, 30))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Missing team 100 in match KR_111");
    }

    @Test
    void matchHistoryReturnsNullRunesWhenRequiredPerkDataIsMissing() {
        assertThat(mappedParticipant(null).runes()).isNull();
        assertThat(mappedParticipant(new MatchV5Response.Perks(null, List.of())).runes())
                .isNull();
        assertThat(mappedParticipant(new MatchV5Response.Perks(
                new MatchV5Response.PerkStats(5001, 5008, 5005), null)).runes())
                .isNull();
    }

    @Test
    void matchHistorySkipsMissingSelectionsAndStillMapsNextStyle() {
        MatchV5Response.Perks perks = new MatchV5Response.Perks(
                new MatchV5Response.PerkStats(5001, 5008, 5005),
                List.of(
                        new MatchV5Response.PerkStyle("primaryStyle", null, 8100),
                        new MatchV5Response.PerkStyle("subStyle", List.of(
                                new MatchV5Response.PerkSelection(8345, 0, 0, 0)), 8300)));

        assertThat(mappedParticipant(perks).runes()).isEqualTo(new MatchRunesResponse(
                0, 8300, List.of(), List.of(8345), 5005, 5008, 5001));
    }

    @Test
    void matchHistoryIgnoresUnknownAndNullStyleDescriptions() {
        MatchV5Response.Perks perks = new MatchV5Response.Perks(
                new MatchV5Response.PerkStats(5001, 5008, 5005),
                List.of(
                        new MatchV5Response.PerkStyle(null, List.of(
                                new MatchV5Response.PerkSelection(8112, 0, 0, 0)), 8100),
                        new MatchV5Response.PerkStyle("unknown", List.of(
                                new MatchV5Response.PerkSelection(8345, 0, 0, 0)), 8300)));

        assertThat(mappedParticipant(perks).runes()).isEqualTo(new MatchRunesResponse(
                0, 0, List.of(), List.of(), 5005, 5008, 5001));
    }

    private MatchParticipantResponse mappedParticipant(MatchV5Response.Perks perks) {
        return mappedParticipant(participantWithPerks(perks), 1800L, List.of(teamWithKills(100, 42)));
    }

    private MatchV5Response.Team teamWithKills(int teamId, int kills) {
        return new MatchV5Response.Team(teamId, true, List.of(),
                new MatchV5Response.Objectives(
                        null, new MatchV5Response.Objective(true, kills),
                        null, null, null, null, null));
    }

    private MatchParticipantResponse mappedParticipant(
            MatchV5Response.Participant participant, long duration, List<MatchV5Response.Team> teams) {
        RiotApiClient client = mock(RiotApiClient.class);
        PlayerService service = new PlayerService(client);
        when(client.getAccountByRiotId("Faker", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Faker", "KR1"));
        when(client.getMatchIdsByPuuid("puuid-123", "asia", 0, 1))
                .thenReturn(List.of("KR_111"));
        when(client.getMatchById("KR_111", "asia"))
                .thenReturn(new MatchV5Response(new MatchV5Response.Metadata("KR_111"),
                        new MatchV5Response.Info(duration, 420, 123456789L, "16.18.1",
                                List.of(participant), teams)));

        return service.getMatchHistoryByRiotId("Faker", "KR1", "KR", 0, 1)
                .getFirst().participants().getFirst();
    }

    private MatchV5Response.Participant participantWithPerks(MatchV5Response.Perks perks) {
        return participantWithPerks(perks, 10, 4, 11, 200, 15);
    }

    private MatchV5Response.Participant participantWithPerks(
            MatchV5Response.Perks perks, int kills, int deaths, int assists, int laneCs, int jungleCs) {
        return new MatchV5Response.Participant(
                "puuid-123", "Faker", "KR1", 1, 100,
                103, "Ahri", 17, "MIDDLE", "TOP",
                kills, deaths, assists, true,
                laneCs, jungleCs, 15346, 14410,
                37, 11, 7, 2,
                3157, 0, 3171, 3089, 4633, 1082, 3340,
                4, 12,
                23708, 40051, 16828, 41294,
                22335, 10164, 4, 2,
                2, 0, 0, 0, false, perks);
    }
}
