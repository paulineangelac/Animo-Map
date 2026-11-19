package com.itismob.s02.group5.animomap.ui.profile;

/**
 * One community post made by the logged-in student, as shown on their Profile.
 * Only New Place Recommendations go through moderator review, so only they have a status.
 */
public class MyPost {

    // Review status of a New Place Recommendation.
    // Keep these names the same as the post statuses used in Create Post.
    public static final String STATUS_NONE = "NONE";          // regular post, no review needed
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private final String title;
    private final String category;
    private final String postedAgo;
    private final String status;

    public MyPost(String title, String category, String postedAgo, String status) {
        this.title = title;
        this.category = category;
        this.postedAgo = postedAgo;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getPostedAgo() {
        return postedAgo;
    }

    public String getStatus() {
        return status;
    }
}