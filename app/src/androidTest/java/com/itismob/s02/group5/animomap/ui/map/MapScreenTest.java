package com.itismob.s02.group5.animomap.ui.map;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.os.SystemClock;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import org.hamcrest.Matcher;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.itismob.s02.group5.animomap.MainActivity;
import com.itismob.s02.group5.animomap.R;
import com.mapbox.geojson.Point;
import com.mapbox.maps.MapView;
import com.mapbox.maps.ScreenCoordinate;
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
        onView(withId(R.id.mapbox_map)).check(matches(isDisplayed()));
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
    @Test public void zoomButtonsMoveCameraAndOverviewResetsIt() throws Exception {
        final double[] zoom = new double[3];
        scenario.onActivity(activity -> zoom[0] = ((MapView) activity.findViewById(R.id.mapbox_map))
                .getMapboxMap().getCameraState().getZoom());
        onView(withId(R.id.btn_map_zoom_in)).perform(click(), confirmTap());
        scenario.onActivity(activity -> zoom[1] = ((MapView) activity.findViewById(R.id.mapbox_map))
                .getMapboxMap().getCameraState().getZoom());
        assertTrue("Zoom in should raise zoom: " + zoom[0] + " -> " + zoom[1], zoom[1] > zoom[0]);
        onView(withId(R.id.btn_map_overview)).perform(click(), confirmTap());
        scenario.onActivity(activity -> zoom[2] = ((MapView) activity.findViewById(R.id.mapbox_map))
                .getMapboxMap().getCameraState().getZoom());
        assertEquals(CampusMapData.OVERVIEW_ZOOM, zoom[2], .05);
        capture("01-overview");
    }
    @Test public void roomSearchEmptyResultsFiltersAndNavigation() throws Exception {
        onView(withId(R.id.et_map_search)).perform(click(), replaceText("zzzz"));
        onView(withId(R.id.et_map_search)).perform(waitForSuggestion(null));
        onView(withText(R.string.map_no_results)).inRoot(isPlatformPopup()).check(matches(isDisplayed()));
        searchFor("la salle", "St. La Salle Hall");
        onView(withId(R.id.tv_map_place_name)).check(matches(withText("St. La Salle Hall")));
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
        onView(withId(R.id.mapbox_map)).check(matches(isDisplayed()));
    }
    private void rotateTo(int requested, int expected) {
        scenario.onActivity(activity -> activity.setRequestedOrientation(requested));
        for (int attempt = 0; attempt < 120; attempt++) {
            java.util.concurrent.atomic.AtomicBoolean ready = new java.util.concurrent.atomic.AtomicBoolean();
            scenario.onActivity(activity -> {
                View map = activity.findViewById(R.id.mapbox_map);
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
    private void assertDemoFitsViewport() {
        onView(withId(R.id.mapbox_map)).perform(confirmTap());
        scenario.onActivity(activity -> {
            MapView map = activity.findViewById(R.id.mapbox_map);
            for (double[] point : CampusMapData.getDemoRoutePoints()) {
                ScreenCoordinate screen = map.getMapboxMap().pixelForCoordinate(
                        Point.fromLngLat(point[1], point[0]));
                assertTrue("Demo point outside visible map: " + screen.getX() + ", " + screen.getY(),
                        screen.getX() >= 0 && screen.getX() <= map.getWidth()
                                && screen.getY() >= 0 && screen.getY() <= map.getHeight());
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
