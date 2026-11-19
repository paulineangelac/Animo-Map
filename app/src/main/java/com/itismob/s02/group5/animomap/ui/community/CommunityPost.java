package com.itismob.s02.group5.animomap.ui.community;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommunityPost {
    enum MapReviewStatus { NOT_APPLICABLE, PENDING }

    private final String id;
    private final String authorName;
    private final String authorInitials;
    private final int avatarResourceId;
    private final String studentAffiliation;
    private final String locationId;
    private final String locationName;
    private final String title;
    private final String description;
    private final int imageResourceId;
    private final int upvoteCount;
    private final int commentCount;
    private final long createdAtMillis;
    private final MapReviewStatus mapReviewStatus;
    private final CommunityPostType postType;
    private final PostLocation location;
    private final List<byte[]> photos;

    public CommunityPost(String id, String authorName, String authorInitials,
                         String studentAffiliation, String locationId, String locationName,
                         String title, String description, int imageResourceId,
                         int upvoteCount, int commentCount, long createdAtMillis,
                         boolean poiCandidate) {
        this(id, authorName, authorInitials, studentAffiliation, locationId, locationName,
                title, description, imageResourceId, upvoteCount, commentCount, createdAtMillis,
                poiCandidate, 0);
    }

    public CommunityPost(String id, String authorName, String authorInitials,
                         String studentAffiliation, String locationId, String locationName,
                         String title, String description, int imageResourceId,
                         int upvoteCount, int commentCount, long createdAtMillis,
                         boolean poiCandidate, int avatarResourceId) {
        this(id, authorName, authorInitials, studentAffiliation, locationId, locationName,
                title, description, imageResourceId, upvoteCount, commentCount, createdAtMillis,
                avatarResourceId, poiCandidate ? CommunityPostType.POI_RECOMMENDATION : null,
                null, Collections.emptyList());
    }

    CommunityPost(String id, String authorName, String authorInitials, CommunityPostType type,
                  String title, String description, PostLocation location, List<byte[]> photos,
                  long createdAtMillis) {
        this(id, authorName, authorInitials, null, location == null ? null : location.placeId,
                location == null ? null : location.name, title, description, 0, 0, 0, createdAtMillis,
                0, type, location, photos);
    }

    private CommunityPost(String id, String authorName, String authorInitials,
                          String studentAffiliation, String locationId, String locationName,
                          String title, String description, int imageResourceId, int upvoteCount,
                          int commentCount, long createdAtMillis, int avatarResourceId,
                          CommunityPostType type, PostLocation location, List<byte[]> photos) {
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
        this.avatarResourceId = avatarResourceId;
        postType = type;
        this.location = location;
        this.photos = Collections.unmodifiableList(new ArrayList<>(photos));
        mapReviewStatus = type == CommunityPostType.POI_RECOMMENDATION
                ? MapReviewStatus.PENDING : MapReviewStatus.NOT_APPLICABLE;
    }

    public int getAvatarResourceId() {
        return avatarResourceId;
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
        return postType == CommunityPostType.POI_RECOMMENDATION;
    }

    CommunityPostType getPostType() { return postType; }
    PostLocation getLocation() { return location; }
    List<byte[]> getPhotos() { return photos; }
    MapReviewStatus getMapReviewStatus() { return mapReviewStatus; }
}
