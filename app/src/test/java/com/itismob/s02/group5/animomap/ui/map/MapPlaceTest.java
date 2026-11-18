package com.itismob.s02.group5.animomap.ui.map;

import org.junit.Test;
import static org.junit.Assert.*;

public class MapPlaceTest {
    private static MapPlace room() {
        return new MapPlace("r1", "Restroom (Henry Sy 3F)", "Restroom", "Restroom",
                MapPlace.Category.RESTROOM, 14.5651, 120.9932, false,
                "Henry Sy Sr. Hall", 3, "G301", "Near the elevators");
    }
    @Test public void hasAllFiveCategories() {
        assertEquals(5, MapPlace.Category.values().length);
        assertNotNull(MapPlace.Category.valueOf("RESTROOM"));
        assertNotNull(MapPlace.Category.valueOf("HANGOUT"));
    }
    @Test public void shortConstructorLeavesDetailsEmpty() {
        MapPlace building = CampusMapData.findPlace(CampusMapData.HENRY_SY);
        assertNotNull(building);
        assertEquals("", building.getBuilding());
        assertEquals(0, building.getFloor());
        assertEquals("", building.getRoom());
        assertEquals("", building.getDescription());
        assertEquals("Building", building.getLocationLine());
    }
    @Test public void locationLineShowsBuildingAndFloor() {
        assertEquals("Henry Sy Sr. Hall · Floor 3", room().getLocationLine());
    }
    @Test public void searchMatchesBuildingAndRoomNumber() {
        MapPlace place = room();
        assertTrue(place.matches("henry sy"));
        assertTrue(place.matches("g301"));
        assertFalse(place.matches("g999"));
        assertFalse(place.hasMapMarker());
    }
}
