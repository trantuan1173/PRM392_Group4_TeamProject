package com.example.prm392_group4_teamproject.model;

import java.util.List;

public class DiscoverUser {
    private String _id;
    private String name;
    private List<Photo> photos;

    public String getId() { return _id; }
    public String getName() { return name; }
    public List<Photo> getPhotos() { return photos; }

    public static class Photo {
        private String url;
        private boolean isMain;

        public String getUrl() { return url; }
        public boolean isMain() { return isMain; }
    }
}
