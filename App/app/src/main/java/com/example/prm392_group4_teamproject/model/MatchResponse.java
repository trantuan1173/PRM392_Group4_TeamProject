package com.example.prm392_group4_teamproject.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MatchResponse {
    @SerializedName("matches")
    private List<Match> matches;

    public List<Match> getMatches() {
        return matches;
    }

    public static class Match {
        @SerializedName("_id")
        private String id;
        private FriendUser user1;
        private FriendUser user2;

        public String getId() {
            return id;
        }

        public FriendUser getUser1() {
            return user1;
        }

        public FriendUser getUser2() {
            return user2;
        }
    }
}
