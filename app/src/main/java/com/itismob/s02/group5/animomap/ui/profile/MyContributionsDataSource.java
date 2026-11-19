package com.itismob.s02.group5.animomap.ui.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample posts and reviews for the Profile tab.
 * Prototype: Firebase will load the student's real posts and reviews in MCO3.
 */
public final class MyContributionsDataSource {

    private MyContributionsDataSource() {
    }

    /*
     * The student's posts, newest first. New Place Recommendations show their review status.
     */
    public static List<MyPost> getMyPosts() {
        List<MyPost> posts = new ArrayList<>();
        posts.add(new MyPost("Quiet study nook, Henry Sy 7F", "New Place Recommendation",
                "2 days ago", MyPost.STATUS_PENDING));
        posts.add(new MyPost("A quiet place to study", "Campus Tips & Discoveries",
                "3 days ago", MyPost.STATUS_NONE));
        posts.add(new MyPost("Lugaw stall on Agno St.", "New Place Recommendation",
                "1 week ago", MyPost.STATUS_APPROVED));
        posts.add(new MyPost("Andrew Hall rooftop", "New Place Recommendation",
                "2 weeks ago", MyPost.STATUS_REJECTED));
        return posts;
    }

    /*
     * The student's place reviews, newest first.
     */
    public static List<MyReview> getMyReviews() {
        List<MyReview> reviews = new ArrayList<>();
        reviews.add(new MyReview("Henry Sy Sr. Hall", "Study space", 4, "Comfortable and quiet."));
        reviews.add(new MyReview("Gokongwei Hall lounge", "Hangout area", 3, "Gets noisy at lunch."));
        return reviews;
    }
}