package com.example.mapproject.service;

import com.example.mapproject.model.Point;
import com.example.mapproject.model.RouteResponse;
import com.example.mapproject.model.map.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {
    private final OsmGraphLoader graphLoader;
    private final MapGraph mapGraph;
    private final AStarRouter router;
    private final FloodZoneManager floodZoneManager;

    public RouteService() {
        this("src/main/resources/map-data/Phuong_VinhHung.osm.pbf", "graph-cache");
    }

    RouteService(String osmFile, String graphDirectory) {
        this.graphLoader = new OsmGraphLoader(osmFile, graphDirectory);
        this.graphLoader.load();
        this.mapGraph = graphLoader.getGraph();
        this.router = new AStarRouter();
        this.floodZoneManager = new FloodZoneManager();
    }

    public RouteResponse findShortestPath(Point from, Point to) {
        int startNodeId = graphLoader.findClosestNodeId(from.getLat(), from.getLng());
        int targetNodeId = graphLoader.findClosestNodeId(to.getLat(), to.getLng());
        AStarResult result = router.route(mapGraph, startNodeId, targetNodeId, floodZoneManager);
        List<Point> path = result.getNodeIds().stream()
                .map(graphLoader::getNode)
                .map(node -> new Point(node.getLat(), node.getLng()))
                .collect(Collectors.toList());
        return new RouteResponse(result.getTotalDistance(), path);
    }

    public void setFloodedArea(List<Point> coordinates) {
        floodZoneManager.addFloodZone(coordinates);
    }
}
