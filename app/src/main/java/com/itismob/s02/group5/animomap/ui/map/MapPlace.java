package com.itismob.s02.group5.animomap.ui.map;

import com.itismob.s02.group5.animomap.R;

import java.util.Locale;

/** A place on the campus map, positioned by latitude and longitude. */
public final class MapPlace {
    public enum Category { BUILDING, FOOD, RESTROOM, STUDY, HANGOUT }
    private final String id, name, shortLabel, subtitle;
    private final Category category;
    private final double latitude, longitude;
    private final boolean mapMarker;
    // Optional details. Empty string / 0 means "not set".
    private final String building, room, description;
    private final int floor;
    // Lower-case text with punctuation removed, built once for searching.
    private final String searchText, searchCompact;

    public MapPlace(String id, String name, String shortLabel, String subtitle,
                    Category category, double latitude, double longitude,
                    boolean mapMarker) {
        this(id, name, shortLabel, subtitle, category, latitude, longitude, mapMarker,
                "", 0, "", "");
    }
    public MapPlace(String id, String name, String shortLabel, String subtitle,
                    Category category, double latitude, double longitude,
                    boolean mapMarker, String building, int floor, String room,
                    String description) {
        this.id = id;
        this.name = name;
        this.shortLabel = shortLabel;
        this.subtitle = subtitle;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mapMarker = mapMarker;
        this.building = building == null ? "" : building;
        this.floor = floor;
        this.room = room == null ? "" : room;
        this.description = description == null ? "" : description;
        this.searchText = normalize(name + " " + shortLabel + " " + subtitle + " "
                + this.building + " " + this.room);
        this.searchCompact = searchText.replace(" ", "");
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getShortLabel() { return shortLabel; }
    public String getSubtitle() { return subtitle; }
    public Category getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean hasMapMarker() { return mapMarker; }
    /** Name of the building this place is in, or "" for outdoor places and buildings themselves. */
    public String getBuilding() { return building; }
    /** Floor number inside the building, or 0 when not applicable. */
    public int getFloor() { return floor; }
    /** Room number such as "G301", or "" when this place is not a room. */
    public String getRoom() { return room; }
    public String getDescription() { return description; }

    /** Short "Building · Floor" text for search results and the place card. */
    public String getLocationLine() {
        StringBuilder line = new StringBuilder();
        if (!building.isEmpty()) { line.append(building); }
        if (floor > 0) {
            if (line.length() > 0) { line.append(" · "); }
            line.append("Floor ").append(floor);
        }
        return line.length() > 0 ? line.toString() : subtitle;
    }

    /** String resource for the category name, such as "Restroom". */
    public static int categoryLabelRes(Category category) {
        switch (category) {
            case FOOD: return R.string.place_type_food;
            case RESTROOM: return R.string.place_type_restroom;
            case STUDY: return R.string.place_type_study;
            case HANGOUT: return R.string.place_type_hangout;
            default: return R.string.place_type_building;
        }
    }
    /** Drawable resource for the category symbol used on markers and search results. */
    public static int glyphRes(Category category) {
        switch (category) {
            case FOOD: return R.drawable.ic_marker_food;
            case RESTROOM: return R.drawable.ic_marker_restroom;
            case STUDY: return R.drawable.ic_marker_study;
            case HANGOUT: return R.drawable.ic_marker_hangout;
            default: return R.drawable.ic_marker_building;
        }
    }

    /** Lower-case, punctuation turned into spaces, repeated spaces removed: "St. La-Salle" -> "st la salle". */
    static String normalize(String text) {
        if (text == null) { return ""; }
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    /**
     * True when every word the user typed appears somewhere in this place's name, label, subtitle,
     * building or room, in any order and ignoring punctuation and case. "st la salle" finds
     * "St. La Salle Hall", "henry sy hall" finds "Henry Sy Sr. Hall", and "g 301" finds room "G301".
     */
    public boolean matches(String query) {
        String value = normalize(query);
        if (value.isEmpty()) { return true; }
        boolean allWords = true;
        for (String word : value.split(" ")) {
            if (!searchText.contains(word)) { allWords = false; break; }
        }
        return allWords || searchCompact.contains(value.replace(" ", ""));
    }

    /**
     * How well this place matches a search; lower is better, {@link #NO_MATCH} means it does not match.
     * 0 exact name, label or room; 1 name starts with the search; 2 every typed word starts a word in
     * the name or label; 3 the name or label contains the words; 4 matched only through the room,
     * building or subtitle.
     */
    public int searchRank(String query) {
        if (!matches(query)) { return NO_MATCH; }
        String value = normalize(query);
        if (value.isEmpty()) { return 4; }
        String normName = normalize(name), normLabel = normalize(shortLabel);
        if (value.equals(normName) || value.equals(normLabel) || value.equals(normalize(room))) { return 0; }
        if (normName.startsWith(value) || normLabel.startsWith(value)) { return 1; }
        String nameAndLabel = " " + normName + " " + normLabel;
        boolean wordStarts = true, contains = true;
        for (String word : value.split(" ")) {
            if (!nameAndLabel.contains(" " + word)) { wordStarts = false; }
            if (!nameAndLabel.contains(word)) { contains = false; }
        }
        return wordStarts ? 2 : contains ? 3 : 4;
    }
    public static final int NO_MATCH = Integer.MAX_VALUE;
}
