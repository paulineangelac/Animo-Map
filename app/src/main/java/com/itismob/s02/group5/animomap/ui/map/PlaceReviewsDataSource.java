package com.itismob.s02.group5.animomap.ui.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample ratings and reviews for places on the map.
 * Prototype: they live in memory only (lost when the app closes). Firebase will load and save the
 * real ratings and reviews in MCO3, so the screens only talk to the methods of this class.
 */
public final class PlaceReviewsDataSource {
    private static final Map<String, List<PlaceReview>> REVIEWS = createSampleReviews();

    private PlaceReviewsDataSource() { }

    /** Every rating of a place, newest first. Includes ratings without written text. */
    public static List<PlaceReview> getReviews(String placeId) {
        List<PlaceReview> reviews = REVIEWS.get(placeId);
        return reviews == null ? new ArrayList<>() : new ArrayList<>(reviews);
    }
    public static int getCount(String placeId) { return getReviews(placeId).size(); }
    /** Average rating from 1 to 5, or 0 when the place has no ratings. */
    public static double getAverage(String placeId) {
        List<PlaceReview> reviews = getReviews(placeId);
        if (reviews.isEmpty()) { return 0; }
        int total = 0;
        for (PlaceReview review : reviews) { total += review.getRating(); }
        return (double) total / reviews.size();
    }
    /** The logged-in student's own star rating of a place, or 0 if they have not rated it. */
    public static int getMyRating(String placeId) {
        PlaceReview mine = findMine(placeId);
        return mine == null ? 0 : mine.getRating();
    }
    /** Saves the student's star rating (1 to 5), keeping any review text they already wrote. */
    public static void rate(String placeId, int stars) {
        if (stars < 1 || stars > 5) { return; }
        PlaceReview mine = findMine(placeId);
        saveMine(placeId, new PlaceReview(PlaceReview.ME, stars,
                mine == null ? "" : mine.getComment(), "Just now"));
    }
    /**
     * Saves the student's written review using their current star rating.
     * Returns false when the text is empty or they have not rated the place yet.
     */
    public static boolean submitReview(String placeId, String comment) {
        String text = comment == null ? "" : comment.trim();
        int rating = getMyRating(placeId);
        if (text.isEmpty() || rating == 0) { return false; }
        saveMine(placeId, new PlaceReview(PlaceReview.ME, rating, text, "Just now"));
        return true;
    }

    private static PlaceReview findMine(String placeId) {
        for (PlaceReview review : getReviews(placeId)) {
            if (review.isMine()) { return review; }
        }
        return null;
    }
    /** Replaces the student's previous entry (if any) and puts the new one first. */
    private static void saveMine(String placeId, PlaceReview mine) {
        List<PlaceReview> reviews = getReviews(placeId);
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).isMine()) { reviews.remove(i); break; }
        }
        reviews.add(0, mine);
        REVIEWS.put(placeId, reviews);
    }

    private static Map<String, List<PlaceReview>> createSampleReviews() {
        Map<String, List<PlaceReview>> data = new HashMap<>();
        add(data, "henry_sy",
                new PlaceReview("Mika R.", 5, "Plenty of outlets and the library floors are quiet.", "2 days ago"),
                new PlaceReview("Paolo D.", 4, "Elevators get crowded around noon.", "1 week ago"),
                new PlaceReview("Ana C.", 4, "", "2 weeks ago"));
        add(data, "gokongwei",
                new PlaceReview("Jessa L.", 4, "Easy to find your room, rooms are cold though.", "3 days ago"),
                new PlaceReview("Migs T.", 3, "Hallways are packed between classes.", "1 week ago"));
        add(data, "st_lasalle",
                new PlaceReview("Carlo V.", 5, "Beautiful building and a good meeting spot.", "4 days ago"),
                new PlaceReview("Bea S.", 5, "", "3 weeks ago"));
        add(data, "agno_food_court",
                new PlaceReview("Dan F.", 4, "Big servings for the price. Long lines at lunch.", "1 day ago"),
                new PlaceReview("Lia M.", 5, "Best place for a quick meal between classes.", "5 days ago"),
                new PlaceReview("Josh P.", 3, "Seats fill up fast.", "2 weeks ago"),
                new PlaceReview("Ria G.", 4, "", "1 month ago"));
        add(data, "bloemen_food",
                new PlaceReview("Kyle B.", 3, "Okay snacks, limited seating.", "1 week ago"));
        add(data, "henry_sy_library",
                new PlaceReview("Tina O.", 5, "Quiet and has group study rooms you can reserve.", "2 days ago"),
                new PlaceReview("Ben N.", 5, "My go-to place during finals week.", "1 week ago"));
        add(data, "velasco_patio",
                new PlaceReview("Sam K.", 4, "Nice breeze and shaded seats while waiting for class.", "6 days ago"));
        add(data, "gokongwei_restroom",
                new PlaceReview("Ivy H.", 4, "Clean and rarely crowded.", "3 days ago"));
        return data;
    }
    /** Sample entries are written oldest-last, so they are already in newest-first order. */
    private static void add(Map<String, List<PlaceReview>> data, String placeId, PlaceReview... reviews) {
        List<PlaceReview> list = new ArrayList<>();
        for (PlaceReview review : reviews) { list.add(review); }
        data.put(placeId, list);
    }
}
