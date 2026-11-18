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
    @Test public void searchIgnoresPunctuationCaseAndWordOrder() {
        MapPlace lasalle = CampusMapData.findPlace(CampusMapData.LASALLE);
        MapPlace henry = CampusMapData.findPlace(CampusMapData.HENRY_SY);
        assertNotNull(lasalle);
        assertNotNull(henry);
        assertTrue(lasalle.matches("st la salle"));
        assertTrue(lasalle.matches("ST. LA-SALLE hall"));
        assertTrue(lasalle.matches("hall salle"));
        assertTrue(henry.matches("henry sy hall"));
        assertTrue(henry.matches("henrysy"));
        assertFalse(henry.matches("henry gokongwei"));
    }
    @Test public void searchFindsRoomNumbersWithOrWithoutSpace() {
        MapPlace place = room();
        assertTrue(place.matches("G301"));
        assertTrue(place.matches("g 301"));
        assertTrue(place.matches("henry sy 3 g301") || place.matches("henry g301"));
        assertFalse(place.matches("g302"));
    }
    @Test public void blankAndSymbolOnlyQueriesMatchEverything() {
        assertTrue(room().matches(null));
        assertTrue(room().matches("   "));
        assertTrue(room().matches("..."));
    }
    @Test public void betterMatchesRankLower() {
        MapPlace henry = CampusMapData.findPlace(CampusMapData.HENRY_SY);
        MapPlace room = room();
        assertNotNull(henry);
        assertEquals(MapPlace.NO_MATCH, henry.searchRank("zzzz"));
        assertEquals(0, henry.searchRank("Henry Sy"));      // exact short label
        assertEquals(1, henry.searchRank("henry sy sr"));   // name starts with it
        assertEquals(2, henry.searchRank("hall sy"));       // each word starts a name word
        assertEquals(3, henry.searchRank("enry"));          // only contained inside a word
        assertEquals(0, room.searchRank("g301"));           // exact room number
        assertTrue(henry.searchRank("henry") < room.searchRank("henry sy"));
    }
}
