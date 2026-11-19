package com.itismob.s02.group5.animomap.ui.map;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CampusMaskCacheTest {
    @Test public void recreatedViewJoinsPendingWorkAndOnlyReceivesItsOwnCallback() {
        Harness harness = new Harness(CampusMaskCacheTest::image);
        List<CampusMask.PreparedImage> oldView = new ArrayList<>();
        List<CampusMask.PreparedImage> newView = new ArrayList<>();
        CampusMask.Request oldRequest = harness.cache.request(spec(), oldView::add);

        // The old view goes away after preparation, but before the UI can run completion.
        harness.worker.remove().run();
        oldRequest.cancel();
        harness.cache.request(spec(), newView::add);
        assertTrue(harness.worker.isEmpty());
        harness.main.remove().run();
        assertTrue(oldView.isEmpty());
        assertEquals(1, newView.size());

        List<CampusMask.PreparedImage> cached = new ArrayList<>();
        harness.cache.request(spec(), cached::add);
        assertTrue(harness.worker.isEmpty());
        harness.main.remove().run();
        assertSame(newView.get(0), cached.get(0));
    }

    @Test public void cacheRequiresTheExactGeometryAndFadeSettings() {
        CampusMask.Spec original = spec();
        double[][] movedOutline = outline();
        movedOutline[1][1] += 0.0001;
        CampusMask.Spec changedShape = new CampusMask.Spec(movedOutline, 14.56, 120.99,
                14.57, 121.0, 0xFF101316, 204, 30, 2);
        assertEquals(original.width, changedShape.width);
        assertEquals(original.height, changedShape.height);

        CampusMask.Spec[] different = {
                changedShape,
                new CampusMask.Spec(outline(), 14.56, 120.991, 14.57, 121.001,
                        0xFF101316, 204, 30, 2),
                parameters(0xFF121316, 204, 30, 2),
                parameters(0xFF101316, 180, 30, 2),
                parameters(0xFF101316, 204, 20, 2),
                parameters(0xFF101316, 204, 30, 3)
        };
        for (CampusMask.Spec other : different) {
            assertNotEquals(original, other);
            Harness harness = new Harness(CampusMaskCacheTest::image);
            harness.cache.request(original, ignored -> { });
            harness.worker.remove().run();
            harness.main.remove().run();
            harness.cache.request(other, ignored -> { });
            assertEquals(1, harness.worker.size());
        }
    }

    @Test public void failedWorkDeliversNoImageAndNextRequestCanRetry() {
        List<CampusMask.PreparedImage> images = new ArrayList<>();
        int[] attempts = {0};
        Harness harness = new Harness(spec -> {
            if (attempts[0]++ == 0) throw new IllegalStateException("Preparation failed");
            return image(spec);
        });

        harness.cache.request(spec(), images::add);
        harness.worker.remove().run();
        harness.main.remove().run();
        assertTrue(images.isEmpty());
        assertEquals(1, harness.errors.size());

        harness.cache.request(spec(), images::add);
        harness.worker.remove().run();
        harness.main.remove().run();
        assertEquals(1, images.size());
        assertEquals(2, attempts[0]);
    }

    private static CampusMask.PreparedImage image(CampusMask.Spec spec) {
        return new CampusMask.PreparedImage(spec.width, spec.height, ByteBuffer.allocateDirect(4));
    }

    private static CampusMask.Spec spec() {
        return parameters(0xFF101316, 204, 30, 2);
    }

    private static CampusMask.Spec parameters(int color, int alpha, double fade, double resolution) {
        return new CampusMask.Spec(outline(), 14.56, 120.99, 14.57, 121.0,
                color, alpha, fade, resolution);
    }

    private static double[][] outline() {
        return new double[][] {{14.561, 120.991}, {14.561, 120.999}, {14.569, 120.999}};
    }

    private static final class Harness {
        final ArrayDeque<Runnable> worker = new ArrayDeque<>();
        final ArrayDeque<Runnable> main = new ArrayDeque<>();
        final List<Throwable> errors = new ArrayList<>();
        final CampusMask.Cache cache;

        Harness(Function<CampusMask.Spec, CampusMask.PreparedImage> prepare) {
            cache = new CampusMask.Cache(worker::add, main::add, prepare, errors::add);
        }
    }
}
