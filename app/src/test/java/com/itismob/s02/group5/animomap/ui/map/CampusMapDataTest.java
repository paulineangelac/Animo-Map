package com.itismob.s02.group5.animomap.ui.map;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class CampusMapDataTest {
    @Test public void referenceCornersRotateClockwise() {
        assertArrayEquals(new float[] {1, 0}, CampusMapData.portraitPoint(0, 0), .0001f);
        assertArrayEquals(new float[] {0, 1}, CampusMapData.portraitPoint(1, 1), .0001f);
        assertArrayEquals(new float[] {.3f, .2f}, CampusMapData.portraitPoint(.2f, .7f), .0001f);
    }
    @Test public void catalogHasUniqueIdsAndValidAnchors() {
        Set<String> ids = new HashSet<>();
        int overview = 0;
        for (MapPlace place : CampusMapData.getPlaces()) {
            assertTrue(ids.add(place.getId()));
            assertTrue(place.getMapX() >= 0 && place.getMapX() <= 1);
            assertTrue(place.getMapY() >= 0 && place.getMapY() <= 1);
            assertFalse(place.getName().isEmpty());
            if (place.hasOverviewLabel()) { overview++; }
        }
        assertEquals(19, ids.size());
        assertEquals(6, overview);
        MapPlace room = CampusMapData.findPlace("g303");
        MapPlace building = CampusMapData.findPlace("gokongwei");
        assertEquals(building.getMapX(), room.getMapX(), .0001f);
        assertEquals(building.getMapY(), room.getMapY(), .0001f);
        assertFalse(room.hasMapMarker());
    }
    @Test public void namesAliasesAndRoomMetadataAreSearchable() {
        assertTrue(CampusMapData.findPlace(CampusMapData.HENRY_SY).matches(" HENRY "));
        assertTrue(CampusMapData.findPlace("strc").matches("strc"));
        assertTrue(CampusMapData.findPlace("g303").matches("level 3"));
        assertTrue(CampusMapData.findPlace("g303").matches("G303"));
        assertFalse(CampusMapData.findPlace("g303").matches("no-such-room"));
        assertTrue(CampusMapData.findPlace("g303").matches(""));
    }
    @Test public void demoIsLimitedToOnePairAndDoesNotExposeMutablePoints() {
        assertNotNull(CampusMapData.findPlace(CampusMapData.LASALLE));
        for (MapPlace place : CampusMapData.getPlaces()) {
            assertEquals(place.getId().equals(CampusMapData.HENRY_SY),
                    CampusMapData.hasDemoRoute(place.getId()));
        }
        float[][] points = CampusMapData.getDemoRoutePoints();
        for (float[] point : points) {
            assertTrue(point[0] >= 0 && point[0] <= 1);
            assertTrue(point[1] >= 0 && point[1] <= 1);
        }
        // Diagram endpoints stay at the north edges of the named buildings, not arbitrary destinations.
        assertArrayEquals(CampusMapData.portraitPoint(.349f, .519f), points[0], .0001f);
        assertArrayEquals(CampusMapData.portraitPoint(.425f, .456f),
                points[points.length - 1], .0001f);
        points[0][0] = -1;
        assertTrue(CampusMapData.getDemoRoutePoints()[0][0] >= 0);
    }
}
