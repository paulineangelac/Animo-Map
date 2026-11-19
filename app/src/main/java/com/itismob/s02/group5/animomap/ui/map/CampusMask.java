package com.itismob.s02.group5.animomap.ui.map;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

final class CampusMask {
    private static final double METERS_PER_DEGREE_LAT = 110574.0;
    private static final double METERS_PER_DEGREE_LNG_AT_EQUATOR = 111320.0;
    private CampusMask() { }

    static final Executor WORKER = Executors.newSingleThreadExecutor();

    private static final class SharedCache {
        private static final Handler MAIN = new Handler(Looper.getMainLooper());
        private static final Cache INSTANCE = new Cache(WORKER, MAIN::post, CampusMask::prepare,
                error -> Log.w("CampusMask", "Could not prepare the campus fade", error));
    }

    @MainThread
    static Request request(int color, int maxAlpha, double fadeMeters, double metersPerPixel,
                           Consumer<PreparedImage> callback) {
        Spec spec = new Spec(CampusMapData.getCampusOutline(), CampusMapData.SOUTH,
                CampusMapData.WEST, CampusMapData.NORTH, CampusMapData.EAST,
                color, maxAlpha, fadeMeters, metersPerPixel);
        return SharedCache.INSTANCE.request(spec, callback);
    }

    private static PreparedImage prepare(Spec spec) {
        Bitmap fade = create(spec);
        try {
            ByteBuffer pixels = ByteBuffer.allocateDirect(fade.getByteCount());
            fade.copyPixelsToBuffer(pixels);
            pixels.rewind();
            return new PreparedImage(fade.getWidth(), fade.getHeight(), pixels);
        } finally {
            fade.recycle();
        }
    }

    private static Bitmap create(Spec spec) {
        double[][] outline = spec.outline;
        int width = spec.width;
        int height = spec.height;

        int n = outline.length;
        double[] px = new double[n], py = new double[n];
        for (int i = 0; i < n; i++) {
            px[i] = (outline[i][1] - spec.west) * spec.lngScale;
            py[i] = (outline[i][0] - spec.south) * METERS_PER_DEGREE_LAT;
        }

        int rgb = spec.color & 0x00FFFFFF;
        int[] pixels = new int[width * height];
        for (int row = 0; row < height; row++) {
            double y = (height - row - 0.5) * spec.metersPerPixel;
            for (int col = 0; col < width; col++) {
                double x = (col + 0.5) * spec.metersPerPixel;
                if (isInside(x, y, px, py)) continue;
                double t = Math.min(1.0, distanceToOutline(x, y, px, py) / spec.fadeMeters);
                double smooth = t * t * (3 - 2 * t);
                pixels[row * width + col] = (Math.round((float) (spec.maxAlpha * smooth)) << 24) | rgb;
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

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

    /** Exact geometry and settings used to create the pixels, independent of any MapView. */
    static final class Spec {
        private final double[][] outline;
        private final double south, west, north, east;
        private final int color, maxAlpha;
        private final double fadeMeters, metersPerPixel, lngScale;
        final int width, height;

        Spec(double[][] outline, double south, double west, double north, double east,
             int color, int maxAlpha, double fadeMeters, double metersPerPixel) {
            this.outline = new double[outline.length][];
            for (int i = 0; i < outline.length; i++) this.outline[i] = outline[i].clone();
            this.south = south;
            this.west = west;
            this.north = north;
            this.east = east;
            this.color = color;
            this.maxAlpha = maxAlpha;
            this.fadeMeters = fadeMeters;
            this.metersPerPixel = metersPerPixel;
            lngScale = METERS_PER_DEGREE_LNG_AT_EQUATOR
                    * Math.cos(Math.toRadians((south + north) / 2));
            width = (int) Math.ceil((east - west) * lngScale / metersPerPixel);
            height = (int) Math.ceil((north - south) * METERS_PER_DEGREE_LAT / metersPerPixel);
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Spec)) return false;
            Spec spec = (Spec) other;
            return color == spec.color && maxAlpha == spec.maxAlpha
                    && width == spec.width && height == spec.height
                    && Double.compare(south, spec.south) == 0 && Double.compare(west, spec.west) == 0
                    && Double.compare(north, spec.north) == 0 && Double.compare(east, spec.east) == 0
                    && Double.compare(fadeMeters, spec.fadeMeters) == 0
                    && Double.compare(metersPerPixel, spec.metersPerPixel) == 0
                    && Arrays.deepEquals(outline, spec.outline);
        }

        @Override public int hashCode() {
            return 31 * Objects.hash(south, west, north, east, color, maxAlpha,
                    fadeMeters, metersPerPixel, width, height) + Arrays.deepHashCode(outline);
        }
    }

    static final class PreparedImage {
        final int width, height;
        private final ByteBuffer pixels;

        PreparedImage(int width, int height, ByteBuffer pixels) {
            this.width = width;
            this.height = height;
            this.pixels = pixels;
        }

        ByteBuffer pixelsForUpload() {
            return pixels.duplicate();
        }
    }

    static final class Request {
        private Consumer<PreparedImage> callback;

        Request(Consumer<PreparedImage> callback) { this.callback = callback; }

        @MainThread void cancel() { callback = null; }

        @MainThread void deliver(PreparedImage image) {
            Consumer<PreparedImage> listener = callback;
            callback = null;
            if (listener != null) listener.accept(image);
        }
    }

    /** Coordinates requests on the main thread and runs only pixel preparation on the worker. */
    static final class Cache {
        private final Executor worker, main;
        private final Function<Spec, PreparedImage> prepare;
        private final Consumer<Throwable> reportFailure;
        private final Map<Spec, List<Request>> inFlight = new HashMap<>();
        private Spec cachedSpec;
        private PreparedImage cachedImage;

        Cache(Executor worker, Executor main, Function<Spec, PreparedImage> prepare,
              Consumer<Throwable> reportFailure) {
            this.worker = worker;
            this.main = main;
            this.prepare = prepare;
            this.reportFailure = reportFailure;
        }

        @MainThread Request request(Spec spec, Consumer<PreparedImage> callback) {
            Request request = new Request(callback);
            if (spec.equals(cachedSpec)) {
                PreparedImage image = cachedImage;
                main.execute(() -> request.deliver(image));
                return request;
            }
            List<Request> waiting = inFlight.get(spec);
            if (waiting != null) {
                waiting.add(request);
                return request;
            }
            waiting = new ArrayList<>();
            waiting.add(request);
            inFlight.put(spec, waiting);
            // Canceling one view's delivery never cancels the shared preparation.
            worker.execute(() -> {
                try {
                    PreparedImage image = prepare.apply(spec);
                    main.execute(() -> complete(spec, image));
                } catch (RuntimeException | OutOfMemoryError error) {
                    main.execute(() -> {
                        List<Request> pendingRequests = inFlight.remove(spec);
                        if (pendingRequests != null) {
                            for (Request pending : pendingRequests) pending.cancel();
                        }
                        reportFailure.accept(error);
                    });
                }
            });
            return request;
        }

        @MainThread private void complete(Spec spec, PreparedImage image) {
            cachedSpec = spec;
            cachedImage = image;
            List<Request> requests = inFlight.remove(spec);
            if (requests != null) {
                for (Request request : requests) request.deliver(image);
            }
        }
    }
}
