package com.example.prm392_group4_teamproject.model;

import java.util.List;

public class LocationData {
    private String type;
    private List<Double> coordinates;
    private String address;

    public LocationData(String type, List<Double> coordinates, String address) {
        this.type = type;
        this.coordinates = coordinates;
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public String getAddress() {
        return address;
    }
    public double getLatitude() {
        return coordinates != null && coordinates.size() >= 2 ? coordinates.get(1) : 0.0;
    }

    public double getLongitude() {
        return coordinates != null && coordinates.size() >= 2 ? coordinates.get(0) : 0.0;
    }

}

