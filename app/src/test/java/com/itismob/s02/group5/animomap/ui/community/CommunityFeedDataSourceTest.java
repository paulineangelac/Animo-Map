package com.itismob.s02.group5.animomap.ui.community;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommunityFeedDataSourceTest {

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
        List<CommunityPost> posts = Arrays.asList(
                post("recommendation", 50, 3, 200L, false));

        List<CommunityPost> result = CommunityFeedDataSource.getPostsForCategory(
                posts, CommunityFeedDataSource.Category.POI_CANDIDATES, new HashSet<>());

        assertTrue(result.isEmpty());
    }

    private CommunityPost post(String id, int votes, int comments, long createdAt,
                               boolean candidate) {
        return new CommunityPost(id, "Student", "ST", null,
                "dlsu_test_location", "Test campus location", "Test title",
                "Test description", 0, votes, comments, createdAt, candidate);
    }
}
