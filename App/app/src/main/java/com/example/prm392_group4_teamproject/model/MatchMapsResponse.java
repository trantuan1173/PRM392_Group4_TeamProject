package com.example.prm392_group4_teamproject.model;

import java.util.List;

public class MatchMapsResponse {
    public List<MatchItem> matches;
    public Pagination pagination;

    public MatchMapsResponse(List<MatchItem> matches, Pagination pagination) {
        this.matches = matches;
        this.pagination = pagination;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<MatchItem> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchItem> matches) {
        this.matches = matches;
    }
}
