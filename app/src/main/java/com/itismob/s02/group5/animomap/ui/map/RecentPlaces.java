package com.itismob.s02.group5.animomap.ui.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The places the user recently selected on the map, newest first. Plain Java so it is easy to test. */
public final class RecentPlaces {
    public static final int MAX = 6;
    private final List<MapPlace> places = new ArrayList<>();

    /** Moves the place to the front, keeping at most {@link #MAX}. */
    public void add(MapPlace place) {
        places.remove(place);
        places.add(0, place);
        while (places.size() > MAX) { places.remove(places.size() - 1); }
    }
    public List<MapPlace> getPlaces() { return Collections.unmodifiableList(places); }

    /** Place ids joined by commas, for saving in SharedPreferences. */
    public String encode() {
        StringBuilder ids = new StringBuilder();
        for (MapPlace place : places) {
            if (ids.length() > 0) { ids.append(','); }
            ids.append(place.getId());
        }
        return ids.toString();
    }
    /** Rebuilds the list from {@link #encode()} text; ids that no longer exist are skipped. */
    public static RecentPlaces decode(String saved) {
        RecentPlaces recent = new RecentPlaces();
        if (saved == null || saved.isEmpty()) { return recent; }
        String[] ids = saved.split(",");
        // Saved newest first, so add oldest first to end up in the same order.
        for (int i = ids.length - 1; i >= 0; i--) {
            MapPlace place = CampusMapData.findPlace(ids[i]);
            if (place != null) { recent.add(place); }
        }
        return recent;
    }
}
