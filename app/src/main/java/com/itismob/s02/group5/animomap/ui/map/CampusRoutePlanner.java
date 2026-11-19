package com.itismob.s02.group5.animomap.ui.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Plans walking routes between campus locations in the Philippines (DLSU Manila and nearby area up to 10km),
 * computes distances, estimated walking times, shortest routes, alternative routes, and validates range.
 */
public final class CampusRoutePlanner {
    private static final double EARTH_RADIUS_METERS = 6371000.0;
    // Average walking speed on campus: ~80 meters per minute
    private static final double METERS_PER_MINUTE = 80.0;

    // DLSU Manila Campus Center coordinates
    public static final double CAMPUS_CENTER_LAT = 14.564794;
    public static final double CAMPUS_CENTER_LNG = 120.993346;
    // Maximum area outside campus in meters (10 km)
    public static final double MAX_NEARBY_RADIUS_METERS = 10000.0;

    private CampusRoutePlanner() { }

    /**
     * Calculates the great-circle distance between two points in meters using the Haversine formula.
     */
    public static double calculateDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Checks whether given coordinates are within the university campus or the 10km nearby area in the Philippines.
     */
    public static boolean isWithinCampusOrNearby(double lat, double lng) {
        double dist = calculateDistanceMeters(CAMPUS_CENTER_LAT, CAMPUS_CENTER_LNG, lat, lng);
        return dist <= MAX_NEARBY_RADIUS_METERS;
    }

    /**
     * Estimates walking time in minutes based on distance in meters.
     */
    public static int estimateWalkingMinutes(double distanceMeters) {
        int minutes = (int) Math.ceil(distanceMeters / METERS_PER_MINUTE);
        return Math.max(1, minutes);
    }

    /**
     * Generates available route options (shortest route and alternative route with fewer stairs)
     * between a starting place and a destination place within campus or nearby area.
     */
    public static List<RouteOption> getRouteOptions(MapPlace from, MapPlace to) {
        Objects.requireNonNull(from, "Starting place cannot be null");
        Objects.requireNonNull(to, "Destination place cannot be null");

        List<RouteOption> options = new ArrayList<>();

        double directDist = calculateDistanceMeters(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude()
        );

        // Add waypoints for direct route
        List<double[]> directPoints = new ArrayList<>();
        directPoints.add(new double[]{from.getLatitude(), from.getLongitude()});
        directPoints.add(new double[]{to.getLatitude(), to.getLongitude()});

        int shortestTime = estimateWalkingMinutes(directDist);
        options.add(new RouteOption(
                "shortest",
                "Shortest Route (Main Path)",
                directDist,
                shortestTime,
                2, // Default stairs
                directPoints
        ));

        // Alternative route (e.g. covered walkway / fewer stairs) if distance is meaningful
        if (directDist > 20.0) {
            double altDist = directDist * 1.15; // slightly longer detour
            int altTime = estimateWalkingMinutes(altDist);

            List<double[]> altPoints = new ArrayList<>();
            altPoints.add(new double[]{from.getLatitude(), from.getLongitude()});
            // Add a mid waypoint offset simulating a covered walkway
            altPoints.add(new double[]{
                    (from.getLatitude() + to.getLatitude()) / 2.0 + 0.0001,
                    (from.getLongitude() + to.getLongitude()) / 2.0
            });
            altPoints.add(new double[]{to.getLatitude(), to.getLongitude()});

            options.add(new RouteOption(
                    "fewer_stairs",
                    "Alternative Route (Fewer Stairs / Covered)",
                    altDist,
                    altTime,
                    0, // 0 stairs or ramp
                    altPoints
            ));
        }

        return options;
    }
}
