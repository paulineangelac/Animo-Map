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
        assertEquals(21, ids.size());
        assertTrue(onCampus(CampusMapData.CENTER_LAT, CampusMapData.CENTER_LNG));
    }
    @Test public void everyCategoryHasAtLeastOneMarkerPlace() {
        for (MapPlace.Category category : MapPlace.Category.values()) {
            boolean found = false;
            for (MapPlace place : CampusMapData.getPlaces()) {
                if (place.getCategory() == category && place.hasMapMarker()) { found = true; }
            }
            assertTrue(category + " has no marker place", found);
        }
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
    @Test public void isOnCampusMatchesBoundsAndManualLocationIsRemembered() {
        assertTrue(CampusMapData.isOnCampus(CampusMapData.CENTER_LAT, CampusMapData.CENTER_LNG));
        assertFalse(CampusMapData.isOnCampus(14.6, 121.0));
        UserLocation.clear();
        assertNull(UserLocation.getManualPlace());
        UserLocation.setManualPlaceId(CampusMapData.HENRY_SY);
        assertEquals(CampusMapData.HENRY_SY, UserLocation.getManualPlace().getId());
        UserLocation.clear();
        assertNull(UserLocation.getManualPlaceId());
    }
    @Test public void selectedPlaceWithoutMarkerIsStillShown() {
        MapPlace restroom = new MapPlace("wc1", "Restroom (Henry Sy 3F)", "Restroom", "Restroom",
                MapPlace.Category.RESTROOM, 14.5650, 120.9931, false, "Henry Sy Sr. Hall", 3, "", "");
        assertFalse(CampusMapData.isShownOnMap(restroom, null, null, false));
        assertTrue(CampusMapData.isShownOnMap(restroom, restroom, null, false));
        // Even with a filter for another category, the selected place stays visible.
        assertTrue(CampusMapData.isShownOnMap(restroom, restroom, MapPlace.Category.FOOD, false));
    }
    @Test public void categoryFilterAndDemoModeControlWhichMarkersShow() {
        MapPlace lasalle = CampusMapData.findPlace(CampusMapData.LASALLE);
        MapPlace henry = CampusMapData.findPlace(CampusMapData.HENRY_SY);
        assertTrue(CampusMapData.isShownOnMap(henry, null, null, false));
        assertTrue(CampusMapData.isShownOnMap(henry, null, MapPlace.Category.BUILDING, false));
        assertFalse(CampusMapData.isShownOnMap(henry, null, MapPlace.Category.FOOD, false));
        // Demo route: only the selected building and the starting building.
        assertTrue(CampusMapData.isShownOnMap(lasalle, henry, null, true));
        assertTrue(CampusMapData.isShownOnMap(henry, henry, null, true));
        assertFalse(CampusMapData.isShownOnMap(CampusMapData.findPlace("gokongwei"), henry, null, true));
    }
}
