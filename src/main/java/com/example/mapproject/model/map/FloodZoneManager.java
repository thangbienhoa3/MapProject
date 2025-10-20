package com.example.mapproject.model.map;

import com.example.mapproject.model.Point;
import com.example.mapproject.model.map.MapNode;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores flooded areas and provides helpers to check whether an edge crosses them.
 */
public class FloodZoneManager {
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final List<Polygon> floodZones = new ArrayList<>();

    public void addFloodZone(List<Point> coordinates) {
        if (coordinates == null || coordinates.size() < 3) {
            return;
        }
        List<Coordinate> coords = new ArrayList<>();
        for (Point point : coordinates) {
            coords.add(new Coordinate(point.getLng(), point.getLat()));
        }
        if (!coords.get(0).equals2D(coords.get(coords.size() - 1))) {
            coords.add(coords.get(0));
        }
        Coordinate[] ring = coords.toArray(new Coordinate[0]);
        LinearRing shell = geometryFactory.createLinearRing(ring);
        Polygon polygon = geometryFactory.createPolygon(shell, null);
        floodZones.add(polygon);
    }

    public boolean isEdgeBlocked(MapNode from, MapNode to) {
        if (floodZones.isEmpty()) {
            return false;
        }
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(from.getLng(), from.getLat()),
                new Coordinate(to.getLng(), to.getLat())
        };
        LineString segment = geometryFactory.createLineString(coords);
        for (Polygon polygon : floodZones) {
            if (polygon.intersects(segment) || polygon.contains(segment)) {
                return true;
            }
        }
        return false;
    }
}