package com.example.mapproject.service;

import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.Graph;
import org.springframework.stereotype.Service;


public class PathService {

    private BaseGraph graph;

    public PathService(BaseGraph graph) {
        this.graph = graph;
    }

    public void setGraph(BaseGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        this.graph = graph;
    }

    /**
     * Returns basic information about the loaded graph.
     */
    public String getPathInfo() {
        if (graph == null) {
            return "Graph is not initialized.";
        }

        int nodeCount = graph.getNodes();
        int edgeCount = graph.getEdges();

        return String.format("Graph info → Nodes: %d, Edges: %d", nodeCount, edgeCount);
    }
}
