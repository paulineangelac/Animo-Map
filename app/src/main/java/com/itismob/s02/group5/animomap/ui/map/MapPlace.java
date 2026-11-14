package com.itismob.s02.group5.animomap.ui.map;

import java.util.Locale;

/** A place on the campus map, positioned by latitude and longitude. */
public final class MapPlace {
    public enum Category { BUILDING, STUDY, FOOD }
    private final String id, name, shortLabel, subtitle;
    private final Category category;
    private final double latitude, longitude;
    private final boolean mapMarker;

    public MapPlace(String id, String name, String shortLabel, String subtitle,
                    Category category, double latitude, double longitude,
                    boolean mapMarker) {
        this.id = id;
        this.name = name;
        this.shortLabel = shortLabel;
        this.subtitle = subtitle;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mapMarker = mapMarker;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getShortLabel() { return shortLabel; }
    public String getSubtitle() { return subtitle; }
    public Category getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean hasMapMarker() { return mapMarker; }

    public boolean matches(String query) {
        String value = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return value.isEmpty() || name.toLowerCase(Locale.ROOT).contains(value)
                || shortLabel.toLowerCase(Locale.ROOT).contains(value)
                || subtitle.toLowerCase(Locale.ROOT).contains(value);
    }
}
