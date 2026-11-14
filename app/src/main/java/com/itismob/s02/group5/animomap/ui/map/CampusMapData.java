package com.itismob.s02.group5.animomap.ui.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Campus places as latitude/longitude pairs.
 * Building coordinates supplied by the team. Add more buildings in createPlaces() as they are collected.
 */
public final class CampusMapData {
    public static final String LASALLE = "st_lasalle";
    public static final String HENRY_SY = "henry_sy";
    /** Initial camera target (roughly the middle of the campus) and zoom. */
    public static final double CENTER_LAT = 14.564794, CENTER_LNG = 120.993346;
    public static final double OVERVIEW_ZOOM = 17.2;
    /** The map cannot be dragged outside these bounds. */
    public static final double SOUTH = 14.5620, WEST = 120.9895, NORTH = 14.5720, EAST = 120.9975;
    private static final List<MapPlace> PLACES = createPlaces();
    private CampusMapData() { }

    public static List<MapPlace> getPlaces() { return PLACES; }
    public static MapPlace findPlace(String id) {
        for (MapPlace place : PLACES) {
            if (place.getId().equals(id)) { return place; }
        }
        return null;
    }
    private static List<MapPlace> createPlaces() {
        List<MapPlace> p = new ArrayList<>();
        // Only these two buildings are on the map for now. Add more with building(...).
        building(p, LASALLE, "St. La Salle Hall", "La Salle", 14.564442015164635, 120.99351319661409);
        building(p, HENRY_SY, "Henry Sy Sr. Hall", "Henry Sy", 14.565146666735775, 120.99317868127247);
        return Collections.unmodifiableList(p);
    }
    private static void building(List<MapPlace> p, String id, String name, String label,
                                 double lat, double lng) {
        add(p, id, name, label, "Building", MapPlace.Category.BUILDING, lat, lng, true);
    }
    private static void add(List<MapPlace> p, String id, String name, String label,
                            String subtitle, MapPlace.Category category, double lat, double lng,
                            boolean marker) {
        p.add(new MapPlace(id, name, label, subtitle, category, lat, lng, marker));
    }
    public static boolean hasDemoRoute(String destinationId) { return HENRY_SY.equals(destinationId); }

    /** {latitude, longitude} pairs for the demo line: a straight line, not a walkable path. */
    public static double[][] getDemoRoutePoints() {
        MapPlace from = findPlace(LASALLE), to = findPlace(HENRY_SY);
        return new double[][] {
                { from.getLatitude(), from.getLongitude() },
                { to.getLatitude(), to.getLongitude() }
        };
    }
}
