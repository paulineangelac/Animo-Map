package com.itismob.s02.group5.animomap.ui.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    private static final double[][] CAMPUS_OUTLINE = {
            { 14.563954, 120.9944315 },
            { 14.5679559, 120.9925815 },
            { 14.5676598, 120.9919188 },
            { 14.567305, 120.9920807 },
            { 14.566995, 120.9913424 },
            { 14.5654919, 120.9921558 },
            { 14.5654305, 120.9921509 },
            { 14.5653856, 120.992104 },
            { 14.5653482, 120.9920464 },
            { 14.5653109, 120.9919889 },
            { 14.5652626, 120.9919144 },
            { 14.5652053, 120.9918149 },
            { 14.5651256, 120.9916623 },
            { 14.5649355, 120.9913396 },
            { 14.5649055, 120.9913656 },
            { 14.5648954, 120.9913927 },
            { 14.564899, 120.9914346 },
            { 14.5649015, 120.9915276 },
            { 14.5649004, 120.9917834 },
            { 14.564866, 120.9918917 },
            { 14.5648398, 120.9919367 },
            { 14.5647797, 120.991992 },
            { 14.5647308, 120.9920177 },
            { 14.5646786, 120.9920475 },
            { 14.564411, 120.9921587 },
            { 14.564372, 120.9921826 },
            { 14.564315, 120.9923401 },
            { 14.5643869, 120.9925636 },
            { 14.564388, 120.9926145 },
            { 14.5643768, 120.9926439 },
            { 14.5643468, 120.9927132 },
            { 14.5642795, 120.9927862 },
            { 14.5642382, 120.992845 },
            { 14.5641524, 120.9929489 },
            { 14.5640022, 120.9931067 },
            { 14.5637376, 120.9933589 },
            { 14.5635934, 120.9935112 },
            { 14.5635746, 120.9935372 },
            { 14.5635639, 120.9935704 },
            { 14.5635767, 120.99361 },
            { 14.5635972, 120.9936736 },
            { 14.5636606, 120.9938017 }
    };
    public static final double SOUTH = 14.5628, WEST = 120.9905, NORTH = 14.5688, EAST = 120.9953;
    public static final double MIN_ZOOM = 16.0;
    private static final List<MapPlace> PLACES = createPlaces();
    private CampusMapData() { }

    public static List<MapPlace> getPlaces() { return PLACES; }
    public static double[][] getCampusOutline() {
        double[][] copy = new double[CAMPUS_OUTLINE.length][];
        for (int i = 0; i < copy.length; i++) { copy[i] = CAMPUS_OUTLINE[i].clone(); }
        return copy;
    }
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
        p.add(new MapPlace(id, name, label, "Building", MapPlace.Category.BUILDING, lat, lng, true));
    }
    public static boolean hasDemoRoute(String destinationId) { return HENRY_SY.equals(destinationId); }

    /** {latitude, longitude} pairs for the demo line: a straight line, not a walkable path. */
    public static double[][] getDemoRoutePoints() {
        MapPlace from = Objects.requireNonNull(findPlace(LASALLE));
        MapPlace to = Objects.requireNonNull(findPlace(HENRY_SY));
        return new double[][] {
                { from.getLatitude(), from.getLongitude() },
                { to.getLatitude(), to.getLongitude() }
        };
    }
}
