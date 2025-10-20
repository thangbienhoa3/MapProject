package com.example.mapproject.model.map;

import com.example.mapproject.model.map.*;

import java.util.*;

/**
 * Simple implementation of the A* shortest path algorithm using geographic distance as heuristic.
 */
public class AStarRouter {

    public AStarResult route(MapGraph graph, int startNodeId, int targetNodeId, FloodZoneManager floodZoneManager) {
        PriorityQueue<SearchNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(SearchNode::getEstimatedTotalCost));
        Map<Integer, Integer> cameFrom = new HashMap<>();
        Map<Integer, Double> gScore = new HashMap<>();
        Set<Integer> closedSet = new HashSet<>();

        MapNode startNode = graph.getNode(startNodeId);
        MapNode targetNode = graph.getNode(targetNodeId);
        if (startNode == null || targetNode == null) {
            throw new IllegalArgumentException("Không tìm thấy nút bắt đầu hoặc kết thúc trong đồ thị");
        }

        gScore.put(startNodeId, 0.0);
        openSet.add(new SearchNode(startNodeId, heuristic(startNode, targetNode)));

        while (!openSet.isEmpty()) {
            SearchNode current = openSet.poll();
            if (!closedSet.add(current.getNodeId())) {
                continue;
            }

            if (current.getNodeId() == targetNodeId) {
                return buildResult(graph, cameFrom, current.getNodeId());
            }

            MapNode currentNode = graph.getNode(current.getNodeId());
            for (MapEdge edge : graph.getNeighbors(current.getNodeId())) {
                int neighborId = edge.getTargetNodeId();
                MapNode neighborNode = graph.getNode(neighborId);
                if (neighborNode == null || closedSet.contains(neighborId)) {
                    continue;
                }
                if (floodZoneManager != null && floodZoneManager.isEdgeBlocked(currentNode, neighborNode)) {
                    continue;
                }

                double tentativeG = gScore.get(current.getNodeId()) + edge.getWeight();
                double currentBest = gScore.getOrDefault(neighborId, Double.POSITIVE_INFINITY);
                if (tentativeG >= currentBest) {
                    continue;
                }

                cameFrom.put(neighborId, current.getNodeId());
                gScore.put(neighborId, tentativeG);
                double fScore = tentativeG + heuristic(neighborNode, targetNode);
                openSet.add(new SearchNode(neighborId, fScore));
            }
        }
        throw new IllegalStateException("Không tìm thấy đường đi khả dụng giữa hai điểm");
    }

    private AStarResult buildResult(MapGraph graph, Map<Integer, Integer> cameFrom, int currentNodeId) {
        List<Integer> nodeIds = new ArrayList<>();
        nodeIds.add(currentNodeId);
        while (cameFrom.containsKey(currentNodeId)) {
            currentNodeId = cameFrom.get(currentNodeId);
            nodeIds.add(currentNodeId);
        }
        Collections.reverse(nodeIds);

        double distance = 0.0;
        for (int i = 1; i < nodeIds.size(); i++) {
            int from = nodeIds.get(i - 1);
            int to = nodeIds.get(i);
            distance += graph.getEdgeWeight(from, to);
        }
        return new AStarResult(nodeIds, distance);
    }

    private double heuristic(MapNode from, MapNode to) {
        double latDistance = Math.toRadians(to.getLat() - from.getLat());
        double lonDistance = Math.toRadians(to.getLng() - from.getLng());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(from.getLat())) * Math.cos(Math.toRadians(to.getLat()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        // Earth radius in meters
        return 6371000 * c;
    }

    private static class SearchNode {
        private final int nodeId;
        private final double estimatedTotalCost;

        private SearchNode(int nodeId, double estimatedTotalCost) {
            this.nodeId = nodeId;
            this.estimatedTotalCost = estimatedTotalCost;
        }

        public int getNodeId() {
            return nodeId;
        }

        public double getEstimatedTotalCost() {
            return estimatedTotalCost;
        }
    }
}

