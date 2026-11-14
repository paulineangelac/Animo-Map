package com.itismob.s02.group5.animomap.ui.map;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class CampusMapDataTest {
    private static boolean onCampus(double lat, double lng) {
        return lat >= CampusMapData.SOUTH && lat <= CampusMapData.NORTH
                && lng >= CampusMapData.WEST && lng <= CampusMapData.EAST;
    }
    @Test public void catalogHasUniqueIdsAndCoordinatesInsideCampusBounds() {
        Set<String> ids = new HashSet<>();
        for (MapPlace place : CampusMapData.getPlaces()) {
            assertTrue(ids.add(place.getId()));
            assertTrue(place.getName() + " is outside the campus bounds",
                    onCampus(place.getLatitude(), place.getLongitude()));
            assertFalse(place.getName().isEmpty());
        }
        assertEquals(2, ids.size());
        assertTrue(onCampus(CampusMapData.CENTER_LAT, CampusMapData.CENTER_LNG));
    }
    @Test public void namesAndAliasesAreSearchable() {
        MapPlace henry = CampusMapData.findPlace(CampusMapData.HENRY_SY);
        MapPlace lasalle = CampusMapData.findPlace(CampusMapData.LASALLE);
        assertNotNull(henry);
        assertNotNull(lasalle);
        assertTrue(henry.matches(" HENRY "));
        assertTrue(lasalle.matches("la salle"));
        assertFalse(lasalle.matches("no-such-place"));
        assertTrue(lasalle.matches(""));
    }
    @Test public void demoIsLimitedToOnePairAndDoesNotExposeMutablePoints() {
        assertNotNull(CampusMapData.findPlace(CampusMapData.LASALLE));
        for (MapPlace place : CampusMapData.getPlaces()) {
            assertEquals(place.getId().equals(CampusMapData.HENRY_SY),
                    CampusMapData.hasDemoRoute(place.getId()));
        }
        double[][] points = CampusMapData.getDemoRoutePoints();
        for (double[] point : points) { assertTrue(onCampus(point[0], point[1])); }
        double original = points[0][0];
        points[0][0] = -1;
        assertEquals(original, CampusMapData.getDemoRoutePoints()[0][0], 1e-9);
    }
}
