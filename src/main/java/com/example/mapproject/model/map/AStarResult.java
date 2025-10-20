package com.example.mapproject.model.map;

import java.util.List;

/**
 * Contains the result of an A* search.
 */
public class AStarResult {
    private final List<Integer> nodeIds;
    private final double totalDistance;

    public AStarResult(List<Integer> nodeIds, double totalDistance) {
        this.nodeIds = nodeIds;
        this.totalDistance = totalDistance;
    }

    public List<Integer> getNodeIds() {
        return nodeIds;
    }

    public double getTotalDistance() {
        return totalDistance;
    }
}