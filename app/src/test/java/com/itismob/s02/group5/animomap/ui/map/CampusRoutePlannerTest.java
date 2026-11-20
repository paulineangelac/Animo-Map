package com.itismob.s02.group5.animomap.ui.map;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

public class CampusRoutePlannerTest {

    @Test
    public void testDistanceAndWalkingTimeCalculation() {
        MapPlace lasalle = CampusMapData.findPlace(CampusMapData.LASALLE);
        MapPlace henry = CampusMapData.findPlace(CampusMapData.HENRY_SY);
        assertNotNull(lasalle);
        assertNotNull(henry);

        double distance = CampusRoutePlanner.calculateDistanceMeters(
                lasalle.getLatitude(), lasalle.getLongitude(),
                henry.getLatitude(), henry.getLongitude()
        );

        assertTrue("Distance should be greater than 0", distance > 0);

        int minutes = CampusRoutePlanner.estimateWalkingMinutes(distance);
        assertTrue("Walking minutes should be at least 1", minutes >= 1);
    }

    @Test
    public void testCampusAnd10KmNearbyAreaValidation() {
        // DLSU Manila center should be within campus or nearby
        assertTrue(CampusRoutePlanner.isWithinCampusOrNearby(
                CampusRoutePlanner.CAMPUS_CENTER_LAT,
                CampusRoutePlanner.CAMPUS_CENTER_LNG
        ));

        // A point far away (e.g. Tokyo or Cebu) should not be within 10km of DLSU Manila
        assertFalse(CampusRoutePlanner.isWithinCampusOrNearby(10.3157, 123.8854));
    }

    @Test
    public void testGetRouteOptionsProvidesShortestAndAlternative() {
        MapPlace lasalle = CampusMapData.findPlace(CampusMapData.LASALLE);
        MapPlace henry = CampusMapData.findPlace(CampusMapData.HENRY_SY);
        assertNotNull(lasalle);
        assertNotNull(henry);

        List<RouteOption> routes = CampusRoutePlanner.getRouteOptions(lasalle, henry);
        assertNotNull(routes);
        assertFalse(routes.isEmpty());

        // First route should be shortest
        RouteOption shortest = routes.get(0);
        assertEquals("shortest", shortest.getId());
        assertNotNull(shortest.getFormattedDistance());
        assertNotNull(shortest.getFormattedTime());
        assertTrue(shortest.getStairCount() >= 0);
        assertFalse(shortest.getPoints().isEmpty());

        // If alternative exists
        if (routes.size() > 1) {
            RouteOption alt = routes.get(1);
            assertEquals("fewer_stairs", alt.getId());
            assertTrue("Alternative route distance should be >= shortest", alt.getDistanceMeters() >= shortest.getDistanceMeters());
            assertTrue(alt.getStairCount() < shortest.getStairCount());
        }
    }
}
