package com.itismob.s02.group5.animomap.ui.map;

/**
 * The building the user picked as "where I am" when GPS is off or denied.
 * Kept in memory so it survives switching tabs; later features (directions, group presence)
 * can read it. It is not saved to disk and resets when the app closes.
 */
public final class UserLocation {
    private static String manualPlaceId;
    private UserLocation() { }

    /** Id of the manually chosen building, or null if none. */
    public static String getManualPlaceId() { return manualPlaceId; }
    public static MapPlace getManualPlace() { return CampusMapData.findPlace(manualPlaceId); }
    public static void setManualPlaceId(String placeId) { manualPlaceId = placeId; }
    public static void clear() { manualPlaceId = null; }
}
