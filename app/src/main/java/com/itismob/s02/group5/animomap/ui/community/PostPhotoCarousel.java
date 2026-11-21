package com.itismob.s02.group5.animomap.ui.community;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;

/** Binds the same swipeable photo strip in the feed and post details. */
final class PostPhotoCarousel {
    private final View area;
    private final RecyclerView photos;
    private final TextView counter;
    private final LinearLayoutManager layout;
    private String postId;

    PostPhotoCarousel(View row) {
        area = row.findViewById(R.id.community_post_photo_area);
        area.setClipToOutline(true);
        photos = row.findViewById(R.id.community_post_photos);
        counter = row.findViewById(R.id.community_post_photo_count);
        layout = new LinearLayoutManager(row.getContext(), RecyclerView.HORIZONTAL, false);
        photos.setLayoutManager(layout);
        photos.setNestedScrollingEnabled(false);
        photos.setSaveEnabled(false);
        PagerSnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(photos);
        photos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView view, int state) {
                if (state != RecyclerView.SCROLL_STATE_IDLE) return;
                View photo = snap.findSnapView(layout);
                if (photo != null) updateCounter(layout.getPosition(photo));
            }
        });
    }

    void bind(CommunityPost post, PostPhotoPagerAdapter.PhotoClick onPhotoClick) {
        // A count-only update should leave the visible photo in place.
        if (post.getId().equals(postId)) return;
        postId = post.getId();
        int count = PostPhotoPagerAdapter.count(post);
        area.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        counter.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
        photos.stopScroll();
        photos.setAdapter(count == 0 ? null : new PostPhotoPagerAdapter(post, onPhotoClick));
        layout.scrollToPositionWithOffset(0, 0);
        updateCounter(0);
    }

    private void updateCounter(int index) {
        RecyclerView.Adapter<?> adapter = photos.getAdapter();
        if (adapter == null || index == RecyclerView.NO_POSITION) return;
        counter.setText(area.getContext().getString(
                R.string.community_photo_index, index + 1, adapter.getItemCount()));
    }
}
