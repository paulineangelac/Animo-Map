package com.itismob.s02.group5.animomap.ui.map;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fills the details part of the map place card. The average rating is always visible; the
 * description, the student's own star rating, recent reviews (expandable), and the
 * write-a-review button sit behind a "Details and reviews" toggle so the card stays short.
 * Data comes from {@link PlaceReviewsDataSource}.
 */
public class PlaceDetailsController {
    /** Reviews shown before the student expands the list. */
    private static final int COLLAPSED_REVIEWS = 2;

    private final Context context;
    private final TextView description, ratingSummary, reviewsEmpty, reviewsToggle, detailsToggle;
    private final View detailsGroup;
    private final LinearLayout starsRow, reviewsList;
    private final TextView[] starViews = new TextView[5];
    private MapPlace place;
    private boolean detailsOpen;
    private boolean expanded;
    /** Called whenever the card's content gets taller or shorter, so the card can resize. */
    private Runnable onContentChanged;

    public PlaceDetailsController(View root) {
        context = root.getContext();
        description = root.findViewById(R.id.tv_map_place_description);
        ratingSummary = root.findViewById(R.id.tv_map_place_rating);
        detailsGroup = root.findViewById(R.id.map_place_expanded);
        detailsToggle = root.findViewById(R.id.tv_map_details_toggle);
        starsRow = root.findViewById(R.id.ll_map_rating_stars);
        reviewsList = root.findViewById(R.id.ll_map_reviews);
        reviewsEmpty = root.findViewById(R.id.tv_map_reviews_empty);
        reviewsToggle = root.findViewById(R.id.tv_map_reviews_toggle);
        createStarButtons();
        detailsToggle.setOnClickListener(view -> {
            detailsOpen = !detailsOpen;
            showDetailsGroup();
            notifyContentChanged();
        });
        reviewsToggle.setOnClickListener(view -> {
            expanded = !expanded;
            showReviews();
            notifyContentChanged();
        });
        root.findViewById(R.id.btn_map_write_review).setOnClickListener(view -> openReviewDialog());
    }

    public void setOnContentChangedListener(Runnable listener) {
        onContentChanged = listener;
    }

    private void notifyContentChanged() {
        if (onContentChanged != null) { onContentChanged.run(); }
    }

    /** Shows the details of a place. Pass visible = false to hide them (for example in demo-route mode). */
    public void bind(MapPlace newPlace, boolean visible) {
        if (place != newPlace) {
            place = newPlace;
            expanded = false;
            detailsOpen = false;
        }
        boolean show = visible && place != null;
        ratingSummary.setVisibility(show ? View.VISIBLE : View.GONE);
        detailsToggle.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            detailsGroup.setVisibility(View.GONE);
            return;
        }
        description.setText(place.getDescription());
        description.setVisibility(place.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
        showRatingSummary();
        showMyRating();
        showReviews();
        showDetailsGroup();
    }

    /** Opens or closes the part of the card below the toggle, and updates the toggle's label. */
    private void showDetailsGroup() {
        detailsGroup.setVisibility(detailsOpen ? View.VISIBLE : View.GONE);
        detailsToggle.setText(detailsOpen ? R.string.place_details_hide : R.string.place_details_show);
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

    /** Opens the write-a-review dialog. A star rating comes first, because a review uses it. */
    private void openReviewDialog() {
        if (place == null) { return; }
        int rating = PlaceReviewsDataSource.getMyRating(place.getId());
        if (rating == 0) {
            Toast.makeText(context, R.string.place_review_need_rating, Toast.LENGTH_SHORT).show();
            return;
        }
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_write_review, null);
        ((TextView) dialogView.findViewById(R.id.tv_review_dialog_title)).setText(
                context.getString(R.string.place_review_dialog_title, place.getName()));
        ((TextView) dialogView.findViewById(R.id.tv_review_dialog_rating)).setText(
                context.getString(R.string.place_review_dialog_rating, rating));
        EditText input = dialogView.findViewById(R.id.et_review_dialog);

        AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialogView.findViewById(R.id.btn_review_dialog_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_review_dialog_submit).setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) {
                // Keep the dialog open and point at the field, so the student does not lose it.
                input.setError(context.getString(R.string.place_review_need_text));
            } else if (PlaceReviewsDataSource.submitReview(place.getId(), text)) {
                dialog.dismiss();
                Toast.makeText(context, R.string.place_review_posted, Toast.LENGTH_SHORT).show();
                showRatingSummary();
                showReviews();
                notifyContentChanged();
            }
        });
        dialog.show();
    }
}
