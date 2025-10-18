package com.example.mapproject.model.map;

import java.util.*;

public class MapGraph {
    private final Map<Integer, MapNode> nodes = new HashMap<>();
    private final Map<Integer, List<MapEdge>> adjacency = new HashMap<>();

    public void addNode(int id, double lat, double lng) {
        nodes.put(id, new MapNode(id, lat, lng));
        adjacency.computeIfAbsent(id, k -> new ArrayList<>());
    }
    public void addEdge(int fromId, int toId, double weight) {
        if (!nodes.containsKey(fromId) || !nodes.containsKey(toId)) {
            return;
        }
        adjacency.computeIfAbsent(fromId, k -> new ArrayList<>()).add(new MapEdge(toId, weight));
    }
    public boolean containsNode(int id) {
        return nodes.containsKey(id);
    }

    public MapNode getNode(int id) {
        return nodes.get(id);
    }

    public Collection<MapNode> getNodes() {
        return nodes.values();
    }

    public List<MapEdge> getNeighbors(int id) {
        return adjacency.getOrDefault(id, Collections.emptyList());
    }

    public double getEdgeWeight(int fromId, int toId) {
        for (MapEdge edge : getNeighbors(fromId)) {
            if (edge.getTargetNodeId() == toId) {
                return edge.getWeight();
            }
        }
        return Double.POSITIVE_INFINITY;
    }
}
