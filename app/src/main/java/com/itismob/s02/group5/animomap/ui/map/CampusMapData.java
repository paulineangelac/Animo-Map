package com.itismob.s02.group5.animomap.ui.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
    /** True when the coordinates are inside the map's campus bounds. */
    public static boolean isOnCampus(double latitude, double longitude) {
        return latitude >= SOUTH && latitude <= NORTH && longitude >= WEST && longitude <= EAST;
    }
    public static double[][] getCampusOutline() {
        double[][] copy = new double[CAMPUS_OUTLINE.length][];
        for (int i = 0; i < copy.length; i++) { copy[i] = CAMPUS_OUTLINE[i].clone(); }
        return copy;
    }
    /**
     * Whether a place gets a marker on the map right now. The selected place is always shown, even
     * if it normally has no marker (a room or an indoor restroom), so the user sees where it is.
     * Other places need a marker and must pass the category filter. In demo-route mode only the
     * selected place and the route's starting building are shown. When the selected place is a room
     * or indoor place (no marker of its own), it is the only place shown.
     */
    public static boolean isShownOnMap(MapPlace place, MapPlace selected,
                                       MapPlace.Category filter, boolean demo) {
        if (place == selected && place != null) { return true; }
        if (!place.hasMapMarker()) { return false; }
        // A selected room or indoor place is shown alone, so it is not lost among other markers.
        if (selected != null && !selected.hasMapMarker()) { return false; }
        if (demo) { return LASALLE.equals(place.getId()); }
        return filter == null || filter == place.getCategory();
    }
    public static MapPlace findPlace(String id) {
        for (MapPlace place : PLACES) {
            if (place.getId().equals(id)) { return place; }
        }
        return null;
    }
    private static List<MapPlace> createPlaces() {
        List<MapPlace> p = new ArrayList<>();
        // Building coordinates supplied by the team. Add more with building(...).
        building(p, LASALLE, "St. La Salle Hall", "La Salle", 14.564366010195952, 120.99383371430477);
        building(p, "connon", "Br. Connon Hall", "Connon", 14.5639910530407, 120.99335203434539);
        building(p, "yuchengco", "Yuchengco Hall", "Yuchengco", 14.564356560750333, 120.99315698814179);
        building(p, HENRY_SY, "Henry Sy Sr. Hall", "Henry Sy", 14.565000549063477, 120.99309197273723);
        building(p, "velasco", "Velasco Hall", "Velasco", 14.565497262251304, 120.99324690305419);
        building(p, "bloemen", "Bloemen Hall", "Bloemen", 14.565295096106826, 120.9926631477404);
        building(p, "miguel", "Miguel Hall", "Miguel", 14.565628469329429, 120.99256908290198);
        building(p, "gokongwei", "Gokongwei Hall", "Gokongwei", 14.566266710334041, 120.99299380292196);
        building(p, "science_tech", "Science and Technology Research Center", "STRC", 14.56635217686468, 120.99208314727609);
        building(p, "razon", "Razon Sports Center", "Razon", 14.56701420145231, 120.9920648806158);
        building(p, "andrew", "Br. Andrew Gonzalez Hall", "Andrew", 14.56705284665336, 120.99261117849204);
        building(p, "faculty", "Faculty Center", "Faculty", 14.564665836048386, 120.99234498725808);
        building(p, "st_joseph", "St. Joseph Hall", "St. Joseph", 14.564896118661604, 120.9925912158032);
        // Points of interest. Coordinates are approximate (placed beside nearby buildings), so
        // adjust them once the team has collected exact positions.
        // Agno is a standalone food court between STRC and Miguel Hall, not inside a building.
        p.add(new MapPlace("agno_food_court", "Agno Food Court", "Agno", "Between STRC and Miguel Hall",
                MapPlace.Category.FOOD, 14.56599, 120.99233, true, "", 0, "",
                "Food stalls and seating for a quick meal between classes."));
        poi(p, "bloemen_food", "Bloemen Food Stalls", "Bloemen Food", MapPlace.Category.FOOD,
                14.56508, 120.99282, "Bloemen Hall",
                "Snacks and drinks near Bloemen Hall.");
        poi(p, "gokongwei_restroom", "Gokongwei Hall Restrooms", "Gokongwei WC", MapPlace.Category.RESTROOM,
                14.56610, 120.99312, "Gokongwei Hall",
                "Restrooms at the entrance of Gokongwei Hall.");
        poi(p, "yuchengco_restroom", "Yuchengco Hall Restrooms", "Yuchengco WC", MapPlace.Category.RESTROOM,
                14.56450, 120.99325, "Yuchengco Hall",
                "Restrooms at the entrance of Yuchengco Hall.");
        poi(p, "henry_sy_library", "Henry Sy Sr. Hall Library", "Library", MapPlace.Category.STUDY,
                14.56515, 120.99318, "Henry Sy Sr. Hall",
                "Quiet reading and group study spaces.");
        poi(p, "faculty_study", "Faculty Center Study Area", "Faculty Study", MapPlace.Category.STUDY,
                14.56480, 120.99245, "Faculty Center",
                "Tables and seats for studying near the Faculty Center.");
        poi(p, "velasco_patio", "Velasco Hall Patio", "Velasco Patio", MapPlace.Category.HANGOUT,
                14.56535, 120.99335, "Velasco Hall",
                "Open patio for waiting and hanging out with friends.");
        poi(p, "andrew_lobby", "Br. Andrew Gonzalez Hall Lobby", "Andrew Lobby", MapPlace.Category.HANGOUT,
                14.56690, 120.99270, "Br. Andrew Gonzalez Hall",
                "Lobby seating for waiting between classes.");
        // PLACEHOLDER indoor places (classrooms and indoor facilities) so search by room number and
        // building/floor display can be tested. Replace with real room data when it is collected.
        room(p, "gokongwei", "G301", 3, MapPlace.Category.BUILDING, "Classroom");
        room(p, "gokongwei", "G210", 2, MapPlace.Category.BUILDING, "Classroom");
        room(p, HENRY_SY, "H305", 3, MapPlace.Category.BUILDING, "Classroom");
        room(p, "velasco", "V204", 2, MapPlace.Category.BUILDING, "Classroom");
        room(p, "yuchengco", "Y402", 4, MapPlace.Category.BUILDING, "Classroom");
        room(p, "andrew", "A1102", 11, MapPlace.Category.BUILDING, "Classroom");
        indoor(p, "henry_sy_restroom_3f", HENRY_SY, 3, MapPlace.Category.RESTROOM,
                "Restroom", "Restroom (Henry Sy 3F)");
        indoor(p, "gokongwei_restroom_2f", "gokongwei", 2, MapPlace.Category.RESTROOM,
                "Restroom", "Restroom (Gokongwei 2F)");
        indoor(p, "yuchengco_study_5f", "yuchengco", 5, MapPlace.Category.STUDY,
                "Study Area", "Study Area (Yuchengco 5F)");
        return Collections.unmodifiableList(p);
    }
    /** Placeholder classroom inside a building: no marker of its own, so it sits at the building's position. */
    private static void room(List<MapPlace> p, String buildingId, String roomNumber, int floor,
                             MapPlace.Category category, String kind) {
        MapPlace b = Objects.requireNonNull(findIn(p, buildingId));
        p.add(new MapPlace(buildingId + "_" + roomNumber.toLowerCase(Locale.ROOT),
                b.getShortLabel() + " " + roomNumber, roomNumber, kind, category,
                b.getLatitude(), b.getLongitude(), false, b.getName(), floor, roomNumber,
                "Placeholder " + kind.toLowerCase(Locale.ROOT) + " data."));
    }
    /** Placeholder indoor facility (restroom, study area) on a floor of a building, without its own marker. */
    private static void indoor(List<MapPlace> p, String id, String buildingId, int floor,
                               MapPlace.Category category, String label, String name) {
        MapPlace b = Objects.requireNonNull(findIn(p, buildingId));
        p.add(new MapPlace(id, name, label, label, category, b.getLatitude(), b.getLongitude(),
                false, b.getName(), floor, "", "Placeholder " + label.toLowerCase(Locale.ROOT) + " data."));
    }
    private static MapPlace findIn(List<MapPlace> list, String id) {
        for (MapPlace place : list) {
            if (place.getId().equals(id)) { return place; }
        }
        return null;
    }
    private static void building(List<MapPlace> p, String id, String name, String label,
                                 double lat, double lng) {
        p.add(new MapPlace(id, name, label, "Building", MapPlace.Category.BUILDING, lat, lng, true));
    }
    /** An outdoor point of interest (food, restroom, study or hangout) shown with its own marker. */
    private static void poi(List<MapPlace> p, String id, String name, String label,
                            MapPlace.Category category, double lat, double lng,
                            String nearBuilding, String description) {
        p.add(new MapPlace(id, name, label, nearBuilding, category, lat, lng, true,
                nearBuilding, 0, "", description));
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
