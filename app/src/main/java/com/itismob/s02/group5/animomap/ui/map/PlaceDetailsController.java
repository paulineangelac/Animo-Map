package com.itismob.s02.group5.animomap.ui.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fills the details part of the map place card: description, average rating, the student's own
 * star rating, recent reviews (expandable), and the write-a-review box.
 * Data comes from {@link PlaceReviewsDataSource}.
 */
public class PlaceDetailsController {
    /** Reviews shown before the student expands the list. */
    private static final int COLLAPSED_REVIEWS = 2;

    private final Context context;
    private final TextView description, ratingSummary, reviewsEmpty, reviewsToggle;
    private final View feedback;
    private final LinearLayout starsRow, reviewsList;
    private final EditText reviewInput;
    private final TextView[] starViews = new TextView[5];
    private MapPlace place;
    private boolean expanded;

    public PlaceDetailsController(View root) {
        context = root.getContext();
        description = root.findViewById(R.id.tv_map_place_description);
        ratingSummary = root.findViewById(R.id.tv_map_place_rating);
        feedback = root.findViewById(R.id.map_place_feedback);
        starsRow = root.findViewById(R.id.ll_map_rating_stars);
        reviewsList = root.findViewById(R.id.ll_map_reviews);
        reviewsEmpty = root.findViewById(R.id.tv_map_reviews_empty);
        reviewsToggle = root.findViewById(R.id.tv_map_reviews_toggle);
        reviewInput = root.findViewById(R.id.et_map_review);
        createStarButtons();
        reviewsToggle.setOnClickListener(view -> {
            expanded = !expanded;
            showReviews();
        });
        root.findViewById(R.id.btn_map_submit_review).setOnClickListener(view -> submitReview());
    }

    /** Shows the details of a place. Pass visible = false to hide them (for example in demo-route mode). */
    public void bind(MapPlace newPlace, boolean visible) {
        if (place != newPlace) {
            place = newPlace;
            expanded = false;
            reviewInput.setText("");
        }
        int visibility = visible && place != null ? View.VISIBLE : View.GONE;
        feedback.setVisibility(visibility);
        ratingSummary.setVisibility(visibility);
        if (visibility == View.GONE) {
            description.setVisibility(View.GONE);
            return;
        }
        description.setText(place.getDescription());
        description.setVisibility(place.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
        showRatingSummary();
        showMyRating();
        showReviews();
    }

    private void createStarButtons() {
        int size = (int) (48 * context.getResources().getDisplayMetrics().density);
        for (int i = 0; i < starViews.length; i++) {
            final int stars = i + 1;
            TextView star = new TextView(context);
            star.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            star.setGravity(android.view.Gravity.CENTER);
            star.setTextSize(26);
            star.setContentDescription(context.getString(R.string.place_star_description, stars));
            star.setBackgroundResource(android.R.color.transparent);
            star.setOnClickListener(view -> {
                if (place == null) { return; }
                PlaceReviewsDataSource.rate(place.getId(), stars);
                showRatingSummary();
                showMyRating();
                showReviews();
            });
            starViews[i] = star;
            starsRow.addView(star);
        }
    }

    private void showRatingSummary() {
        int count = PlaceReviewsDataSource.getCount(place.getId());
        if (count == 0) {
            ratingSummary.setText(R.string.place_no_reviews);
        } else {
            ratingSummary.setText(context.getResources().getQuantityString(
                    R.plurals.place_rating_summary, count,
                    PlaceReviewsDataSource.getAverage(place.getId()), count));
        }
    }

    private void showMyRating() {
        int mine = PlaceReviewsDataSource.getMyRating(place.getId());
        int filled = ContextCompat.getColor(context, R.color.accent_teal);
        int empty = ContextCompat.getColor(context, R.color.text_secondary);
        for (int i = 0; i < starViews.length; i++) {
            boolean isFilled = i < mine;
            starViews[i].setText(isFilled ? "★" : "☆");
            starViews[i].setTextColor(isFilled ? filled : empty);
        }
    }

    /** Lists the written reviews: two at first, all of them once expanded. */
    private void showReviews() {
        List<PlaceReview> written = new ArrayList<>();
        for (PlaceReview review : PlaceReviewsDataSource.getReviews(place.getId())) {
            if (!review.getComment().isEmpty()) { written.add(review); }
        }
        reviewsList.removeAllViews();
        int shown = expanded ? written.size() : Math.min(COLLAPSED_REVIEWS, written.size());
        LayoutInflater inflater = LayoutInflater.from(context);
        for (int i = 0; i < shown; i++) {
            PlaceReview review = written.get(i);
            View row = inflater.inflate(R.layout.item_map_review, reviewsList, false);
            ((TextView) row.findViewById(R.id.tv_review_meta)).setText(context.getString(
                    R.string.place_review_meta, review.getAuthor(), review.getStars(), review.getTimeAgo()));
            ((TextView) row.findViewById(R.id.tv_review_comment)).setText(review.getComment());
            reviewsList.addView(row);
        }
        reviewsEmpty.setVisibility(written.isEmpty() ? View.VISIBLE : View.GONE);
        boolean canExpand = written.size() > COLLAPSED_REVIEWS;
        reviewsToggle.setVisibility(canExpand ? View.VISIBLE : View.GONE);
        reviewsToggle.setText(expanded ? context.getString(R.string.place_show_fewer_reviews)
                : context.getString(R.string.place_show_all_reviews, written.size()));
    }

    private void submitReview() {
        if (place == null) { return; }
        String text = reviewInput.getText().toString().trim();
        if (PlaceReviewsDataSource.getMyRating(place.getId()) == 0) {
            Toast.makeText(context, R.string.place_review_need_rating, Toast.LENGTH_SHORT).show();
        } else if (text.isEmpty()) {
            Toast.makeText(context, R.string.place_review_need_text, Toast.LENGTH_SHORT).show();
        } else if (PlaceReviewsDataSource.submitReview(place.getId(), text)) {
            reviewInput.setText("");
            hideKeyboard();
            Toast.makeText(context, R.string.place_review_posted, Toast.LENGTH_SHORT).show();
            showRatingSummary();
            showReviews();
        }
    }

    private void hideKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) { keyboard.hideSoftInputFromWindow(reviewInput.getWindowToken(), 0); }
        reviewInput.clearFocus();
    }
}
