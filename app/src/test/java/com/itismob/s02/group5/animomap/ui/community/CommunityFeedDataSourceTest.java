package com.itismob.s02.group5.animomap.ui.community;

import org.junit.Test;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.map.CampusMapData;
import com.itismob.s02.group5.animomap.ui.map.MapPlace;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CommunityFeedDataSourceTest {

    @Test
    public void composerRequiresCategoryDescriptionAndPoiDetails() {
        assertEquals(R.string.community_require_category,
                CommunityFeedDataSource.validateDraft(null, "Campus tip", "", false));
        String blank = CommunityFeedDataSource.trimDraftText("\u00a0 \t\n");
        assertEquals(R.string.community_require_description, CommunityFeedDataSource.validateDraft(
                CommunityPostType.CAMPUS_TIPS, blank, "", false));
        assertEquals(R.string.community_require_place_name, CommunityFeedDataSource.validateDraft(
                CommunityPostType.POI_RECOMMENDATION, "A quiet spot", "", true));
        assertEquals(R.string.community_require_location, CommunityFeedDataSource.validateDraft(
                CommunityPostType.POI_RECOMMENDATION, "A quiet spot", "Study nook", false));
        assertEquals(0, CommunityFeedDataSource.validateDraft(
                CommunityPostType.GENERAL_DISCUSSION, "Campus tip", "", false));
    }

    @Test
    public void publishingPoiKeepsItInSessionAndDoesNotChangeCampusPlaces() {
        List<?> campusPlaces = CampusMapData.getPlaces();
        int initialPlaceCount = campusPlaces.size();
        MapPlace lasalle = CampusMapData.findPlace(CampusMapData.LASALLE);
        assertNotNull(lasalle);
        PostLocation location = PostLocation.fromPlace(lasalle);
        CommunityPost post = CommunityFeedDataSource.submitPost("@alexsantos", "AS",
                CommunityPostType.POI_RECOMMENDATION, "Study nook", "Two quiet tables.",
                location, Collections.emptyList());

        List<CommunityPost> candidates = CommunityFeedDataSource.getPostsForCategory(
                CommunityFeedDataSource.getSessionPosts(),
                CommunityFeedDataSource.Category.POI_CANDIDATES, Collections.emptySet());
        assertEquals(post.getId(), candidates.get(0).getId());
        assertEquals(post, CommunityFeedDataSource.findPost(post.getId()));
        assertEquals(CommunityPost.MapReviewStatus.PENDING, post.getMapReviewStatus());
        assertEquals(0, post.getUpvoteCount());
        assertEquals(0, post.getCommentCount());
        assertEquals(CampusMapData.LASALLE, post.getLocation().placeId);
        assertEquals(initialPlaceCount, CampusMapData.getPlaces().size());
    }

    @Test
    public void sampleAndSessionSnapshotsKeepTheSameUniqueSeedPosts() {
        List<CommunityPost> samples = CommunityFeedDataSource.getSamplePosts();
        List<CommunityPost> session = CommunityFeedDataSource.getSessionPosts();
        assertFalse(samples.isEmpty());
        Set<String> postIds = new HashSet<>();
        Set<String> authorIds = new HashSet<>();
        for (int i = 0; i < samples.size(); i++) {
            CommunityPost sample = samples.get(i);
            assertTrue(postIds.add(sample.getId()));
            assertTrue(authorIds.add(sample.getAuthorId()));
            assertSame(sample, session.get(i));
            assertSame(sample, CommunityFeedDataSource.findPost(sample.getId()));
        }

        List<CommunityPost> secondRead = CommunityFeedDataSource.getSamplePosts();
        assertEquals(samples, secondRead);
        secondRead.clear();
        session.clear();
        assertEquals(samples, CommunityFeedDataSource.getSamplePosts());
        assertEquals(samples.get(0), CommunityFeedDataSource.getSessionPosts().get(0));
    }

    @Test
    public void newPostsHaveDifferentIdsAndShareTheComposerAuthor() {
        List<CommunityPost> samples = CommunityFeedDataSource.getSamplePosts();
        byte[] photo = {1, 2, 3};
        CommunityPost first = CommunityFeedDataSource.submitPost("@alexsantos", "AS",
                CommunityPostType.CAMPUS_TIPS, "", "Bring an umbrella.", null,
                Collections.singletonList(photo));
        CommunityPost second = CommunityFeedDataSource.submitPost("@alexsantos", "AS",
                CommunityPostType.GENERAL_DISCUSSION, "", "How was your day?", null,
                Collections.emptyList());

        assertNotEquals(first.getId(), second.getId());
        assertNotNull(first.getAuthorId());
        assertEquals(first.getAuthorId(), second.getAuthorId());
        assertSame(first, CommunityFeedDataSource.findPost(first.getId()));
        assertTrue(CommunityFeedDataSource.getSessionPosts().contains(second));
        assertSame(photo, first.getPhotos().get(0));
        assertEquals(CommunityPost.MapReviewStatus.NOT_APPLICABLE, second.getMapReviewStatus());
        for (CommunityPost sample : samples) {
            assertNotEquals(sample.getAuthorId(), first.getAuthorId());
        }
        assertEquals(samples, CommunityFeedDataSource.getSamplePosts());
    }

    @Test
    public void commentsUpdateOnlyTheirPostAndAreReturnedNewestFirst() {
        CommunityPost first = submitDiscussion();
        CommunityPost second = submitDiscussion();

        assertTrue(CommunityFeedDataSource.addComment(first.getId(), "Alex Santos",
                "alexsantos", "AS", null, "  First comment.  "));
        assertTrue(CommunityFeedDataSource.addComment(first.getId(), "Alex Santos",
                "alexsantos", "AS", null, "Second comment."));

        List<CommunityComment> comments = CommunityFeedDataSource.getComments(first.getId());
        assertEquals(2, comments.size());
        assertEquals("Second comment.", comments.get(0).text);
        assertEquals("First comment.", comments.get(1).text);
        assertEquals("alexsantos", comments.get(0).username);
        assertEquals(2, CommunityFeedDataSource.findPost(first.getId()).getCommentCount());
        assertEquals(0, second.getCommentCount());
        assertTrue(CommunityFeedDataSource.getComments(second.getId()).isEmpty());
        comments.clear();
        assertEquals(2, CommunityFeedDataSource.getComments(first.getId()).size());
    }

    @Test
    public void blankCommentsAndUnknownPostsDoNotChangeCounts() {
        CommunityPost post = submitDiscussion();
        assertFalse(CommunityFeedDataSource.addComment(post.getId(), "Alex Santos",
                "alexsantos", "AS", null, "\u00a0 \t\n"));
        assertFalse(CommunityFeedDataSource.addComment("missing-post", "Alex Santos",
                "alexsantos", "AS", null, "A valid comment."));
        assertEquals(0, post.getCommentCount());
        assertTrue(CommunityFeedDataSource.getComments(post.getId()).isEmpty());
        assertTrue(CommunityFeedDataSource.getComments("missing-post").isEmpty());
    }

    @Test
    public void sampleCountsMatchTheirExistingCommentRows() {
        List<Integer> expectedCounts = Arrays.asList(18, 11, 7, 5, 3, 2);
        List<CommunityPost> posts = CommunityFeedDataSource.getSamplePosts();
        assertEquals(expectedCounts.size(), posts.size());
        for (int i = 0; i < posts.size(); i++) {
            CommunityPost post = posts.get(i);
            assertEquals(expectedCounts.get(i).intValue(), post.getCommentCount());
            assertEquals(post.getCommentCount(),
                    CommunityFeedDataSource.getComments(post.getId()).size());
        }
    }

    @Test
    public void sharedVoteCountMatchesFeedSortingAndCanBeReset() {
        CommunityPost upvoted = submitDiscussion();
        CommunityPost other = submitDiscussion();
        CommunityFeedDataSource.toggleUpvote(upvoted);
        try {
            List<CommunityPost> relevant = CommunityFeedDataSource.getVisiblePosts(
                    CommunityFeedDataSource.Category.RECOMMENDED);
            assertTrue(relevant.indexOf(upvoted) < relevant.indexOf(other));
            assertTrue(CommunityFeedDataSource.isUpvoted(upvoted));
            assertEquals(1, CommunityFeedDataSource.getDisplayedVoteCount(
                    CommunityFeedDataSource.findPost(upvoted.getId())));
            assertEquals(0, CommunityFeedDataSource.getDisplayedVoteCount(other));
        } finally {
            CommunityFeedDataSource.toggleUpvote(upvoted);
        }
        assertEquals(0, CommunityFeedDataSource.getDisplayedVoteCount(upvoted));
    }

    @Test
    public void recentCategoryShowsAllPostsNewestFirst() {
        List<CommunityPost> posts = Arrays.asList(
                post("old", 90, 4, 100L, false),
                post("new", 1, 0, 300L, false),
                post("middle", 30, 2, 200L, true));

        List<CommunityPost> result = CommunityFeedDataSource.getPostsForCategory(
                posts, CommunityFeedDataSource.Category.RECENT, new HashSet<>());

        assertEquals(3, result.size());
        assertEquals("new", result.get(0).getId());
        assertEquals("middle", result.get(1).getId());
        assertEquals("old", result.get(2).getId());
    }

    @Test
    public void allPostsIncludesCandidatesAndUsesNewestFirst() {
        List<CommunityPost> posts = Arrays.asList(
                post("older", 50, 0, 100L, false),
                post("candidate", 1, 0, 200L, true));
        List<CommunityPost> result = CommunityFeedDataSource.getPostsForCategory(
                posts, CommunityFeedDataSource.Category.ALL_POSTS, new HashSet<>());
        assertEquals(2, result.size());
        assertEquals("candidate", result.get(0).getId());
    }

    @Test
    public void recommendedCategorySortsByVoteCountThenRecency() {
        List<CommunityPost> posts = Arrays.asList(
                post("older-popular", 20, 1, 100L, false),
                post("newer-popular", 20, 1, 200L, false),
                post("less-popular", 19, 0, 300L, false));

        List<CommunityPost> result = CommunityFeedDataSource.getPostsForCategory(
                posts, CommunityFeedDataSource.Category.RECOMMENDED, new HashSet<>());

        assertEquals("newer-popular", result.get(0).getId());
        assertEquals("older-popular", result.get(1).getId());
        assertEquals("less-popular", result.get(2).getId());
    }

    @Test
    public void activeUpvoteChangesRecommendedOrderAndCanBeRemoved() {
        List<CommunityPost> posts = Arrays.asList(
                post("first", 10, 0, 100L, false),
                post("second", 10, 0, 200L, false));
        Set<String> activeUpvotes = new HashSet<>();

        assertTrue(CommunityFeedDataSource.toggleUpvote(activeUpvotes, "first"));
        List<CommunityPost> votedOrder = CommunityFeedDataSource.getPostsForCategory(
                posts, CommunityFeedDataSource.Category.RECOMMENDED, activeUpvotes);
        assertEquals("first", votedOrder.get(0).getId());
        assertEquals(11, CommunityFeedDataSource.getDisplayedVoteCount(
                votedOrder.get(0), activeUpvotes));

        assertFalse(CommunityFeedDataSource.toggleUpvote(activeUpvotes, "first"));
        assertTrue(activeUpvotes.isEmpty());
    }

    @Test
    public void candidatesCategoryOnlyShowsPostsMarkedForReview() {
        List<CommunityPost> posts = Arrays.asList(
                post("candidate", 10, 0, 100L, true),
                post("recommendation", 50, 3, 200L, false));

        List<CommunityPost> result = CommunityFeedDataSource.getPostsForCategory(
                posts, CommunityFeedDataSource.Category.POI_CANDIDATES, new HashSet<>());

        assertEquals(1, result.size());
        assertEquals("candidate", result.get(0).getId());
    }

    @Test
    public void categoryWithNoMatchingPostsReturnsEmptyList() {
        List<CommunityPost> posts = Collections.singletonList(
                post("recommendation", 50, 3, 200L, false));

        List<CommunityPost> result = CommunityFeedDataSource.getPostsForCategory(
                posts, CommunityFeedDataSource.Category.POI_CANDIDATES, new HashSet<>());

        assertTrue(result.isEmpty());
    }

    private CommunityPost submitDiscussion() {
        return CommunityFeedDataSource.submitPost("@alexsantos", "AS",
                CommunityPostType.GENERAL_DISCUSSION, "", "Test discussion.",
                null, Collections.emptyList());
    }

    private CommunityPost post(String id, int votes, int comments, long createdAt,
                               boolean candidate) {
        return new CommunityPost(id, "Student", "ST", null,
                "dlsu_test_location", "Test campus location", "Test title",
                "Test description", 0, votes, comments, createdAt, candidate);
    }
}
