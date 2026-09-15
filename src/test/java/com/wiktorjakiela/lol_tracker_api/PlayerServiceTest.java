package com.wiktorjakiela.lol_tracker_api;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

class PlayerServiceTest {
    @Test
    void getDemoPlayerReturnsFakePlayerData() {
        PlayerService playerService = new PlayerService();
    
        PlayerResponse player = playerService.getDemoPlayer();

        assertThat(player.gameName()).isEqualTo("Faker");
        assertThat(player.tagLine()).isEqualTo("KR1");
        assertThat(player.region()).isEqualTo("KR");
        assertThat(player.level()).isEqualTo(100);
    }
}
