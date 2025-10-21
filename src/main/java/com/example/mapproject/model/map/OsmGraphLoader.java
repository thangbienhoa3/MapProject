package com.example.mapproject.model.map;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;

/**
 * Loads an OSM file into memory using GraphHopper and exposes a simplified graph structure.
 */
public class OsmGraphLoader {
    private final String osmFile;
    private final String graphDirectory;
    private GraphHopper hopper;
    private MapGraph graph;

    public OsmGraphLoader(String osmFile, String graphDirectory) {
        this.osmFile = osmFile;
        this.graphDirectory = graphDirectory;
    }

    public void load() {
        hopper = new GraphHopper();
        hopper.setOSMFile(osmFile);
        hopper.setGraphHopperLocation(graphDirectory);
        Profile profile = new Profile("car")
                .setVehicle("car")
                .setWeighting("custom");
        hopper.setProfiles(profile);
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
        hopper.importOrLoad();
        buildGraph();
    }

    private void buildGraph() {
        BaseGraph storage = hopper.getBaseGraph();
        MapGraph mapGraph = new MapGraph();
        NodeAccess nodeAccess = storage.getNodeAccess();
        int nodes = storage.getNodes();
        for (int nodeId = 0; nodeId < nodes; nodeId++) {
            double lat = nodeAccess.getLat(nodeId);
            double lon = nodeAccess.getLon(nodeId);
            if (Double.isNaN(lat) || Double.isNaN(lon)) {
                continue;
            }
            mapGraph.addNode(nodeId, lat, lon);
        }

        AllEdgesIterator iterator = storage.getAllEdges();
        while (iterator.next()) {
            int base = iterator.getBaseNode();
            int adj = iterator.getAdjNode();
            double distance = iterator.getDistance();
            if (mapGraph.containsNode(base) && mapGraph.containsNode(adj)) {
                mapGraph.addEdge(base, adj, distance);
                mapGraph.addEdge(adj, base, distance);
            }
        }
        this.graph = mapGraph;
        System.out.println("Graph nodes: " + mapGraph.getNumNodes());
        System.out.println("Graph edges: " + mapGraph.getNumEdges());
    }

    public MapGraph getGraph() {
        return graph;
    }

    public int findClosestNodeId(double lat, double lng) {
        LocationIndex locationIndex = hopper.getLocationIndex();
        Snap snap = locationIndex.findClosest(lat, lng, EdgeFilter.ALL_EDGES);
        if (!snap.isValid()) {
            throw new IllegalArgumentException("Không tìm thấy nút gần nhất với toạ độ đã cho");
        }
        return snap.getClosestNode();
    }

    public MapNode getNode(int nodeId) {
        return graph.getNode(nodeId);
    }
}
