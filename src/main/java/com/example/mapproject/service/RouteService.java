package com.example.mapproject.service;

import com.example.mapproject.model.Point;
import com.example.mapproject.model.RouteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GraphHopper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.config.CHProfile;
import com.graphhopper.json.Statement;
import com.graphhopper.util.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteService {
    private final GraphHopper hopper;
    private PathService pathService;
    private final ObjectMapper mapper = new ObjectMapper();
    private CustomModel floodedModel = new CustomModel();
    private int numberOfFloodZone = 0;
    JsonFeatureCollection fc = new JsonFeatureCollection();

    public RouteService() {
        hopper = new GraphHopper();
        hopper.setOSMFile("src/main/resources/map-data/Phuong_VinhHung.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");
        // Dùng weighting = custom
        Profile profile = new Profile("car")
                .setVehicle("car")
                .setWeighting("custom")
                .setCustomModel(new CustomModel()); // mặc định
        hopper.setProfiles(profile);


        // Bật Contraction Hierarchies
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

        hopper.importOrLoad();

//        pathService = new PathService(hopper.getBaseGraph());
//        System.out.println(pathService.getPathInfo());
//        System.out.println(hopper.getBaseGraph());
    }


    public RouteResponse findShortestPath(Point from, Point to) {

        GHRequest req = new GHRequest(from.getLat(), from.getLng(),
                to.getLat(), to.getLng())
                .setProfile("car");

        req.putHint("ch.disable", true);

        if (floodedModel != null) {
            System.out.println(floodedModel.toString());
            req.setCustomModel(floodedModel);
        }
        GHResponse rsp = hopper.route(req);

        if (rsp.hasErrors()) {
            throw new RuntimeException(rsp.getErrors().toString());
        }

        ResponsePath path = rsp.getBest();
        double distance = path.getDistance(); // mét

        // Lấy polyline
        PointList ghPoints = path.getPoints();
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < ghPoints.size(); i++) {
            points.add(new Point(ghPoints.getLat(i), ghPoints.getLon(i)));
        }

        return new RouteResponse(distance, points);
    }

    public void setFloodedArea(List<Point> coords) {
        // 1) Tạo Polygon JTS từ danh sách lat/lng của client (chú ý: lon = x, lat = y)
        GeometryFactory gf = new GeometryFactory();
        List<Coordinate> cs = new ArrayList<>();
        for (Point p : coords) cs.add(new Coordinate(p.getLng(), p.getLat()));
        // đóng vòng nếu cần
        if (cs.isEmpty() || !cs.get(0).equals(cs.get(cs.size()-1))) cs.add(cs.get(0));
        Coordinate[] ring = cs.toArray(new Coordinate[0]);
        LinearRing shell = gf.createLinearRing(ring);
        Polygon poly = gf.createPolygon(shell, null);

        // 2) Tạo Feature có id bắt buộc (GraphHopper yêu cầu area phải có "id")
        JsonFeature feature = new JsonFeature();
        feature.setId("flooded_zone_" + numberOfFloodZone);
        feature.setGeometry(poly);
        feature.setProperties(Map.of("name", "flooded_zone"));

        // 3) Bỏ feature vào FeatureCollection
        fc.getFeatures().add(feature);

        // 4) Tạo CustomModel và gán areas = FeatureCollection ở trên
        CustomModel cm = new CustomModel();
        cm.setAreas(fc);

        // 5) Thêm rule: nếu cạnh nằm trong vùng flooded_zone thì nhân 0 (loại bỏ),
        //    còn lại giữ nguyên. Dùng factory methods vì constructor của Statement là private.
        String floodedZoneId = "in_flooded_zone_" + numberOfFloodZone + " == true";
        System.out.println("flooded zone id: " + floodedZoneId);
        cm.getPriority().add(Statement.If(floodedZoneId, Statement.Op.MULTIPLY, "0.0"));
        cm.getPriority().add(Statement.Else(Statement.Op.MULTIPLY, "1.0"));
        // 6) Lưu lại để dùng khi route
        floodedModel = cm;
        numberOfFloodZone++;
    }




}
