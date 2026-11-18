package com.itismob.s02.group5.animomap.ui.map;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class RecentPlacesTest {
    private static MapPlace place(String id) {
        MapPlace place = CampusMapData.findPlace(id);
        assertNotNull(id, place);
        return place;
    }
    @Test public void startsEmpty() {
        assertTrue(new RecentPlaces().getPlaces().isEmpty());
    }
    @Test public void newestFirstWithoutDuplicates() {
        RecentPlaces recent = new RecentPlaces();
        recent.add(place("henry_sy"));
        recent.add(place("gokongwei"));
        recent.add(place("henry_sy"));
        List<MapPlace> places = recent.getPlaces();
        assertEquals(2, places.size());
        assertEquals("henry_sy", places.get(0).getId());
        assertEquals("gokongwei", places.get(1).getId());
    }
    @Test public void keepsOnlyTheMostRecentSix() {
        RecentPlaces recent = new RecentPlaces();
        String[] ids = {"st_lasalle", "connon", "yuchengco", "henry_sy", "velasco", "bloemen", "miguel"};
        for (String id : ids) { recent.add(place(id)); }
        assertEquals(RecentPlaces.MAX, recent.getPlaces().size());
        assertEquals("miguel", recent.getPlaces().get(0).getId());
        assertFalse(recent.getPlaces().contains(place("st_lasalle")));
    }
    @Test public void encodeAndDecodeKeepOrderAndSkipUnknownIds() {
        RecentPlaces recent = new RecentPlaces();
        recent.add(place("connon"));
        recent.add(place("razon"));
        assertEquals("razon,connon", recent.encode());
        RecentPlaces restored = RecentPlaces.decode("razon,no_such_place,connon");
        assertEquals(2, restored.getPlaces().size());
        assertEquals("razon", restored.getPlaces().get(0).getId());
        assertTrue(RecentPlaces.decode(null).getPlaces().isEmpty());
        assertTrue(RecentPlaces.decode("").getPlaces().isEmpty());
    }
}
