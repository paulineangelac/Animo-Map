package com.itismob.s02.group5.animomap.ui.map;

import org.junit.Test;
import static org.junit.Assert.*;

public class PlaceReviewsDataSourceTest {
    @Test public void sampleReviewsGiveAverageAndCount() {
        assertEquals(3, PlaceReviewsDataSource.getCount("henry_sy"));
        assertEquals(13.0 / 3, PlaceReviewsDataSource.getAverage("henry_sy"), 1e-9);
    }
    @Test public void placeWithoutReviewsHasNoRating() {
        assertEquals(0, PlaceReviewsDataSource.getCount("no_reviews_place"));
        assertEquals(0.0, PlaceReviewsDataSource.getAverage("no_reviews_place"), 1e-9);
        assertEquals(0, PlaceReviewsDataSource.getMyRating("no_reviews_place"));
    }
    @Test public void ratingAndReviewUpdateTheStudentsOwnEntry() {
        String id = "rate_test_place";
        assertFalse("needs a star rating first", PlaceReviewsDataSource.submitReview(id, "Nice"));
        PlaceReviewsDataSource.rate(id, 4);
        PlaceReviewsDataSource.rate(id, 5);
        assertEquals(1, PlaceReviewsDataSource.getCount(id));
        assertEquals(5, PlaceReviewsDataSource.getMyRating(id));
        assertFalse(PlaceReviewsDataSource.submitReview(id, "   "));
        assertTrue(PlaceReviewsDataSource.submitReview(id, " Great spot "));
        assertEquals(1, PlaceReviewsDataSource.getCount(id));
        assertEquals("Great spot", PlaceReviewsDataSource.getReviews(id).get(0).getComment());
        // Rating again keeps the written text.
        PlaceReviewsDataSource.rate(id, 3);
        assertEquals("Great spot", PlaceReviewsDataSource.getReviews(id).get(0).getComment());
        assertEquals(3, PlaceReviewsDataSource.getMyRating(id));
        PlaceReviewsDataSource.rate(id, 9);
        assertEquals(3, PlaceReviewsDataSource.getMyRating(id));
    }
    @Test public void starsTextMatchesRating() {
        assertEquals("★★★★☆", PlaceReview.stars(4));
    }
}
