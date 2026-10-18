package com.wiktorjakiela.lol_tracker_api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.wiktorjakiela.lol_tracker_api.client.RiotApiClient;
import com.wiktorjakiela.lol_tracker_api.exception.PlayerNotFoundException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotApiUnavailableException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotRateLimitException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotResourceNotFoundException;
import com.wiktorjakiela.lol_tracker_api.response.AccountV1Response;
import com.wiktorjakiela.lol_tracker_api.response.MatchV5Response;

class RiotApiClientTest {
    private static final String ACCOUNT_URL =
            "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/Faker/KR1";

    private MockRestServiceServer server;
    private RiotApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RiotApiClient(builder, "test-api-key");
    }

    @Test
    void accountLookupReturnsAccountOnSuccess() {
        server.expect(requestTo(ACCOUNT_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Riot-Token", "test-api-key"))
                .andRespond(withSuccess("""
                        {"puuid":"puuid-123","gameName":"Faker","tagLine":"KR1"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getAccountByRiotId("Faker", "KR1", "asia"))
                .isEqualTo(new AccountV1Response("puuid-123", "Faker", "KR1"));
        server.verify();
    }

    @Test
    void accountNotFoundBecomesPlayerNotFoundException() {
        server.expect(requestTo(ACCOUNT_URL))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getAccountByRiotId("Faker", "KR1", "asia"))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("Player not found.");
        server.verify();
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"UNAUTHORIZED", "FORBIDDEN"})
    void accountAccessErrorsBecomeUnavailableException(HttpStatus status) {
        server.expect(requestTo(ACCOUNT_URL))
                .andRespond(withStatus(status));

        assertThatThrownBy(() -> client.getAccountByRiotId("Faker", "KR1", "asia"))
                .isInstanceOf(RiotApiUnavailableException.class)
                .hasMessage("Data retrieval is currently unavailable.");
        server.verify();
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"UNAUTHORIZED", "FORBIDDEN"})
    void matchAccessErrorsAlsoBecomeUnavailableException(HttpStatus status) {
        server.expect(requestTo("https://asia.api.riotgames.com/lol/match/v5/matches/KR_111"))
                .andRespond(withStatus(status));

        assertThatThrownBy(() -> client.getMatchById("KR_111", "asia"))
                .isInstanceOf(RiotApiUnavailableException.class)
                .hasMessage("Data retrieval is currently unavailable.");
        server.verify();
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {
            "INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "SERVICE_UNAVAILABLE", "GATEWAY_TIMEOUT"
    })
    void accountServerErrorsBecomeUnavailableException(HttpStatus status) {
        server.expect(requestTo(ACCOUNT_URL)).andRespond(withStatus(status));

        assertThatThrownBy(() -> client.getAccountByRiotId("Faker", "KR1", "asia"))
                .isInstanceOf(RiotApiUnavailableException.class)
                .hasMessage("Riot services are temporarily unavailable.");
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"summoner", "league", "matchIds", "match"})
    void defaultStatusHandlerAlsoAppliesToOtherEndpoints(String endpoint) {
        String url = switch (endpoint) {
            case "summoner" -> "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/puuid-123";
            case "league" -> "https://kr.api.riotgames.com/lol/league/v4/entries/by-puuid/puuid-123";
            case "matchIds" -> "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/puuid-123/ids?start=0&count=5";
            case "match" -> "https://asia.api.riotgames.com/lol/match/v5/matches/KR_111";
            default -> throw new IllegalArgumentException(endpoint);
        };
        server.expect(requestTo(url)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> {
            switch (endpoint) {
                case "summoner" -> client.getSummonerByPuuid("puuid-123", "kr");
                case "league" -> client.getLeagueEntriesByPuuid("puuid-123", "kr");
                case "matchIds" -> client.getMatchIdsByPuuid("puuid-123", "asia", 0, 5);
                case "match" -> client.getMatchById("KR_111", "asia");
                default -> throw new IllegalArgumentException(endpoint);
            }
        }).isInstanceOf(RiotApiUnavailableException.class)
                .hasMessage("Riot services are temporarily unavailable.");
        server.verify();
    }

    @Test
    void accountRateLimitBecomesRateLimitException() {
        server.expect(requestTo(ACCOUNT_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.getAccountByRiotId("Faker", "KR1", "asia"))
                .isInstanceOf(RiotRateLimitException.class)
                .hasMessage("Riot API rate limit reached. Please try again later.");
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({"30, 30", "0, 0", "' 30 ', 30", "9223372036854775807, 9223372036854775807"})
    void rateLimitPreservesValidRetryAfterSeconds(String headerValue, long expectedSeconds) {
        server.expect(requestTo(ACCOUNT_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", headerValue));

        assertThatThrownBy(() -> client.getAccountByRiotId("Faker", "KR1", "asia"))
                .isInstanceOfSatisfying(RiotRateLimitException.class, exception -> {
                    assertThat(exception.getRetryAfterSeconds()).isEqualTo(expectedSeconds);
                    assertThat(exception.getMessage()).isEqualTo(
                            "Riot API rate limit reached. Please try again later.");
                });
        server.verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "abc", "-5", "1.5", "9223372036854775808", "-9223372036854775809"})
    void missingOrInvalidRetryAfterDoesNotHideRateLimit(String headerValue) {
        var response = withStatus(HttpStatus.TOO_MANY_REQUESTS);
        if (headerValue != null) {
            response.header("Retry-After", headerValue);
        }
        server.expect(requestTo(ACCOUNT_URL)).andRespond(response);

        assertThatThrownBy(() -> client.getAccountByRiotId("Faker", "KR1", "asia"))
                .isInstanceOfSatisfying(RiotRateLimitException.class, exception -> {
                    assertThat(exception.getRetryAfterSeconds()).isNull();
                    assertThat(exception.getMessage()).isEqualTo(
                            "Riot API rate limit reached. Please try again later.");
                });
        server.verify();
    }

    @Test
    void matchRateLimitAlsoBecomesRateLimitException() {
        server.expect(requestTo("https://asia.api.riotgames.com/lol/match/v5/matches/KR_111"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.getMatchById("KR_111", "asia"))
                .isInstanceOf(RiotRateLimitException.class)
                .hasMessage("Riot API rate limit reached. Please try again later.");
        server.verify();
    }

    @Test
    void matchLookupReturnsDataThroughExecuteRequest() {
        server.expect(requestTo("https://asia.api.riotgames.com/lol/match/v5/matches/KR_111"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Riot-Token", "test-api-key"))
                .andRespond(withSuccess("""
                        {
                          "metadata": {"matchId": "KR_111"},
                          "info": {
                            "gameDuration": 1800,
                            "queueId": 420,
                            "gameStartTimestamp": 123456789,
                            "gameVersion": "16.18.1",
                            "participants": [],
                            "teams": []
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        MatchV5Response match = client.getMatchById("KR_111", "asia");

        assertThat(match.metadata().matchId()).isEqualTo("KR_111");
        assertThat(match.info().gameDuration()).isEqualTo(1800L);
        assertThat(match.info().queueId()).isEqualTo(420);
        assertThat(match.info().participants()).isEmpty();
        assertThat(match.info().teams()).isEmpty();
        server.verify();
    }

    @Test
    void matchConnectionFailureBecomesUnavailableException() {
        server.expect(requestTo("https://asia.api.riotgames.com/lol/match/v5/matches/KR_111"))
                .andRespond(withException(new ConnectException("Connection refused")));

        assertThatThrownBy(() -> client.getMatchById("KR_111", "asia"))
                .isInstanceOf(RiotApiUnavailableException.class)
                .hasMessage("Unable to connect to Riot services.");
        server.verify();
    }

    @Test
    void matchTimeoutBecomesUnavailableException() {
        server.expect(requestTo("https://asia.api.riotgames.com/lol/match/v5/matches/KR_111"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> client.getMatchById("KR_111", "asia"))
                .isInstanceOf(RiotApiUnavailableException.class)
                .hasMessage("Unable to connect to Riot services.");
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({
            "account, false", "account, true",
            "summoner, false", "summoner, true",
            "league, false", "league, true",
            "matchIds, false", "matchIds, true"
    })
    void remainingEndpointsHandleConnectionFailuresAndTimeouts(String endpoint, boolean timeout) {
        String url = switch (endpoint) {
            case "account" -> ACCOUNT_URL;
            case "summoner" -> "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/puuid-123";
            case "league" -> "https://kr.api.riotgames.com/lol/league/v4/entries/by-puuid/puuid-123";
            case "matchIds" -> "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/puuid-123/ids?start=0&count=5";
            default -> throw new IllegalArgumentException(endpoint);
        };
        server.expect(requestTo(url)).andRespond(withException(timeout
                ? new SocketTimeoutException("Read timed out")
                : new ConnectException("Connection refused")));

        assertThatThrownBy(() -> {
            switch (endpoint) {
                case "account" -> client.getAccountByRiotId("Faker", "KR1", "asia");
                case "summoner" -> client.getSummonerByPuuid("puuid-123", "kr");
                case "league" -> client.getLeagueEntriesByPuuid("puuid-123", "kr");
                case "matchIds" -> client.getMatchIdsByPuuid("puuid-123", "asia", 0, 5);
                default -> throw new IllegalArgumentException(endpoint);
            }
        }).isInstanceOf(RiotApiUnavailableException.class)
                .hasMessage("Unable to connect to Riot services.");
        server.verify();
    }

    @Test
    void missingSummonerBecomesResourceNotFoundException() {
        server.expect(requestTo("https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/puuid-123"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getSummonerByPuuid("puuid-123", "kr"))
                .isInstanceOf(RiotResourceNotFoundException.class)
                .hasMessage("Player profile not found.");
        server.verify();
    }

    @Test
    void missingMatchIsNotReportedAsMissingPlayer() {
        server.expect(requestTo("https://asia.api.riotgames.com/lol/match/v5/matches/KR_111"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getMatchById("KR_111", "asia"))
                .isInstanceOf(RiotResourceNotFoundException.class)
                .hasMessage("Match not found.");
        server.verify();
    }
}
