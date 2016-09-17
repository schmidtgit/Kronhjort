package Model;

import Controller.Main;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to correctly connect all the coastlineParts.
 * It uses CoastlinePart objects to create the coast and convert them into PolygonApprox objects.
 */
public class CoastBuilder {
    List<PolygonApprox> islands;
    Map<Point2D, CoastlinePart> coastMap;
    float minLat, maxLat, minLon, maxLon;

    /**
     * Sets up the coast builder.
     */
    public CoastBuilder(){
        islands = new ArrayList<>();
        coastMap = new HashMap<>();
    }

    /**
     * Add a coast to the builder.
     * @param way The coast as a list of Point2D's.
     */
    public void coastline(List<Point2D> way) {
        CoastlinePart clp = new CoastlinePart(way);
        Point2D start = clp.startPoint();
        if(!coastMap.containsKey(start)){
            coastMap.put(start, clp);
        }
    }

    /**
     * Returns the current list of complete islands.
     * @return All completed island.
     */
    public List<PolygonApprox> coast(){
        return islands;
    }

    /**
     * Converts all the current CoastlinePart objects into complete islands and adds them to the island list.
     */
    public void sortCoast() {
        if(coastMap.size() == 0){
            return;
        }
        Model m = Main.model();
        minLon = m.minLon();
        maxLon = m.maxLon();
        minLat = m.minLat();
        maxLat = m.maxLat();
        connectCoasts();
        if(coastMap.size() == 0){
            return;
        }
        extendCoasts();
        fixCoast();
        coastMap = new HashMap<>();
    }

    /**
     * Connects all coasts to each other and add all completed islands to the island list.
     */
    private void connectCoasts(){
        Iterator<Point2D> it = coastMap.keySet().iterator();
        List<Point2D> usedKeys = new ArrayList<>();
        while(it.hasNext()) {
            Point2D key = it.next();
            if (usedKeys.contains(key)) {
                continue;
            }
            CoastlinePart cl = coastMap.get(key);
            Point2D current = cl.getCurrentPoint();
            boolean connected = cl.complete();
            while (!connected) {
                if (cl.complete()) {
                    connected = true;
                    continue;
                }
                if (coastMap.containsKey(current) && !usedKeys.contains(current)) {
                    CoastlinePart cc = coastMap.get(current);
                    usedKeys.add(current);
                    cl.append(cc, true);
                    current = cl.getCurrentPoint();
                } else {
                    break;
                }
            }
            if (connected) {
                usedKeys.add(cl.startPoint());
                add(cl);
            }
        }
        for(Point2D p: usedKeys){
            coastMap.remove(p);
        }
    }

    /**
     * Extends all coasts that end inside the bounds of the map to end just outside the bounds.
     */
    private void extendCoasts(){
        Iterator<Point2D> it = coastMap.keySet().iterator();
        while(it.hasNext()){
            Point2D key = it.next();
            CoastlinePart cl = coastMap.get(key);
            switch (extendWay(cl.startPoint())){
                case "up":
                    cl.falseStart((float)cl.startPoint().getX(), maxLat - 0.01f);
                    break;
                case "down":
                    cl.falseStart((float)cl.startPoint().getX(), minLat + 0.01f);
                    break;
                case "right":
                    cl.falseStart(maxLon + 0.01f, (float) cl.startPoint().getY());
                    break;
                case "left":
                    cl.falseStart(minLon - 0.01f, (float)cl.startPoint().getY());
                    break;
                default:
                    break;
            }
            switch (extendWay(cl.getCurrentPoint())){
                case "up":
                    cl.lineTo(cl.getCurrentPoint().getX(), maxLat - 0.01f);
                    break;
                case "down":
                    cl.lineTo(cl.getCurrentPoint().getX(), minLat + 0.01f);
                    break;
                case "right":
                    cl.lineTo(maxLon + 0.01f, cl.getCurrentPoint().getY());
                    break;
                case "left":
                    cl.lineTo(minLon - 0.01f, cl.getCurrentPoint().getY());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Calculates which side of the bounds the given point is closest to
     * @param point The Point2D from which an extension is needed
     * @return Describes which way to extend path outside the bounds of the map. "stay" is returned if the point is outside the map.
     */
    private String extendWay(Point2D point){
        double pointX = point.getX();
        double pointY = point.getY();
        if(pointX > minLon && pointX < maxLon && pointY < minLat && pointY > maxLat){
            if(Math.min(Math.abs(pointY-minLat),Math.abs(pointY-maxLat)) < Math.min(Math.abs(pointX-minLon), Math.abs(pointX-maxLon))) {
                if(Math.abs(pointY-minLat) < Math.abs(pointY-maxLat)){
                    return "down";
                } else {
                    return "up";
                }
            } else {
                if(Math.abs(pointX-minLon) < Math.abs(pointX-maxLon)){
                    return "left";
                } else {
                    return "right";
                }
            }
        }
        return "stay";
    }

    /**
     * Connects the remaining coasts to each other by going counter clockwise around the bounds of the map.
     */
    private void fixCoast() {
        Iterator<Point2D> it = coastMap.keySet().iterator();
        List<CoastlinePart> startAbove = new ArrayList<>();
        List<CoastlinePart> startBelow = new ArrayList<>();
        List<CoastlinePart> startLeft = new ArrayList<>();
        List<CoastlinePart> startRight = new ArrayList<>();
        while (it.hasNext()) {
            CoastlinePart cl = coastMap.get(it.next());
            Point2D start = cl.startPoint();
            if (start.getX() < minLon) {
                startLeft.add(cl);
            }
            if (start.getX() > maxLon) {
                startRight.add(cl);
            }
            if (start.getY() < maxLat) {
                startAbove.add(cl);
            }
            if (start.getY() > minLat){
                startBelow.add(cl);
            }
        }

        it = coastMap.keySet().iterator();
        List<CoastlinePart> used = new ArrayList<>();
        while (it.hasNext()) {
            Point2D key = it.next();
            if (used.contains(coastMap.get(key))) {
                continue;
            }
            CoastlinePart cl = coastMap.get(key);
            int cornerID = 0;
            Point2D current = cl.getCurrentPoint();
            while (!cl.complete()) {
                CoastlinePart next = null;
                if (current.getX() <= minLon && cornerID != 1) {
                    for (CoastlinePart cc : startLeft) {
                        if (!used.contains(cc) && current.getY() <= cc.startPoint().getY()) {
                            if (next == null || next.startPoint().getY() > cc.startPoint().getY()) {
                                next = cc;
                            }
                        }
                    }
                    if (next != null) {
                        if (next.equals(cl)) {
                            cl.lineTo(next.startPoint().getX(), next.startPoint().getY());
                        } else {
                            cl.append(next, true);
                        }
                        used.add(next);
                    } else {
                        cl.lineTo(minLon - 0.1f, minLat + 0.1f);
                        cornerID = 1;
                    }
                } else if (current.getY() >= minLat && cornerID != 2) {
                    for (CoastlinePart cc : startBelow) {
                        if (!used.contains(cc) && current.getX() <= cc.startPoint().getX()) {
                            if (next == null || next.startPoint().getX() > cc.startPoint().getX()) {
                                next = cc;
                            }
                        }
                    }
                    if (next != null) {
                        if (next.equals(cl)) {
                            cl.lineTo(next.startPoint().getX(), next.startPoint().getY());
                        } else {
                            cl.append(next, true);
                        }
                        used.add(next);
                    } else {
                        cl.lineTo(maxLon + 0.1f, minLat + 0.1f);
                        cornerID = 2;
                    }
                } else if (current.getX() >= maxLon && cornerID != 3) {
                    for (CoastlinePart cc : startRight) {
                        if (!used.contains(cc) && current.getY() >= cc.startPoint().getY()) {
                            if (next == null || next.startPoint().getY() < cc.startPoint().getY()) {
                                next = cc;
                            }
                        }
                    }
                    if (next != null) {
                        if (next.equals(cl)) {
                            cl.lineTo(next.startPoint().getX(), next.startPoint().getY());
                        } else {
                            cl.append(next, true);
                        }
                        used.add(next);
                    } else {
                        cl.lineTo(maxLon + 0.1f, maxLat - 0.1f);
                        cornerID = 3;
                    }
                } else if (current.getY() <= maxLat && cornerID != 4) {
                    for (CoastlinePart cc : startAbove) {
                        if (!used.contains(cc) && current.getX() >= cc.startPoint().getX()) {
                            if (next == null || next.startPoint().getX() < cc.startPoint().getX()) {
                                next = cc;
                            }
                        }
                    }
                    if (next != null) {
                        if (next.equals(cl)) {
                            cl.lineTo(next.startPoint().getX(), next.startPoint().getY());
                        } else {
                            cl.append(next, true);
                        }
                        used.add(next);
                    } else {
                        cl.lineTo(minLon - 0.1f, maxLat - 0.1f);
                        cornerID = 4;
                    }
                } else {
                    cl.lineTo(cl.startPoint().getX(), cl.startPoint().getY());
                }
                current = cl.getCurrentPoint();
            }
            add(cl);
        }
    }

    /**
     * Converts a CoastlinePart to a PolygonApprox and adds it to the island list.
     * @param cl The CoastlinePart to be added.
     */
    private void add(CoastlinePart cl){
        PolygonApprox pa = cl.toPolygonApprox();
        islands.add(pa);
    }
}
