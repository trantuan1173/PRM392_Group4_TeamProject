package com.example.prm392_group4_teamproject;

public class MatchDetailResponse {
    private Match match;

    public MatchDetailResponse(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }
}