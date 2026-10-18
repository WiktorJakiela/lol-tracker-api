package com.wiktorjakiela.lol_tracker_api;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wiktorjakiela.lol_tracker_api.controller.PlayerController;
import com.wiktorjakiela.lol_tracker_api.exception.PlayerNotFoundException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotApiUnavailableException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotRateLimitException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotResourceNotFoundException;
import com.wiktorjakiela.lol_tracker_api.response.RankedEntryResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchDetailResponse;
import com.wiktorjakiela.lol_tracker_api.response.PlayerResponse;
import com.wiktorjakiela.lol_tracker_api.service.PlayerService;

/**
 * Sprawdza obsługę requestów przez kontroler przy użyciu
 * udawanego PlayerService.
 */
@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Test
    void playerByRiotIdReturnsPlayerDataFromRequestParams() throws Exception {
        RankedEntryResponse soloQueue = new RankedEntryResponse(
                "RANKED_SOLO_5x5", "CHALLENGER", "I", 1000, 100, 50, 150, 66.67
        );

        when(playerService.getPlayerByRiotId("Faker", "KR1", "KR"))
                .thenReturn(new PlayerResponse(
                        "puuid-123", "Faker", "KR1", "KR", 100, 1234, List.of(soloQueue)
                ));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puuid").value("puuid-123"))
                .andExpect(jsonPath("$.gameName").value("Faker"))
                .andExpect(jsonPath("$.tagLine").value("KR1"))
                .andExpect(jsonPath("$.region").value("KR"))
                .andExpect(jsonPath("$.level").value(100))
                .andExpect(jsonPath("$.profileIconId").value(1234))
                .andExpect(jsonPath("$.rankedEntries[0].queueType")
                        .value("RANKED_SOLO_5x5"))
                .andExpect(jsonPath("$.rankedEntries[0].tier")
                        .value("CHALLENGER"))
                .andExpect(jsonPath("$.rankedEntries[0].rank").value("I"))
                .andExpect(jsonPath("$.rankedEntries[0].leaguePoints")
                        .value(1000))
                .andExpect(jsonPath("$.rankedEntries[0].gamesPlayed").value(150))
                .andExpect(jsonPath("$.rankedEntries[0].winrate").value(66.67))
                .andExpect(jsonPath("$.rankedEntries[0].wins").value(100))
                .andExpect(jsonPath("$.rankedEntries[0].losses").value(50));
    }
    @Test
    void playerWithoutRanksReturnsEmptyRankedEntries() throws Exception {
        when(playerService.getPlayerByRiotId("Faker", "KR1", "KR"))
                .thenReturn(new PlayerResponse("puuid-123", "Faker", "KR1", "KR", 100, 5678, List.of()));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puuid").value("puuid-123"))
                .andExpect(jsonPath("$.profileIconId").value(5678))
                .andExpect(jsonPath("$.rankedEntries").isArray())
                .andExpect(jsonPath("$.rankedEntries").isEmpty());
    }

    @Test
    void unsupportedRegionReturnsBadRequest() throws Exception {
        when(playerService.getPlayerByRiotId("Faker", "KR1", "XYZ"))
                .thenThrow(new IllegalArgumentException(
                        "Unsupported region: XYZ"
                ));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Unsupported region: XYZ"));
    }

    @Test
    void missingPlayerReturnsNotFoundWithMessage() throws Exception {
        when(playerService.getPlayerByRiotId("Missing", "KR1", "KR"))
                .thenThrow(new PlayerNotFoundException("Player not found."));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Missing")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Player not found."));
    }

    @Test
    void matchHistoryForMissingPlayerReturnsNotFoundWithMessage() throws Exception {
        when(playerService.getMatchHistoryByRiotId("Missing", "KR1", "KR", 0, 5))
                .thenThrow(new PlayerNotFoundException("Player not found."));

        mockMvc.perform(get("/api/players/matches")
                        .param("gameName", "Missing")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Player not found."));
    }

    @Test
    void unavailableRiotReturnsServiceUnavailableForPlayerLookup() throws Exception {
        when(playerService.getPlayerByRiotId("Faker", "KR1", "KR"))
                .thenThrow(new RiotApiUnavailableException("Riot services are temporarily unavailable."));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Riot services are temporarily unavailable."));
    }

    @Test
    void unavailableRiotReturnsServiceUnavailableForMatchHistory() throws Exception {
        when(playerService.getMatchHistoryByRiotId("Faker", "KR1", "KR", 0, 5))
                .thenThrow(new RiotApiUnavailableException("Riot services are temporarily unavailable."));

        mockMvc.perform(get("/api/players/matches")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Riot services are temporarily unavailable."));
    }

    @Test
    void rateLimitedPlayerLookupReturnsServiceUnavailableWithMessage() throws Exception {
        String message = "Riot API rate limit reached. Please try again later.";
        when(playerService.getPlayerByRiotId("Faker", "KR1", "KR"))
                .thenThrow(new RiotRateLimitException(message));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().doesNotExist("Retry-After"))
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void rateLimitedMatchHistoryReturnsServiceUnavailableWithMessage() throws Exception {
        String message = "Riot API rate limit reached. Please try again later.";
        when(playerService.getMatchHistoryByRiotId("Faker", "KR1", "KR", 0, 5))
                .thenThrow(new RiotRateLimitException(message));

        mockMvc.perform(get("/api/players/matches")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().doesNotExist("Retry-After"))
                .andExpect(jsonPath("$.message").value(message));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 30})
    void rateLimitedPlayerLookupIncludesRetryAfter(long seconds) throws Exception {
        String message = "Riot API rate limit reached. Please try again later.";
        when(playerService.getPlayerByRiotId("Faker", "KR1", "KR"))
                .thenThrow(new RiotRateLimitException(message, seconds));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Faker").param("tagLine", "KR1").param("region", "KR"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", Long.toString(seconds)))
                .andExpect(jsonPath("$.message").value(message));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 30})
    void rateLimitedMatchHistoryIncludesRetryAfter(long seconds) throws Exception {
        String message = "Riot API rate limit reached. Please try again later.";
        when(playerService.getMatchHistoryByRiotId("Faker", "KR1", "KR", 0, 5))
                .thenThrow(new RiotRateLimitException(message, seconds));

        mockMvc.perform(get("/api/players/matches")
                        .param("gameName", "Faker").param("tagLine", "KR1").param("region", "KR"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", Long.toString(seconds)))
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void missingSummonerReturnsNotFoundWithProfileMessage() throws Exception {
        when(playerService.getPlayerByRiotId("Faker", "KR1", "KR"))
                .thenThrow(new RiotResourceNotFoundException("Player profile not found."));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Faker").param("tagLine", "KR1").param("region", "KR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Player profile not found."));
    }

    @Test
    void missingMatchReturnsNotFoundWithMatchMessage() throws Exception {
        when(playerService.getMatchHistoryByRiotId("Faker", "KR1", "KR", 0, 5))
                .thenThrow(new RiotResourceNotFoundException("Match not found."));

        mockMvc.perform(get("/api/players/matches")
                        .param("gameName", "Faker").param("tagLine", "KR1").param("region", "KR"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Match not found."));
    }

    @Test
    void matchHistoryUsesDefaultPaginationAndReturnsMatches() throws Exception {
        MatchDetailResponse match = new MatchDetailResponse(
                "KR_111", 123456789L, 1800L, 420, "16.18.1", List.of(), List.of()
        );
        when(playerService.getMatchHistoryByRiotId("Faker", "KR1", "KR", 0, 5))
                .thenReturn(List.of(match));

        mockMvc.perform(get("/api/players/matches")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchId").value("KR_111"))
                .andExpect(jsonPath("$[0].gameDuration").value(1800))
                .andExpect(jsonPath("$[0].queueId").value(420))
                .andExpect(jsonPath("$[0].participants").isArray())
                .andExpect(jsonPath("$[0].teams").isArray())
                .andExpect(jsonPath("$[0].metadata").doesNotExist())
                .andExpect(jsonPath("$[0].info").doesNotExist());

        verify(playerService).getMatchHistoryByRiotId(
                "Faker", "KR1", "KR", 0, 5
        );
    }

}
