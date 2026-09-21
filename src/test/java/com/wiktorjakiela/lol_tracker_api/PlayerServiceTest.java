package com.wiktorjakiela.lol_tracker_api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.wiktorjakiela.lol_tracker_api.client.RiotApiClient;
import com.wiktorjakiela.lol_tracker_api.response.AccountV1Response;
import com.wiktorjakiela.lol_tracker_api.response.PlayerResponse;
import com.wiktorjakiela.lol_tracker_api.response.SummonerV4Response;
import com.wiktorjakiela.lol_tracker_api.service.PlayerService;

/**
 * Sprawdza logikę PlayerService bez uruchamiania serwera
 * i kontekstu Springa.
 */
class PlayerServiceTest {
    @Test
    void getPlayerByRiotIdCombinesRiotApiResponses() {
        RiotApiClient riotApiClient = mock(RiotApiClient.class);
        PlayerService playerService = new PlayerService(riotApiClient);

        when(riotApiClient.getAccountByRiotId("Faker", "KR1", "asia"))
                .thenReturn(new AccountV1Response("puuid-123", "Faker", "KR1"));
        when(riotApiClient.getSummonerByPuuid("puuid-123", "kr"))
                .thenReturn(new SummonerV4Response("puuid-123", 1234, 123456789L, 636L));

        PlayerResponse player = playerService.getPlayerByRiotId("Faker", "KR1", "KR");

        assertThat(player.gameName()).isEqualTo("Faker");
        assertThat(player.tagLine()).isEqualTo("KR1");
        assertThat(player.region()).isEqualTo("KR");
        assertThat(player.level()).isEqualTo(636L);

        verify(riotApiClient).getAccountByRiotId("Faker", "KR1", "asia");
        verify(riotApiClient).getSummonerByPuuid("puuid-123", "kr");
    }
}
