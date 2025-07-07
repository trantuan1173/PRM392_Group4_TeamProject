package com.example.prm392_group4_teamproject.model;

public class Photo {
    private String url;
    private boolean isMain;

    public Photo(String url, boolean isMain) {
        this.url = url;
        this.isMain = isMain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }
}

