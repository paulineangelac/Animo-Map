package com.itismob.s02.group5.animomap.ui.map;

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

    public boolean matches(String query) {
        String value = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return value.isEmpty() || name.toLowerCase(Locale.ROOT).contains(value)
                || shortLabel.toLowerCase(Locale.ROOT).contains(value)
                || subtitle.toLowerCase(Locale.ROOT).contains(value)
                || building.toLowerCase(Locale.ROOT).contains(value)
                || room.toLowerCase(Locale.ROOT).contains(value);
    }
}
