package com.wiktorjakiela.lol_tracker_api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;
    
    // Test Fakeowego Gracza
    @Test
    void demoPlayerReturnsFakePlayerData() throws Exception {
        when(playerService.getDemoPlayer())
                .thenReturn(new PlayerResponse("Faker", "KR1", "KR", 100));

        mockMvc.perform(get("/api/players/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameName").value("Faker"))
                .andExpect(jsonPath("$.tagLine").value("KR1"))
                .andExpect(jsonPath("$.region").value("KR"))
                .andExpect(jsonPath("$.level").value(100));
    }
    // Test Zczytywania Danych Gracza z URL 
    @Test
    void playerByRiotIdReturnsPlayerDataFromRequestParams() throws Exception {
        when(playerService.getPlayerByRiotId("Faker", "KR1", "KR"))
                .thenReturn(new PlayerResponse("Faker", "KR1", "KR", 100));

        mockMvc.perform(get("/api/players/by-riot-id")
                        .param("gameName", "Faker")
                        .param("tagLine", "KR1")
                        .param("region", "KR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameName").value("Faker"))
                .andExpect(jsonPath("$.tagLine").value("KR1"))
                .andExpect(jsonPath("$.region").value("KR"))
                .andExpect(jsonPath("$.level").value(100));
    }
    
}