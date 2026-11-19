package com.itismob.s02.group5.animomap.ui.moderation;

import android.text.format.DateUtils;

import com.itismob.s02.group5.animomap.ui.community.CommunityFeedDataSource;
import com.itismob.s02.group5.animomap.ui.community.CommunityPost;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample moderation queue: New Place Recommendations and reported posts.
 * Each list is made once and kept while the app is open, so decisions stay after leaving the screen.
 * Prototype: Firebase will store the queue and the decisions in MCO3.
 */
public final class ModerationDataSource {

    private static List<PlaceRecommendation> recommendations;
    private static List<ReportedPost> reports;

    private ModerationDataSource() {
    }

    /*
     * RECOMMENDATIONS: the Community posts marked as place candidates.
     * They're the same posts that show "POI candidate" in the Community feed.
     */
    public static List<PlaceRecommendation> getRecommendations() {
        if (recommendations == null) {
            recommendations = new ArrayList<>();
            long now = System.currentTimeMillis();

            for (CommunityPost post : CommunityFeedDataSource.getSamplePosts()) {
                if (post.isPoiCandidate()) {
                    // Example: "38 minutes ago"
                    String submittedAgo = DateUtils.getRelativeTimeSpanString(
                            post.getCreatedAtMillis(), now, DateUtils.MINUTE_IN_MILLIS).toString();
                    recommendations.add(new PlaceRecommendation(post.getTitle(), post.getAuthorName(),
                            post.getLocationName(), post.getDescription(), submittedAgo));
                }
            }
        }
        return recommendations;
    }

    /*
     * REPORTS: posts that students reported, with the reason and how many reported them.
     */
    public static List<ReportedPost> getReports() {
        if (reports == null) {
            reports = new ArrayList<>();
            reports.add(new ReportedPost("Selling reviewers, DM me!", "Jay K.",
                    "Spam or selling", 4));
            reports.add(new ReportedPost("Shaded waiting area near the side entrance", "Carlo P.",
                    "Wrong location", 2));
            reports.add(new ReportedPost("Water refill station near the lobby", "Nina T.",
                    "Out of date: the station isn't working", 1));
        }
        return reports;
    }

    /*
     * COUNTS: how many items still need a moderator. Shown on Profile and the moderator screen.
     */
    public static int countPendingRecommendations() {
        int count = 0;
        for (PlaceRecommendation recommendation : getRecommendations()) {
            if (recommendation.isPending()) {
                count++;
            }
        }
        return count;
    }

    public static int countOpenReports() {
        int count = 0;
        for (ReportedPost report : getReports()) {
            if (report.isOpen()) {
                count++;
            }
        }
        return count;
    }
}