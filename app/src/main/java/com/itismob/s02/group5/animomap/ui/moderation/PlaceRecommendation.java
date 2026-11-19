package com.itismob.s02.group5.animomap.ui.moderation;

import com.itismob.s02.group5.animomap.ui.profile.MyPost;

/**
 * A New Place Recommendation in the moderator's review list.
 * It uses the same status names as MyPost, so Profile and the moderator tools match.
 */
public class PlaceRecommendation {

    private final String title;
    private final String authorName;
    private final String location;
    private final String description;
    private final String submittedAgo;
    private String status;   // MyPost.STATUS_PENDING, STATUS_APPROVED, or STATUS_REJECTED

    public PlaceRecommendation(String title, String authorName, String location,
                               String description, String submittedAgo) {
        this.title = title;
        this.authorName = authorName;
        this.location = location;
        this.description = description;
        this.submittedAgo = submittedAgo;
        this.status = MyPost.STATUS_PENDING;   // every recommendation starts as Pending
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getSubmittedAgo() {
        return submittedAgo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPending() {
        return MyPost.STATUS_PENDING.equals(status);
    }
}