package com.itismob.s02.group5.animomap.ui.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;

public class CommunityPhotoViewerActivity extends AppCompatActivity {
    static final String EXTRA_POST_ID = "community.viewer.post_id";
    static final String EXTRA_PHOTO_INDEX = "community.viewer.photo_index";

    private int currentIndex;

    static void open(Context context, CommunityPost post, int photoIndex) {
        Intent intent = new Intent(context, CommunityPhotoViewerActivity.class)
                .putExtra(EXTRA_POST_ID, post.getId())
                .putExtra(EXTRA_PHOTO_INDEX, photoIndex);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        CommunityPost post = CommunityFeedDataSource.findPost(getIntent().getStringExtra(EXTRA_POST_ID));
        if (post == null || PostPhotoPagerAdapter.count(post) == 0) {
            finish();
            return;
        }

        CommunityWindow.enableEdgeToEdge(this);
        setContentView(R.layout.activity_community_photo_viewer);
        CommunityWindow.applyInsets(findViewById(R.id.community_viewer_root), false);
        findViewById(R.id.community_viewer_close).setOnClickListener(view -> finish());

        RecyclerView pager = findViewById(R.id.community_viewer_photos);
        TextView counter = findViewById(R.id.community_viewer_count);
        LinearLayoutManager layout = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        pager.setLayoutManager(layout);
        PostPhotoPagerAdapter photos = new PostPhotoPagerAdapter(post, null);
        pager.setAdapter(photos);
        int count = photos.getItemCount();
        currentIndex = state == null ? getIntent().getIntExtra(EXTRA_PHOTO_INDEX, 0)
                : state.getInt(EXTRA_PHOTO_INDEX);
        currentIndex = Math.max(0, Math.min(currentIndex, count - 1));
        layout.scrollToPositionWithOffset(currentIndex, 0);
        counter.setText(getString(R.string.community_photo_index, currentIndex + 1, count));
        counter.setVisibility(count > 1 ? View.VISIBLE : View.GONE);

        PagerSnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(pager);
        pager.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView view, int state) {
                if (state != RecyclerView.SCROLL_STATE_IDLE) return;
                View photo = snap.findSnapView(layout);
                if (photo == null) return;
                int index = layout.getPosition(photo);
                if (index == RecyclerView.NO_POSITION) return;
                currentIndex = index;
                counter.setText(getString(R.string.community_photo_index, index + 1, count));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(EXTRA_PHOTO_INDEX, currentIndex);
    }
}
