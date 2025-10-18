package com.example.mapproject.model.map;

public class MapNode {
    private final int id;
    private final double lat;
    private final double lng;

    public MapNode(int id, double lat, double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
