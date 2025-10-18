package com.example.mapproject.model.map;

public class MapEdge {
    private final int targetNodeId;
    private final double weight;

    public MapEdge(int targetNodeId, double weight) {
        this.targetNodeId = targetNodeId;
        this.weight = weight;
    }

    public int getTargetNodeId() {
        return targetNodeId;
    }

    public double getWeight() {
        return weight;
    }
}
