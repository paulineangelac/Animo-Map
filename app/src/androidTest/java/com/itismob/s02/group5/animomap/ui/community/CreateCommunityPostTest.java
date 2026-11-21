package com.itismob.s02.group5.animomap.ui.community;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.itismob.s02.group5.animomap.MainActivity;
import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.map.CampusMapData;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class CreateCommunityPostTest {
    @Test
    public void recommendationPublishesFromNamedLocationAndSurvivesActivityRecreation() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.navigation_community)).perform(click());
            onView(withId(R.id.community_create_post)).perform(click());
            waitFor(withId(R.id.create_post_submit));
            onView(withId(R.id.create_post_submit)).perform(click());
            onView(withText(R.string.community_require_category)).check(matches(isDisplayed()));
            onView(withId(R.id.create_post_type_poi)).perform(click()).check(matches(isChecked()));
            onView(withId(R.id.create_post_place_name)).check(matches(isDisplayed()));
            onView(withId(R.id.create_post_type_updates)).perform(click()).check(matches(isChecked()));
            onView(withId(R.id.create_post_place_name)).check(matches(not(isDisplayed())));
            onView(withId(R.id.create_post_type_poi)).perform(click());
            onView(withId(R.id.create_post_place_name)).perform(replaceText("A new study nook"));
            onView(withId(R.id.create_post_description))
                    .perform(click(), replaceText("Two quiet tables."));
            onView(withId(R.id.create_post_submit)).check(matches(isDisplayed()));
            onView(withId(R.id.create_post_description)).perform(closeSoftKeyboard());
            onView(withId(R.id.create_post_add_location)).perform(click());
            waitFor(withId(R.id.community_picker_query));
            onView(withId(R.id.community_picker_query))
                    .perform(replaceText("St. La Salle Hall"), closeSoftKeyboard());
            onView(allOf(withId(R.id.tv_search_result_name), withText("St. La Salle Hall")))
                    .perform(click());
            onView(withId(R.id.community_picker_confirm)).perform(click());
            waitFor(withId(R.id.create_post_location_label));
            onView(withId(R.id.create_post_location_label))
                    .check(matches(withText(containsString("St. La Salle Hall"))));
            onView(withId(R.id.create_post_submit)).perform(click());
            waitFor(withText("A new study nook"));
            onView(withText("A new study nook")).check(matches(isDisplayed()));

            onView(withId(R.id.navigation_groups)).perform(click());
            onView(withId(R.id.navigation_community)).perform(click());
            onView(withText("A new study nook")).check(matches(isDisplayed()));

            scenario.recreate();
            onView(withText("A new study nook")).check(matches(isDisplayed()));
            onView(withId(R.id.community_filter)).perform(click());
            onView(withContentDescription("Most Recent, selected")).check(matches(isDisplayed()));
            onView(withText(R.string.community_filter_relevant)).check(matches(isDisplayed()));
            onView(withText(R.string.community_filter_all)).check(matches(isDisplayed()));
            onView(withText(R.string.community_filter_candidates)).perform(click());
            onView(withId(R.id.community_filter)).check(matches(withText(R.string.community_filter_candidates)));
            onView(withText("A new study nook")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void unmarkedCampusTapReturnsAPrivatePin() {
        try (ActivityScenario<CommunityLocationPickerActivity> scenario =
                     ActivityScenario.launchActivityForResult(CommunityLocationPickerActivity.class)) {
            onView(withId(R.id.community_picker_pin_mode)).perform(click());
            onView(allOf(withId(R.id.community_picker_map), isAssignableFrom(MapView.class)))
                    .perform(waitForCampusMap(), click());
            waitFor(withId(R.id.community_picker_selection));
            onView(withId(R.id.community_picker_selection))
                    .check(matches(withText(containsString("Campus pin"))));
            onView(withId(R.id.community_picker_confirm)).perform(click());
            assertEquals(Activity.RESULT_OK, scenario.getResult().getResultCode());
            PostLocation location = PostLocation.fromBundle(scenario.getResult().getResultData()
                    .getBundleExtra(CommunityLocationPickerActivity.EXTRA_LOCATION));
            assertNotNull(location);
            assertNull(location.placeId);
            assertTrue(CampusMapData.isOnCampus(location.latitude, location.longitude));
        }
    }

    @Test
    public void importedPhotosSurviveDraftRecreationAndOpenFromFeedAndDetails() throws IOException {
        File first = photoFixture(Color.GREEN);
        File second = photoFixture(Color.BLUE);
        try (ActivityScenario<CreateCommunityPostActivity> scenario =
                     ActivityScenario.launchActivityForResult(CreateCommunityPostActivity.class)) {
            onView(withText(R.string.community_type_updates)).perform(click());
            onView(withId(R.id.create_post_description)).perform(replaceText("A post with two photos."));
            scenario.onActivity(activity -> new ViewModelProvider(activity)
                    .get(DraftPhotoViewModel.class).add(activity.getContentResolver(),
                            Arrays.asList(Uri.fromFile(first), Uri.fromFile(second))));
            waitFor(withContentDescription("Photo 2 of 2"));
            onView(withContentDescription("Remove photo 1")).perform(click());
            waitFor(withContentDescription("Photo 1 of 1"));
            scenario.onActivity(activity -> new ViewModelProvider(activity)
                    .get(DraftPhotoViewModel.class).add(activity.getContentResolver(),
                            Collections.singletonList(Uri.fromFile(first))));
            waitFor(withContentDescription("Photo 2 of 2"));
            scenario.recreate();
            waitFor(withContentDescription("Photo 2 of 2"));
            onView(withId(R.id.create_post_type_updates)).check(matches(isChecked()));
            onView(withId(R.id.create_post_description)).check(matches(withText("A post with two photos.")));
            onView(withId(R.id.create_post_submit)).perform(click());
            assertEquals(Activity.RESULT_OK, scenario.getResult().getResultCode());
        } finally {
            first.delete();
            second.delete();
        }

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.navigation_community)).perform(click());
            onView(withText("A post with two photos.")).check(matches(isDisplayed()));
            Matcher<View> inTestCard = isDescendantOfA(allOf(withId(R.id.community_post_card),
                    hasDescendant(withText("A post with two photos."))));
            Matcher<View> carousel = allOf(withId(R.id.community_post_photos), inTestCard);
            onView(carousel).perform(swipeLeft());
            waitFor(allOf(withId(R.id.community_post_photo_count), inTestCard, withText("2 / 2")));
            onView(carousel).perform(click());
            waitFor(withId(R.id.community_viewer_count));
            onView(withId(R.id.community_viewer_count)).check(matches(withText("2 / 2")));
            onView(withId(R.id.community_viewer_photos)).perform(swipeRight());
            waitFor(allOf(withId(R.id.community_viewer_count), withText("1 / 2")));
            pressBack();
            waitFor(withText("A post with two photos."));
            onView(withText("A post with two photos.")).check(matches(isDisplayed()));

            onView(withText("A post with two photos.")).perform(click());
            waitFor(withId(R.id.details_post_description));
            onView(withId(R.id.community_post_photos)).perform(swipeLeft());
            waitFor(allOf(withId(R.id.community_post_photo_count), withText("2 / 2")));
            onView(withId(R.id.community_post_photos)).perform(click());
            waitFor(allOf(withId(R.id.community_viewer_count), withText("2 / 2")));
            pressBack();
            waitFor(withId(R.id.details_post_description));
            onView(withId(R.id.details_post_description)).check(matches(isDisplayed()));
        }
    }

    private File photoFixture(int color) throws IOException {
        File cache = InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir();
        File fixture = File.createTempFile("post-photo-test-", ".png", cache);
        Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color);
        try (FileOutputStream output = new FileOutputStream(fixture)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        } finally {
            bitmap.recycle();
        }
        return fixture;
    }

    private void waitFor(Matcher<View> view) {
        long timeout = SystemClock.uptimeMillis() + 8000;
        do {
            try {
                onView(view).check(matches(isDisplayed()));
                return;
            } catch (NoMatchingViewException | AssertionError waiting) {
                SystemClock.sleep(100);
            }
        } while (SystemClock.uptimeMillis() < timeout);
        onView(view).check(matches(isDisplayed()));
    }

    private ViewAction waitForCampusMap() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "wait for the offline campus layer"; }
            @Override public void perform(UiController controller, View view) {
                long timeout = SystemClock.uptimeMillis() + 8000;
                MapView map = (MapView) view;
                do {
                    Style style = map.getMapboxMap().getStyle();
                    if (style != null && style.styleLayerExists("picker-campus-fill")) return;
                    controller.loopMainThreadForAtLeast(100);
                } while (SystemClock.uptimeMillis() < timeout);
                fail("The offline campus layer did not load.");
            }
        };
    }

}
