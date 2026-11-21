package com.itismob.s02.group5.animomap.ui.community;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.itismob.s02.group5.animomap.MainActivity;
import com.itismob.s02.group5.animomap.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.hamcrest.Matcher;

import java.util.Collections;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public class CommunityPostDetailsTest {
    @Test
    public void feedOpensDetailsAndShowsUpdatedCountAfterComment() {
        String body = "Post details test without attachments.";
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Matcher<View> inPost = isDescendantOfA(allOf(
                withId(R.id.community_post_card), hasDescendant(withText(body))));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> CommunityFeedDataSource.submitPost(
                    "@alexsantos", "AS", CommunityPostType.GENERAL_DISCUSSION, "", body,
                    null, Collections.emptyList()));
            onView(withId(R.id.navigation_community)).perform(click());
            onView(withId(R.id.community_filter)).perform(click());
            onView(withText(R.string.community_filter_all)).perform(click());
            onView(allOf(withId(R.id.community_upvote_button), inPost)).perform(click());

            onView(withText(body)).perform(click());
            onView(withId(R.id.details_post_description)).check(matches(withText(body)));
            onView(withId(R.id.details_vote_count)).check(matches(withText(
                    context.getResources().getQuantityString(R.plurals.community_upvote_total, 1, 1))));
            onView(withId(R.id.details_location_label)).check(matches(not(isDisplayed())));
            onView(withId(R.id.community_post_photo_area)).check(matches(not(isDisplayed())));
            onView(withId(R.id.details_post_more)).perform(click());
            onView(withText(R.string.community_report_post)).perform(click());
            onView(withText(R.string.community_report_confirmation)).check(matches(isDisplayed()));
            onView(withText(R.string.community_report_confirm)).perform(click());
            onView(withText(R.string.community_report_acknowledgement)).check(matches(isDisplayed()));
            onView(withId(R.id.post_details_back)).perform(click());

            onView(allOf(withId(R.id.community_comments_button), inPost)).perform(click());
            onView(withText(R.string.community_no_comments)).check(matches(isDisplayed()));
            onView(withId(R.id.post_details_comment_input)).perform(replaceText(" \u00a0  "));
            onView(withId(R.id.post_details_send)).perform(click());
            onView(withId(R.id.post_details_comment_input)).check(matches(
                    hasErrorText(context.getString(R.string.community_comment_required))));

            onView(withId(R.id.post_details_comment_input))
                    .perform(click(), replaceText("See you at the library."));
            onView(withId(R.id.post_details_send)).check(matches(isDisplayed())).perform(click());
            onView(withText("See you at the library.")).check(matches(isDisplayed()));
            onView(withId(R.id.details_comments_heading)).check(matches(
                    withText(context.getString(R.string.community_comments_heading, 1))));
            onView(withId(R.id.post_details_back)).perform(click());
            onView(withId(R.id.community_filter)).check(matches(withText(R.string.community_filter_all)));
            onView(allOf(withId(R.id.community_comments_button), inPost)).check(matches(withText(
                    context.getResources().getQuantityString(R.plurals.community_comments_count, 1, 1))));
        }
    }

    @Test
    public void commentsEntryKeepsSortAndDraftThroughRecreation() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String postId = CommunityFeedDataSource.getSamplePosts().get(0).getId();
        Intent intent = new Intent(context, CommunityPostDetailsActivity.class)
                .putExtra(CommunityPostDetailsActivity.EXTRA_POST_ID, postId)
                .putExtra(CommunityPostDetailsActivity.EXTRA_OPEN_COMMENTS, true);
        try (ActivityScenario<CommunityPostDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.details_comments_sort))
                    .check(matches(withText(R.string.community_filter_recent))).perform(click());
            onView(withText(R.string.community_comments_oldest)).perform(click());
            onView(withText("I found a seat here right before my first class."))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.post_details_comment_input)).perform(replaceText("An unfinished reply."));
            scenario.recreate();
            onView(withId(R.id.post_details_comment_input))
                    .check(matches(withText("An unfinished reply.")));
            onView(withId(R.id.details_comments_sort))
                    .check(matches(withText(R.string.community_comments_oldest)));
        }
    }
}
