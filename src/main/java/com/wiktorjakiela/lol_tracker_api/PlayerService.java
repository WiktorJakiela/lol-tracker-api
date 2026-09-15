package com.wiktorjakiela.lol_tracker_api;


import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    public PlayerResponse getDemoPlayer() {
        return new PlayerResponse("Faker", "KR1", "KR", 100);
    }
}
