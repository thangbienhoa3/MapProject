package com.example.mapproject.model;

import java.util.List;

public class RouteResponse {
    private double distance;
    private List <Point> path;
    public RouteResponse(double distance, List<Point> path) {
        this.distance = distance;
        this.path = path;
    }

    public double getDistance() {return distance;}

    public List <Point> getPath() {return path;}
}
