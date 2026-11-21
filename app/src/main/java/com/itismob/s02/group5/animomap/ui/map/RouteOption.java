package com.itismob.s02.group5.animomap.ui.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a calculated walking route option between a starting place and a destination.
 * Supports distance, estimated walking time, stair counts, and waypoint coordinates.
 */
public class RouteOption {
    private final String id;
    private final String title;
    private final double distanceMeters;
    private final int estimatedMinutes;
    private final int stairCount;
    private final List<double[]> points;

    public RouteOption(String id, String title, double distanceMeters, int estimatedMinutes,
                       int stairCount, List<double[]> points) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.distanceMeters = distanceMeters;
        this.estimatedMinutes = estimatedMinutes;
        this.stairCount = stairCount;
        this.points = points != null ? new ArrayList<>(points) : new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public double getDistanceMeters() { return distanceMeters; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public int getStairCount() { return stairCount; }
    public List<double[]> getPoints() { return Collections.unmodifiableList(points); }

    /** Formatted distance string (e.g., "150 m" or "1.2 km"). */
    public String getFormattedDistance() {
        if (distanceMeters < 1000) {
            return Math.round(distanceMeters) + " m";
        }
        return String.format(Locale.US, "%.1f km", distanceMeters / 1000.0);
    }

    /** Formatted walking time string (e.g., "2 mins walk"). */
    public String getFormattedTime() {
        return estimatedMinutes + (estimatedMinutes == 1 ? " min walk" : " mins walk");
    }
}
