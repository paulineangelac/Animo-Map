package com.itismob.s02.group5.animomap.ui.profile;

/**
 * One place review written by the logged-in student, as shown on their Profile.
 */
public class MyReview {

    private final String placeName;
    private final String placeType;
    private final int rating;          // 1 to 5 stars
    private final String comment;

    public MyReview(String placeName, String placeType, int rating, String comment) {
        this.placeName = placeName;
        this.placeType = placeType;
        this.rating = rating;
        this.comment = comment;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getPlaceType() {
        return placeType;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    /*
     * The rating as stars, e.g. 4 becomes "★★★★☆".
     */
    public String getStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }
        return stars.toString();
    }
}