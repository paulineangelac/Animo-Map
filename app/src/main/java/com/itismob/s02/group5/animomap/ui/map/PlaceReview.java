package com.itismob.s02.group5.animomap.ui.map;

/** One student's rating (and optional written review) for a place. */
public class PlaceReview {
    /** Author name used for the logged-in student's own rating and review. */
    public static final String ME = "You";

    private final String author;
    private final int rating;       // 1 to 5 stars
    private final String comment;   // "" when the student only rated
    private final String timeAgo;

    public PlaceReview(String author, int rating, String comment, String timeAgo) {
        this.author = author;
        this.rating = rating;
        this.comment = comment == null ? "" : comment;
        this.timeAgo = timeAgo;
    }

    public String getAuthor() { return author; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getTimeAgo() { return timeAgo; }
    public boolean isMine() { return ME.equals(author); }

    /** The rating as stars, e.g. 4 becomes "★★★★☆". */
    public String getStars() { return stars(rating); }

    public static String stars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) { stars.append(i <= rating ? "★" : "☆"); }
        return stars.toString();
    }
}
