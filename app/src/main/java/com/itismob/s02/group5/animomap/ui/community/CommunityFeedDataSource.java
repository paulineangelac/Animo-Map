package com.itismob.s02.group5.animomap.ui.community;

import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Local demonstration data for the Community feed. Replace this source when a shared
 * persistence or backend layer is added.
 */
public final class CommunityFeedDataSource {
    // The first entries remain the sample posts. New submissions are appended below.
    private static final List<CommunityPost> sessionPosts = createSamplePosts();
    private static final int SAMPLE_POST_COUNT = sessionPosts.size();
    private static final Map<String, List<CommunityComment>> commentsByPost = createSampleComments();
    private static final Set<String> sessionUpvotes = new HashSet<>();

    public enum Category {
        RECOMMENDED(R.id.community_filter_recommended),
        RECENT(R.id.community_filter_newest),
        ALL_POSTS(R.id.community_filter_all_posts),
        POI_CANDIDATES(R.id.community_filter_poi_candidates);

        final int menuItemId;

        Category(int menuItemId) {
            this.menuItemId = menuItemId;
        }
    }

    private CommunityFeedDataSource() {
    }

    static List<CommunityPost> getSessionPosts() {
        return new ArrayList<>(sessionPosts);
    }

    static List<CommunityPost> getVisiblePosts(Category category) {
        return getPostsForCategory(sessionPosts, category, sessionUpvotes);
    }

    static ArrayList<String> getSessionUpvotes() {
        return new ArrayList<>(sessionUpvotes);
    }

    static void restoreSessionUpvotes(List<String> upvotes) {
        sessionUpvotes.addAll(upvotes);
    }

    static boolean isUpvoted(CommunityPost post) {
        return sessionUpvotes.contains(post.getId());
    }

    static void toggleUpvote(CommunityPost post) {
        toggleUpvote(sessionUpvotes, post.getId());
    }

    static int getDisplayedVoteCount(CommunityPost post) {
        return getDisplayedVoteCount(post, sessionUpvotes);
    }

    static CommunityPost findPost(String id) {
        for (CommunityPost post : sessionPosts) {
            if (post.getId().equals(id)) return post;
        }
        return null;
    }

    static List<CommunityComment> getComments(String postId) {
        List<CommunityComment> comments = commentsByPost.get(postId);
        if (comments == null) return Collections.emptyList();
        List<CommunityComment> result = new ArrayList<>(comments);
        result.sort(Comparator.comparingLong((CommunityComment comment) -> comment.createdAtMillis)
                .reversed());
        return result;
    }

    static boolean addComment(String postId, String authorName, String username, String initials,
                              String avatarUri, String text) {
        CommunityPost post = findPost(postId);
        String body = trimDraftText(text);
        if (post == null || body.isEmpty()) return false;

        List<CommunityComment> comments = commentsByPost.computeIfAbsent(postId,
                key -> new ArrayList<>());
        comments.add(0, new CommunityComment(authorName, username, initials, 0, avatarUri, body,
                System.currentTimeMillis()));
        post.setCommentCount(comments.size());
        return true;
    }

    static CommunityPost submitPost(String author, String initials, CommunityPostType type,
                                    String placeName, String description, PostLocation location,
                                    List<byte[]> photos) {
        String title = type == CommunityPostType.POI_RECOMMENDATION ? placeName : null;
        CommunityPost post = new CommunityPost(UUID.randomUUID().toString(), author, initials,
                type, title, description, location, photos, System.currentTimeMillis());
        sessionPosts.add(post);
        return post;
    }

    static String trimDraftText(String value) {
        return value.replaceAll("^[\\s\\p{Z}]+|[\\s\\p{Z}]+$", "");
    }

    static int validateDraft(CommunityPostType type, String description, String placeName,
                             boolean hasLocation) {
        if (type == null) return R.string.community_require_category;
        if (description.isEmpty()) return R.string.community_require_description;
        if (type == CommunityPostType.POI_RECOMMENDATION) {
            if (placeName.isEmpty()) return R.string.community_require_place_name;
            if (!hasLocation) return R.string.community_require_location;
        }
        return 0;
    }

    public static List<CommunityPost> getSamplePosts() {
        return new ArrayList<>(sessionPosts.subList(0, SAMPLE_POST_COUNT));
    }

    private static List<CommunityPost> createSamplePosts() {
        long now = System.currentTimeMillis();
        List<CommunityPost> posts = new ArrayList<>();

        posts.add(samplePost(
                "post_learning_commons_quiet_seats", "mika_s", "Mika S.", "MS", "CCS student",
                "dlsu_henry_sy_sr_hall_level_6", "Henry Sy Sr. Hall · Level 6",
                "Quiet seats near the Learning Commons",
                "The tables beside the windows are cool, quiet, and usually open before 10 AM.",
                R.drawable.community_photo_placeholder, 48, now - minutes(12), false, R.drawable.community_avatar_placeholder));

        posts.add(samplePost(
                "post_velasco_covered_benches", "bea_r", "Bea R.", "BR", "COS student",
                "dlsu_velasco_hall_south_side", "Velasco Hall · South side",
                "Covered benches with charging nearby",
                "Good for short waits between classes. Moderate noise at lunchtime.",
                0, 94, now - minutes(38), true, 0));

        posts.add(samplePost(
                "post_goks_lounge", "luis_d", "Luis D.", "LD", "COB student",
                "dlsu_gokongwei_lounge_level_3", "Gokongwei Hall · Level 3 lounge",
                "A calm spot between afternoon classes",
                "The lounge has a few open seats after lunch and is close to the elevators.",
                R.drawable.community_photo_placeholder, 72, now - minutes(54), true, 0));

        posts.add(samplePost(
                "post_andrew_gonzalez_study_nook", "ana_m", "Ana M.", "AM", "CLA student",
                "dlsu_andrew_gonzalez_hall_level_7", "Br. Andrew Gonzalez Hall · Level 7",
                "Small study nook away from the elevators",
                "There are two tables at the end of the hallway. It is usually quieter after 3 PM.",
                0, 63, now - minutes(75), false, 0));

        posts.add(samplePost(
                "post_yuchengco_waiting_area", "carlo_p", "Carlo P.", "CP", null,
                "dlsu_yuchengco_hall_ground_floor", "Yuchengco Hall · Ground floor",
                "Shaded waiting area near the side entrance",
                "A handy place to wait for friends when it rains. Please keep the walkway clear.",
                0, 41, now - minutes(145), false, 0));

        posts.add(samplePost(
                "post_miguel_hall_water_refill", "nina_t", "Nina T.", "NT", "CCS student",
                "dlsu_miguel_hall_ground_floor", "Miguel Hall · Ground floor",
                "Water refill station near the lobby",
                "The refill station is beside the lobby seating area and was working this morning.",
                0, 27, now - minutes(240), false, 0));

        return posts;
    }

    private static CommunityPost samplePost(String id, String authorId, String authorName,
                                            String initials, String affiliation, String locationId,
                                            String locationName, String title, String description,
                                            int image, int votes, long createdAt,
                                            boolean candidate, int avatar) {
        CommunityPostType type = candidate ? CommunityPostType.POI_RECOMMENDATION : null;
        return new CommunityPost(id, authorId, authorName, initials, affiliation, locationId,
                locationName, title, description, image, votes, 0, createdAt, avatar,
                type, null, Collections.emptyList());
    }

    private static Map<String, List<CommunityComment>> createSampleComments() {
        Map<String, List<CommunityComment>> comments = new HashMap<>();
        // Match the original feed totals with sample conversations for the six sample posts.
        seedComments(comments, "post_learning_commons_quiet_seats",
                "I found a seat here right before my first class.",
                "The window side is my favorite too.",
                "Is there an outlet near the corner?",
                "There are a couple along the wall.",
                "Thanks! I'll check those tables tomorrow.",
                "It tends to fill up during lunch.",
                "This helped me find a spot for a quick review session.",
                "Bring a sweater if you're sitting there for a while.",
                "The table by the aisle was free this morning.",
                "Good spot for reading between classes.",
                "I usually go before 9 and it is still quiet.",
                "Are the seats okay for a pair studying together?",
                "Yes, if you both work quietly.",
                "The far end has the best natural light.",
                "Saved this for finals week.",
                "The seats were full when I got there after lunch.",
                "Early is definitely better on Mondays.",
                "Found a seat there today. Thanks for the tip!");
        seedComments(comments, "post_velasco_covered_benches",
                "Useful when it starts raining between classes.",
                "The bench at the far end was open this afternoon.",
                "Are the outlets beside the benches?",
                "Look near the wall on the south side.",
                "There's more foot traffic around lunchtime.",
                "I wait here when my next class is in Velasco.",
                "Good to know there's a covered spot nearby.",
                "It gets breezy when it rains.",
                "I found space here after my last class.",
                "Handy for a short wait.",
                "Thanks for including which side of the building.");
        seedComments(comments, "post_goks_lounge",
                "This worked well before my afternoon lab.",
                "It gets busy right after classes end.",
                "How close is it to the elevators?",
                "It's just off the elevator landing.",
                "I found an open seat there after 3.",
                "Good place to wait for a groupmate.",
                "Thanks for the floor number!");
        seedComments(comments, "post_andrew_gonzalez_study_nook",
                "Nice spot to finish a reading before class.",
                "There was space there after 3 today.",
                "The corner seats are quieter than the ones by the elevators.",
                "Is there room for a group?",
                "I'd use it for solo study or a pair.");
        seedComments(comments, "post_yuchengco_waiting_area",
                "Convenient for meeting friends before lunch.",
                "There was plenty of shade in the afternoon.",
                "Good reminder to keep the walkway clear.");
        seedComments(comments, "post_miguel_hall_water_refill",
                "I found it next to the lobby seats. Thanks!",
                "It was working after lunch too.");
        return comments;
    }

    private static void seedComments(Map<String, List<CommunityComment>> comments, String postId,
                                     String... messages) {
        CommunityPost post = findPost(postId);
        List<CommunityComment> entries = new ArrayList<>();
        for (int i = 0; i < messages.length; i++) {
            int authorIndex = (sessionPosts.indexOf(post) + 1 + i % (SAMPLE_POST_COUNT - 1))
                    % SAMPLE_POST_COUNT;
            CommunityPost author = sessionPosts.get(authorIndex);
            long createdAt = post.getCreatedAtMillis() + (i + 1) * minutes(1) / messages.length;
            entries.add(new CommunityComment(author.getAuthorName(), author.getAuthorId(),
                    author.getAuthorInitials(), author.getAvatarResourceId(), null,
                    messages[i], createdAt));
        }
        comments.put(postId, entries);
        post.setCommentCount(entries.size());
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
            result.sort((first, second) -> {
                int voteComparison = Integer.compare(
                        getDisplayedVoteCount(second, activeUpvotes),
                        getDisplayedVoteCount(first, activeUpvotes));
                return voteComparison != 0 ? voteComparison : byNewest.compare(first, second);
            });
        } else {
            result.sort(byNewest);
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
