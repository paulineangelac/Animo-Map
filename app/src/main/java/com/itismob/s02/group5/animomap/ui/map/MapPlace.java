package com.itismob.s02.group5.animomap.ui.map;

import java.util.Locale;

/** A local map entry used by the map screen prototype. Coordinates are normalized to the map image. */
public final class MapPlace {

    public enum Category {
        BUILDING,
        STUDY,
        FOOD
    }

    private final String name;
    private final String subtitle;
    private final Category category;
    private final float mapX;
    private final float mapY;

    public MapPlace(String name, String subtitle, Category category, float mapX, float mapY) {
        this.name = name;
        this.subtitle = subtitle;
        this.category = category;
        this.mapX = mapX;
        this.mapY = mapY;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Category getCategory() {
        return category;
    }

    public float getMapX() {
        return mapX;
    }

    public float getMapY() {
        return mapY;
    }

    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        return name.toLowerCase(Locale.ROOT).contains(normalizedQuery)
                || subtitle.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }
}
