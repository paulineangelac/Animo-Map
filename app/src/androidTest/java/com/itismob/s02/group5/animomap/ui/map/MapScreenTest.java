package com.itismob.s02.group5.animomap.ui.map;

import android.content.pm.ActivityInfo;
import android.graphics.RectF;
import android.view.View;
import android.view.MotionEvent;
import android.os.SystemClock;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Swipe;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import org.hamcrest.Matcher;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import com.itismob.s02.group5.animomap.MainActivity;
import com.itismob.s02.group5.animomap.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MapScreenTest {
    private ActivityScenario<MainActivity> scenario;
    @Before public void launch() {
        scenario = ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.iv_campus_map)).check(matches(isDisplayed()));
    }
    @After public void close() { scenario.close(); }

    private void searchFor(String query, String result) throws Exception {
        onView(withId(R.id.et_map_search)).perform(click(), replaceText(query));
        onView(withId(R.id.et_map_search)).perform(waitForSuggestion(result));
        capture("search-results-" + query);
        onView(withText(result)).inRoot(isPlatformPopup()).perform(click());
        capture("search-selected-" + query);
        onView(withId(R.id.et_map_search)).perform(waitForPlaceCard());
        onView(withId(R.id.card_map_place)).check(matches(isDisplayed()));
    }
    private ViewAction waitForSuggestion(String name) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isAssignableFrom(AutoCompleteTextView.class); }
            @Override public String getDescription() { return "wait for autocomplete filtering"; }
            @Override public void perform(UiController controller, View view) {
                for (int attempt = 0; attempt < 120; attempt++) {
                    ListAdapter adapter = ((AutoCompleteTextView) view).getAdapter();
                    Object item = adapter.getCount() == 1 ? adapter.getItem(0) : null;
                    if (adapter.getCount() == 1 && (name == null ? item == null
                            : item instanceof MapPlace && ((MapPlace) item).getName().equals(name))) {
                        // AutoComplete's popup and IME finish their layout after the filter publishes.
                        controller.loopMainThreadForAtLeast(300);
                        return;
                    }
                    controller.loopMainThreadForAtLeast(25);
                }
                fail("Autocomplete did not finish filtering for " + name);
            }
        };
    }
    private ViewAction confirmTap() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "wait for confirmed single tap"; }
            @Override public void perform(UiController controller, View view) {
                controller.loopMainThreadForAtLeast(400);
            }
        };
    }
    private ViewAction waitForPlaceCard() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "wait for place selection and layout"; }
            @Override public void perform(UiController controller, View view) {
                for (int attempt = 0; attempt < 120; attempt++) {
                    View card = view.getRootView().findViewById(R.id.card_map_place);
                    if (card.getVisibility() == View.VISIBLE && card.getHeight() > 0) { return; }
                    controller.loopMainThreadForAtLeast(25);
                }
                fail("Place card did not open");
            }
        };
    }
    private void capture(String name) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        // Shell-owned screenshots survive Gradle's uninstall cleanup and can be pulled for review.
        try (android.os.ParcelFileDescriptor fd = InstrumentationRegistry.getInstrumentation()
                .getUiAutomation().executeShellCommand("screencap -p /data/local/tmp/animo-map-" + name + ".png");
             java.io.InputStream input = new java.io.FileInputStream(fd.getFileDescriptor())) {
            byte[] buffer = new byte[1024];
            while (input.read(buffer) != -1) { /* wait for screenshot completion */ }
        }
    }
    @Test public void cameraFitsClampsAndKeepsCoordinateRoundTrip() throws Exception {
        scenario.onActivity(activity -> {
            ZoomableImageView map = activity.findViewById(R.id.iv_campus_map);
            RectF viewport = map.getMapViewport();
            float[] first = map.mapPoint(0, 0), last = map.mapPoint(1, 1);
            assertTrue(first[0] >= viewport.left - 1 && first[1] >= viewport.top - 1);
            assertTrue(last[0] <= viewport.right + 1 && last[1] <= viewport.bottom + 1);
            map.zoomBy(100);
            assertEquals(4, map.getRelativeZoom(), .001);
            map.focusOnPoint(.4f, .5f);
            float[] p = map.mapPoint(.37f, .63f);
            assertArrayEquals(new float[] {.37f, .63f}, map.screenToMap(p[0], p[1]), .0001f);
            float[] state = map.getCameraState();
            map.showOverview();
            assertEquals(1, map.getRelativeZoom(), .001);
            map.restoreCameraState(state);
            assertArrayEquals(state, map.getCameraState(), .001f);
            map.zoomBy(.001f);
            assertEquals(1, map.getRelativeZoom(), .001);
        });
        capture("01-overview");
    }
    @Test public void markerTapSelectsAndZoomDoesNotDismissCard() throws Exception {
        onView(withId(R.id.iv_campus_map)).perform(new GeneralClickAction(Tap.SINGLE, view -> {
            ZoomableImageView map = (ZoomableImageView) view;
            MapPlace place = CampusMapData.findPlace(CampusMapData.HENRY_SY);
            assertNotNull(place);
            float[] point = map.mapPoint(place.getMapX(), place.getMapY());
            int[] screen = new int[2];
            view.getLocationOnScreen(screen);
            return new float[] {screen[0] + point[0], screen[1] + point[1]};
        }, Press.FINGER, 0, 0), confirmTap());
        onView(withId(R.id.tv_map_place_name)).check(matches(withText("Henry Sy Sr. Hall")));
        onView(withId(R.id.btn_map_zoom_in)).perform(click());
        onView(withId(R.id.btn_map_overview)).perform(click());
        onView(withId(R.id.card_map_place)).check(matches(isDisplayed()));
        capture("02-selected-place");
        onView(withId(R.id.btn_close_map_place)).perform(click());
        onView(withId(R.id.card_map_place)).check(matches(withEffectiveVisibility(Visibility.GONE)));
    }
    @Test public void roomSearchEmptyResultsFiltersAndNavigation() throws Exception {
        onView(withId(R.id.et_map_search)).perform(click(), replaceText("zzzz"));
        onView(withId(R.id.et_map_search)).perform(waitForSuggestion(null));
        onView(withText(R.string.map_no_results)).inRoot(isPlatformPopup()).check(matches(isDisplayed()));
        searchFor("g303", "G303");
        onView(withId(R.id.tv_map_place_subtitle)).check(matches(withText("Gokongwei Hall · Level 3")));
        onView(withId(R.id.btn_map_directions)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        capture("03-room-search");
        onView(withId(R.id.chip_study)).perform(scrollTo(), click());
        onView(withId(R.id.card_map_place)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        capture("04-study-filter");
        onView(withId(R.id.chip_food)).perform(scrollTo(), click());
        capture("05-food-filter");
        onView(withId(R.id.chip_buildings)).perform(scrollTo(), click());
        onView(withId(R.id.chip_all)).perform(scrollTo(), click());
        onView(withId(R.id.navigation_community)).perform(click());
        onView(withId(R.id.navigation_groups)).perform(click());
        onView(withId(R.id.navigation_map)).perform(click());
        onView(withId(R.id.iv_campus_map)).check(matches(isDisplayed()));
    }
    private void rotateTo(int requested, int expected) {
        scenario.onActivity(activity -> activity.setRequestedOrientation(requested));
        for (int attempt = 0; attempt < 120; attempt++) {
            java.util.concurrent.atomic.AtomicBoolean ready = new java.util.concurrent.atomic.AtomicBoolean();
            scenario.onActivity(activity -> {
                View map = activity.findViewById(R.id.iv_campus_map);
                ready.set(activity.getResources().getConfiguration().orientation == expected
                        && (expected == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                        ? map.getWidth() > map.getHeight() : map.getHeight() > map.getWidth()));
            });
            if (ready.get()) {
                onView(withId(R.id.et_map_search)).perform(confirmTap());
                return;
            }
            SystemClock.sleep(25);
        }
        fail("Screen did not finish rotating");
    }
    @Test public void pinchAndDragMoveCameraWithoutSelectingPlaces() throws Exception {
        onView(withId(R.id.iv_campus_map)).perform(new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isAssignableFrom(ZoomableImageView.class); }
            @Override public String getDescription() { return "two-finger pinch outward"; }
            @Override public void perform(UiController controller, View view) {
                RectF viewport = ((ZoomableImageView) view).getMapViewport();
                MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[2];
                MotionEvent.PointerCoords[] points = new MotionEvent.PointerCoords[2];
                float density = view.getResources().getDisplayMetrics().density;
                for (int i = 0; i < 2; i++) {
                    properties[i] = new MotionEvent.PointerProperties();
                    properties[i].id = i;
                    properties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
                    points[i] = new MotionEvent.PointerCoords();
                    points[i].pressure = 1;
                    points[i].size = 1;
                    points[i].y = viewport.centerY();
                    points[i].x = viewport.centerX() + (i == 0 ? -20 : 20) * density;
                }
                long down = SystemClock.uptimeMillis();
                send(view, down, MotionEvent.ACTION_DOWN, 1, properties, points);
                send(view, down, MotionEvent.ACTION_POINTER_DOWN | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                        2, properties, points);
                for (int i = 1; i <= 12; i++) {
                    points[0].x = viewport.centerX() - (20 + i * 10) * density;
                    points[1].x = viewport.centerX() + (20 + i * 10) * density;
                    send(view, down, MotionEvent.ACTION_MOVE, 2, properties, points);
                    controller.loopMainThreadForAtLeast(20);
                }
                send(view, down, MotionEvent.ACTION_POINTER_UP | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                        2, properties, points);
                send(view, down, MotionEvent.ACTION_UP, 1, properties, points);
            }
        });
        final float[][] before = new float[1][];
        scenario.onActivity(activity -> {
            ZoomableImageView map = activity.findViewById(R.id.iv_campus_map);
            assertTrue("Pinch zoom was " + map.getRelativeZoom(), map.getRelativeZoom() > 1.2f);
            before[0] = map.getCameraState();
        });
        onView(withId(R.id.iv_campus_map)).perform(new GeneralSwipeAction(Swipe.SLOW,
                view -> swipePoint(view, .65f, .65f), view -> swipePoint(view, .35f, .35f), Press.FINGER), confirmTap());
        onView(withId(R.id.card_map_place)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        scenario.onActivity(activity -> {
            ZoomableImageView map = activity.findViewById(R.id.iv_campus_map);
            float[] after = map.getCameraState();
            assertTrue(Math.abs(after[0] - before[0][0]) > .01f || Math.abs(after[1] - before[0][1]) > .01f);
            MapPlace henry = CampusMapData.findPlace(CampusMapData.HENRY_SY);
            assertNotNull(henry);
            map.focusOnPoint(henry.getMapX(), henry.getMapY());
        });
        onView(withId(R.id.iv_campus_map)).perform(confirmTap());
        scenario.onActivity(activity -> {
            ZoomableImageView map = activity.findViewById(R.id.iv_campus_map);
            MapOverlayView overlay = activity.findViewById(R.id.map_overlay);
            MapPlace henry = CampusMapData.findPlace(CampusMapData.HENRY_SY);
            assertNotNull(henry);
            float[] p = map.mapPoint(henry.getMapX(), henry.getMapY());
            MapPlace tapped = overlay.findPlaceAt(p[0], p[1]);
            assertNotNull(tapped);
            assertEquals(henry.getId(), tapped.getId());
        });
        capture("09-zoomed-map");
    }
    private static float[] swipePoint(View view, float x, float y) {
        RectF viewport = ((ZoomableImageView) view).getMapViewport();
        int[] screen = new int[2];
        view.getLocationOnScreen(screen);
        return new float[] {screen[0] + viewport.left + viewport.width() * x,
                screen[1] + viewport.top + viewport.height() * y};
    }
    private static void send(View view, long down, int action, int count,
            MotionEvent.PointerProperties[] properties, MotionEvent.PointerCoords[] points) {
        MotionEvent event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, count,
                properties, points, 0, 0, 1, 1, 0, 0, android.view.InputDevice.SOURCE_TOUCHSCREEN, 0);
        view.dispatchTouchEvent(event);
        event.recycle();
    }
    private void assertDemoFitsViewport() {
        onView(withId(R.id.iv_campus_map)).perform(confirmTap());
        scenario.onActivity(activity -> {
            ZoomableImageView map = activity.findViewById(R.id.iv_campus_map);
            RectF viewport = map.getMapViewport();
            viewport.inset(-1, -1);
            for (float[] point : CampusMapData.getDemoRoutePoints()) {
                float[] screen = map.mapPoint(point[0], point[1]);
                assertTrue("Demo point outside visible map: " + screen[0] + ", " + screen[1],
                        viewport.contains(screen[0], screen[1]));
            }
        });
        onView(withId(R.id.tv_map_place_category)).check(matches(isDisplayed()));
    }
    @Test public void demoRouteBackAndRecreationPreserveSelection() throws Exception {
        searchFor("henry", "Henry Sy Sr. Hall");
        onView(withId(R.id.btn_map_directions)).perform(scrollTo(), click());
        onView(withId(R.id.tv_map_place_category)).check(matches(withText(R.string.map_demo_notice)));
        assertDemoFitsViewport();
        capture("06-demo-route");
        scenario.recreate();
        onView(withId(R.id.tv_map_place_name)).check(matches(withText(R.string.map_demo_title)));
        androidx.test.espresso.Espresso.pressBack();
        onView(withId(R.id.btn_map_directions)).check(matches(withText(R.string.map_preview_demo)));
        rotateTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, android.content.res.Configuration.ORIENTATION_LANDSCAPE);
        onView(withId(R.id.card_map_place)).check(matches(isDisplayed()));
        capture("07-landscape-place");
        onView(withId(R.id.btn_map_directions)).perform(scrollTo(), click());
        onView(withId(R.id.et_map_search)).perform(confirmTap());
        onView(withId(R.id.tv_map_place_category)).check(matches(withText(R.string.map_demo_notice)));
        assertDemoFitsViewport();
        capture("08-landscape-demo");
        rotateTo(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, android.content.res.Configuration.ORIENTATION_PORTRAIT);
        onView(withId(R.id.tv_map_place_category)).check(matches(withText(R.string.map_demo_notice)));
    }
}
