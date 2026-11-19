package com.itismob.s02.group5.animomap.ui.map;

import android.os.SystemClock;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.itismob.s02.group5.animomap.MainActivity;
import com.itismob.s02.group5.animomap.R;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CampusFadeStabilityTest {
    @Test
    public void mapRespondsWhileFadeIsPendingAcrossRecreationAndFiveRelaunches() throws Exception {
        CountDownLatch workerHeld = new CountDownLatch(1);
        CountDownLatch releaseWorker = new CountDownLatch(1);
        CampusMask.WORKER.execute(() -> {
            workerHeld.countDown();
            try {
                releaseWorker.await();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            assertTrue(workerHeld.await(10, TimeUnit.SECONDS));
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
                waitForFadeLayer(scenario);
                assertZoomResponds(scenario);
                scenario.recreate();
                waitForFadeLayer(scenario);
                assertZoomResponds(scenario);
                releaseWorker.countDown();
                CountDownLatch prepared = new CountDownLatch(1);
                CampusMask.WORKER.execute(prepared::countDown);
                assertTrue("Fade worker did not finish", prepared.await(30, TimeUnit.SECONDS));
                assertZoomResponds(scenario);
                assertComposerAndPickerReturnToMap(scenario);
            }
        } finally {
            releaseWorker.countDown();
        }

        for (int launch = 1; launch <= 5; launch++) {
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
                waitForFadeLayer(scenario);
                assertZoomResponds(scenario);
                android.util.Log.i("CampusFadeTest", "Relaunch " + launch + " responded");
            }
        }
    }

    private static void waitForFadeLayer(ActivityScenario<MainActivity> scenario) {
        long deadline = SystemClock.uptimeMillis() + 20_000L;
        AtomicBoolean found = new AtomicBoolean();
        do {
            scenario.onActivity(activity -> {
                MapView map = activity.findViewById(R.id.mapbox_map);
                Style style = map.getMapboxMap().getStyle();
                found.set(style != null && style.isValid() && style.styleLayerExists("campus-fade-layer"));
            });
            if (found.get()) return;
            SystemClock.sleep(50);
        } while (SystemClock.uptimeMillis() < deadline);
        fail("Campus fade layer was not registered");
    }

    private static void assertZoomResponds(ActivityScenario<MainActivity> scenario) {
        double[] previousZoom = new double[1];
        scenario.onActivity(activity -> {
            MapView map = activity.findViewById(R.id.mapbox_map);
            previousZoom[0] = map.getMapboxMap().getCameraState().getZoom();
        });
        onView(withId(R.id.btn_map_zoom_in)).perform(click());
        AtomicBoolean moved = new AtomicBoolean();
        long deadline = SystemClock.uptimeMillis() + 10_000L;
        do {
            scenario.onActivity(activity -> {
                MapView map = activity.findViewById(R.id.mapbox_map);
                moved.set(map.getMapboxMap().getCameraState().getZoom() > previousZoom[0] + 0.2);
            });
            if (moved.get()) return;
            SystemClock.sleep(50);
        } while (SystemClock.uptimeMillis() < deadline);
        fail("Map zoom did not respond");
    }

    private static void assertComposerAndPickerReturnToMap(ActivityScenario<MainActivity> scenario) {
        onView(withId(R.id.navigation_community)).perform(click());
        onView(withId(R.id.community_create_post)).perform(click());
        waitForScreen(R.id.create_post_add_location);
        onView(withId(R.id.create_post_add_location)).perform(click());
        waitForScreen(R.id.community_picker_query);
        onView(withId(R.id.community_picker_pin_mode)).perform(click());
        onView(allOf(withId(R.id.community_picker_map), isAssignableFrom(MapView.class)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.community_picker_search_mode)).perform(click());
        waitForScreen(R.id.community_picker_query);
        pressBack();
        waitForScreen(R.id.create_post_add_location);
        pressBack();
        waitForScreen(R.id.community_create_post);
        onView(withId(R.id.navigation_map)).perform(click());
        waitForFadeLayer(scenario);
    }

    private static void waitForScreen(int id) {
        long deadline = SystemClock.uptimeMillis() + 12_000L;
        do {
            try {
                onView(withId(id)).check(matches(isDisplayed()));
                return;
            } catch (NoMatchingViewException | AssertionError waiting) {
                SystemClock.sleep(100);
            }
        } while (SystemClock.uptimeMillis() < deadline);
        onView(withId(id)).check(matches(isDisplayed()));
    }
}
