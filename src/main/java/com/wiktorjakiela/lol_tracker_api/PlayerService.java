package com.wiktorjakiela.lol_tracker_api;


import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    public PlayerResponse getDemoPlayer() {
        return new PlayerResponse("Faker", "KR1", "KR", 100);
    }

    public PlayerResponse getPlayerByRiotId(String gameName, String tagLine,String region) {
        return new PlayerResponse(gameName,tagLine,region, 100);
    }
}
