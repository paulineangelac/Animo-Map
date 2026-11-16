package com.itismob.s02.group5.animomap.ui.map;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Builds the dark overlay that covers everything outside the campus.
 * Right at the campus edge the overlay is clear; it gets darker over {@code fadeMeters} until it
 * reaches full strength, so the edge looks soft instead of cut off.
 * The bitmap covers the camera bounds in CampusMapData (north-up, one pixel = a few meters).
 */
final class CampusMask {
    private static final double METERS_PER_DEGREE_LAT = 110574.0;
    private static final double METERS_PER_DEGREE_LNG_AT_EQUATOR = 111320.0;
    private CampusMask() { }

    /**
     * @param color overlay color (alpha is ignored)
     * @param maxAlpha 0-255 darkness reached outside the fade zone
     * @param fadeMeters how far outside the campus edge the fade lasts
     * @param metersPerPixel bitmap resolution; the map smooths between pixels
     */
    static Bitmap create(int color, int maxAlpha, double fadeMeters, double metersPerPixel) {
        double[][] outline = CampusMapData.getCampusOutline();
        double south = CampusMapData.SOUTH, north = CampusMapData.NORTH;
        double west = CampusMapData.WEST, east = CampusMapData.EAST;
        double lngScale = METERS_PER_DEGREE_LNG_AT_EQUATOR * Math.cos(Math.toRadians((south + north) / 2));
        int width = (int) Math.ceil((east - west) * lngScale / metersPerPixel);
        int height = (int) Math.ceil((north - south) * METERS_PER_DEGREE_LAT / metersPerPixel);

        // Outline corners in meters from the south-west corner of the bitmap area.
        int n = outline.length;
        double[] px = new double[n], py = new double[n];
        for (int i = 0; i < n; i++) {
            px[i] = (outline[i][1] - west) * lngScale;
            py[i] = (outline[i][0] - south) * METERS_PER_DEGREE_LAT;
        }

        int rgb = Color.rgb(Color.red(color), Color.green(color), Color.blue(color)) & 0x00FFFFFF;
        int[] pixels = new int[width * height];
        for (int row = 0; row < height; row++) {
            double y = (height - row - 0.5) * metersPerPixel; // row 0 is the north edge
            for (int col = 0; col < width; col++) {
                double x = (col + 0.5) * metersPerPixel;
                if (isInside(x, y, px, py)) { continue; } // stays fully transparent
                double t = Math.min(1.0, distanceToOutline(x, y, px, py) / fadeMeters);
                double smooth = t * t * (3 - 2 * t);
                pixels[row * width + col] = (Math.round((float) (maxAlpha * smooth)) << 24) | rgb;
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    /** Standard ray-casting test: is the point inside the polygon? */
    private static boolean isInside(double x, double y, double[] px, double[] py) {
        boolean inside = false;
        for (int i = 0, j = px.length - 1; i < px.length; j = i++) {
            if ((py[i] > y) != (py[j] > y)
                    && x < (px[j] - px[i]) * (y - py[i]) / (py[j] - py[i]) + px[i]) {
                inside = !inside;
            }
        }
        return inside;
    }

    /** Shortest distance from the point to any edge of the polygon. */
    private static double distanceToOutline(double x, double y, double[] px, double[] py) {
        double best = Double.MAX_VALUE;
        for (int i = 0, j = px.length - 1; i < px.length; j = i++) {
            double dx = px[i] - px[j], dy = py[i] - py[j];
            double t = ((x - px[j]) * dx + (y - py[j]) * dy) / (dx * dx + dy * dy);
            t = Math.max(0.0, Math.min(1.0, t));
            double ex = px[j] + t * dx - x, ey = py[j] + t * dy - y;
            best = Math.min(best, ex * ex + ey * ey);
        }
        return Math.sqrt(best);
    }
}
