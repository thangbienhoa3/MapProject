package com.example.mapproject.controller;

import com.example.mapproject.model.Point;
import com.example.mapproject.model.RouteRequest;
import com.example.mapproject.model.RouteResponse;
import com.example.mapproject.service.RouteService;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.JsonFeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.RouteMatcher;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")

public class RouteController {
    @Autowired
    private RouteService routeService;
    @PostMapping("/route")
    public RouteResponse getRoute(@RequestBody RouteRequest q) {
        Point from = q.getFrom();
        Point to = q.getTo();

        return routeService.findShortestPath(from, to);
    }

    @PostMapping("/flooded")
    public String addFlooded(@RequestBody List<Point> coords) {
            routeService.setFloodedArea(coords);
            return "Flooded model loaded!";
    }

}
