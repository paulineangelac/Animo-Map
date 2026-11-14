package com.itismob.s02.group5.animomap.ui.map;

import java.util.Locale;

/** A place on the local diagram. Coordinates use the portrait map's 0–1 range. */
public final class MapPlace {
    public enum Category { BUILDING, STUDY, FOOD }
    private final String id, name, shortLabel, subtitle;
    private final Category category;
    private final float mapX, mapY;
    private final boolean overviewLabel, mapMarker;

    public MapPlace(String id, String name, String shortLabel, String subtitle,
                    Category category, float mapX, float mapY,
                    boolean overviewLabel, boolean mapMarker) {
        this.id = id;
        this.name = name;
        this.shortLabel = shortLabel;
        this.subtitle = subtitle;
        this.category = category;
        this.mapX = mapX;
        this.mapY = mapY;
        this.overviewLabel = overviewLabel;
        this.mapMarker = mapMarker;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getShortLabel() { return shortLabel; }
    public String getSubtitle() { return subtitle; }
    public Category getCategory() { return category; }
    public float getMapX() { return mapX; }
    public float getMapY() { return mapY; }
    public boolean hasOverviewLabel() { return overviewLabel; }
    public boolean hasMapMarker() { return mapMarker; }

    public boolean matches(String query) {
        String value = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return value.isEmpty() || name.toLowerCase(Locale.ROOT).contains(value)
                || shortLabel.toLowerCase(Locale.ROOT).contains(value)
                || subtitle.toLowerCase(Locale.ROOT).contains(value);
    }
}
