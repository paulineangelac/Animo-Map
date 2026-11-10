package com.itismob.s02.group5.animomap.ui.community;

public class CommunityPost {

    private final String id;
    private final String authorName;
    private final String authorInitials;
    private final String studentAffiliation;
    private final String locationId;
    private final String locationName;
    private final String title;
    private final String description;
    private final int imageResourceId;
    private final int upvoteCount;
    private final int commentCount;
    private final long createdAtMillis;
    private final boolean poiCandidate;

    public CommunityPost(String id, String authorName, String authorInitials,
                         String studentAffiliation, String locationId, String locationName,
                         String title, String description, int imageResourceId,
                         int upvoteCount, int commentCount, long createdAtMillis,
                         boolean poiCandidate) {
        this.id = id;
        this.authorName = authorName;
        this.authorInitials = authorInitials;
        this.studentAffiliation = studentAffiliation;
        this.locationId = locationId;
        this.locationName = locationName;
        this.title = title;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.upvoteCount = upvoteCount;
        this.commentCount = commentCount;
        this.createdAtMillis = createdAtMillis;
        this.poiCandidate = poiCandidate;
    }

    public String getId() {
        return id;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorInitials() {
        return authorInitials;
    }

    public String getStudentAffiliation() {
        return studentAffiliation;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public boolean isPoiCandidate() {
        return poiCandidate;
    }
}
