package com.itismob.s02.group5.animomap.ui.community;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Local demonstration data for the Community feed. Replace this source when a shared
 * persistence or backend layer is added.
 */
public final class CommunityFeedDataSource {

    public enum Category {
        RECOMMENDED,
        RECENT,
        POI_CANDIDATES
    }

    private CommunityFeedDataSource() {
    }

    public static List<CommunityPost> getSamplePosts() {
        long now = System.currentTimeMillis();
        List<CommunityPost> posts = new ArrayList<>();

        posts.add(new CommunityPost(
                "post_learning_commons_quiet_seats", "Mika S.", "MS", "CCS student",
                "dlsu_henry_sy_sr_hall_level_6", "Henry Sy Sr. Hall · Level 6",
                "Quiet seats near the Learning Commons",
                "The tables beside the windows are cool, quiet, and usually open before 10 AM.",
                0, 128, 18, now - minutes(12), false));

        posts.add(new CommunityPost(
                "post_velasco_covered_benches", "Bea R.", "BR", "COS student",
                "dlsu_velasco_hall_south_side", "Velasco Hall · South side",
                "Covered benches with charging nearby",
                "Good for short waits between classes. Moderate noise at lunchtime.",
                0, 94, 11, now - minutes(38), true));

        posts.add(new CommunityPost(
                "post_goks_lounge", "Luis D.", "LD", "COB student",
                "dlsu_gokongwei_lounge_level_3", "Gokongwei Hall · Level 3 lounge",
                "A calm spot between afternoon classes",
                "The lounge has a few open seats after lunch and is close to the elevators.",
                0, 72, 7, now - minutes(54), true));

        posts.add(new CommunityPost(
                "post_andrew_gonzalez_study_nook", "Ana M.", "AM", "CLA student",
                "dlsu_andrew_gonzalez_hall_level_7", "Br. Andrew Gonzalez Hall · Level 7",
                "Small study nook away from the elevators",
                "There are two tables at the end of the hallway. It is usually quieter after 3 PM.",
                0, 63, 5, now - minutes(75), false));

        posts.add(new CommunityPost(
                "post_yuchengco_waiting_area", "Carlo P.", "CP", null,
                "dlsu_yuchengco_hall_ground_floor", "Yuchengco Hall · Ground floor",
                "Shaded waiting area near the side entrance",
                "A handy place to wait for friends when it rains. Please keep the walkway clear.",
                0, 41, 3, now - minutes(145), false));

        posts.add(new CommunityPost(
                "post_miguel_hall_water_refill", "Nina T.", "NT", "CCS student",
                "dlsu_miguel_hall_ground_floor", "Miguel Hall · Ground floor",
                "Water refill station near the lobby",
                "The refill station is beside the lobby seating area and was working this morning.",
                0, 27, 2, now - minutes(240), false));

        return posts;
    }

    public static List<CommunityPost> getPostsForCategory(List<CommunityPost> posts,
                                                           Category category,
                                                           Set<String> activeUpvotes) {
        List<CommunityPost> result = new ArrayList<>();
        for (CommunityPost post : posts) {
            if (category != Category.POI_CANDIDATES || post.isPoiCandidate()) {
                result.add(post);
            }
        }

        Comparator<CommunityPost> byNewest = (first, second) ->
                Long.compare(second.getCreatedAtMillis(), first.getCreatedAtMillis());

        if (category == Category.RECOMMENDED) {
            Collections.sort(result, (first, second) -> {
                int voteComparison = Integer.compare(
                        getDisplayedVoteCount(second, activeUpvotes),
                        getDisplayedVoteCount(first, activeUpvotes));
                return voteComparison != 0 ? voteComparison : byNewest.compare(first, second);
            });
        } else {
            Collections.sort(result, byNewest);
        }
        return result;
    }

    public static boolean toggleUpvote(Set<String> activeUpvotes, String postId) {
        if (activeUpvotes.contains(postId)) {
            activeUpvotes.remove(postId);
            return false;
        }
        activeUpvotes.add(postId);
        return true;
    }

    public static int getDisplayedVoteCount(CommunityPost post, Set<String> activeUpvotes) {
        return post.getUpvoteCount() + (activeUpvotes.contains(post.getId()) ? 1 : 0);
    }

    private static long minutes(long minutes) {
        return minutes * 60L * 1000L;
    }
}
