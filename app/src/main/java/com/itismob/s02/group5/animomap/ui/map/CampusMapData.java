package com.itismob.s02.group5.animomap.ui.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Schematic data traced from drawable/dlsu_map.png; not surveyed or geographic data. */
public final class CampusMapData {
    public static final String LASALLE = "st_lasalle";
    public static final String HENRY_SY = "henry_sy";
    private static final List<MapPlace> PLACES = createPlaces();
    private CampusMapData() { }

    public static float[] portraitPoint(float referenceX, float referenceY) {
        return new float[] { 1f - referenceY, referenceX };
    }
    public static List<MapPlace> getPlaces() { return PLACES; }
    public static MapPlace findPlace(String id) {
        for (MapPlace place : PLACES) {
            if (place.getId().equals(id)) { return place; }
        }
        return null;
    }
    private static List<MapPlace> createPlaces() {
        List<MapPlace> p = new ArrayList<>();
        building(p, LASALLE, "St. La Salle Hall", "La Salle", .218f, .710f, true);
        building(p, "john", "Br. John Hall", "Br. John", .048f, .550f, false);
        building(p, "connon", "Br. Connon Hall", "Connon", .168f, .485f, false);
        building(p, "yuchengco", "Don Enrique T. Yuchengco Hall", "Yuchengco", .268f, .439f, true);
        building(p, HENRY_SY, "Henry Sy Sr. Hall", "Henry Sy", .425f, .560f, true);
        building(p, "faculty", "Faculty Center", "Faculty Center", .420f, .155f, false);
        building(p, "william", "Br. William Hall", "Br. William", .518f, .155f, false);
        building(p, "joseph", "St. Joseph Hall", "St. Joseph", .440f, .290f, false);
        building(p, "bloemen", "Br. Bloemen Hall", "Bloemen", .520f, .415f, false);
        building(p, "velasco", "Velasco Hall", "Velasco", .510f, .680f, true);
        building(p, "miguel", "St. Miguel Hall", "St. Miguel", .600f, .455f, true);
        building(p, "mutien", "St. Mutien Marie Hall", "Mutien Marie", .665f, .360f, false);
        building(p, "strc", "Science and Technology Research Center", "STRC", .787f, .395f, false);
        building(p, "razon", "Enrique M. Razon Sports Center", "Razon", .933f, .423f, false);
        building(p, "gokongwei", "Gokongwei Hall", "Gokongwei", .696f, .750f, true);
        building(p, "andrew", "Br. Andrew Gonzalez Hall", "Br. Andrew", .907f, .740f, false);
        // Rooms are searchable without drawing a duplicate marker over the building.
        add(p, "g303", "G303", "G303", "Gokongwei Hall · Level 3",
                MapPlace.Category.BUILDING, .696f, .750f, false, false);
        add(p, "study_demo", "Study area (demo)", "Study · demo", "Sample study location",
                MapPlace.Category.STUDY, .435f, .585f, false, true);
        add(p, "food_demo", "Food place (demo)", "Food · demo", "Sample food location",
                MapPlace.Category.FOOD, .523f, .425f, false, true);
        return Collections.unmodifiableList(p);
    }
    private static void building(List<MapPlace> p, String id, String name, String label,
                                 float x, float y, boolean overview) {
        add(p, id, name, label, "Building", MapPlace.Category.BUILDING, x, y, overview, true);
    }
    private static void add(List<MapPlace> p, String id, String name, String label,
                            String subtitle, MapPlace.Category category, float x, float y,
                            boolean overview, boolean marker) {
        float[] point = portraitPoint(x, y);
        p.add(new MapPlace(id, name, label, subtitle, category,
                point[0], point[1], overview, marker));
    }
    public static boolean hasDemoRoute(String destinationId) { return HENRY_SY.equals(destinationId); }

    /** Boundary points near the two buildings, not claims about entrances. */
    public static float[][] getDemoRoutePoints() {
        float[][] reference = {
                { .349f, .519f }, { .338f, .519f }, { .338f, .342f },
                { .483f, .342f }, { .483f, .443f }, { .425f, .443f }, { .425f, .456f }
        };
        float[][] portrait = new float[reference.length][];
        for (int i = 0; i < reference.length; i++) {
            portrait[i] = portraitPoint(reference[i][0], reference[i][1]);
        }
        return portrait;
    }
}
