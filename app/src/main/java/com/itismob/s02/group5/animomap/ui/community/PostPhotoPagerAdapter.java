package com.itismob.s02.group5.animomap.ui.community;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;

/** The same photo order is used in the feed and in the full-screen viewer. */
final class PostPhotoPagerAdapter extends RecyclerView.Adapter<PostPhotoPagerAdapter.PhotoHolder> {
    interface PhotoClick {
        void open(int index);
    }

    private final CommunityPost post;
    private final PhotoClick onClick;

    PostPhotoPagerAdapter(CommunityPost post, PhotoClick onClick) {
        this.post = post;
        this.onClick = onClick;
    }

    static int count(CommunityPost post) {
        return post.getImageResourceId() != 0 ? 1 : post.getPhotos().size();
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView image = new ImageView(parent.getContext());
        image.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (onClick == null) {
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setForeground(AppCompatResources.getDrawable(
                    parent.getContext(), R.drawable.bg_community_photo_ripple));
            image.setFocusable(true);
        }
        return new PhotoHolder(image);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
        ImageView image = holder.image;
        if (post.getImageResourceId() != 0) {
            image.setImageResource(post.getImageResourceId());
            image.setContentDescription(image.getContext().getString(R.string.community_photo_placeholder));
        } else {
            PostPhotoLoader.load(image, post.getPhotos().get(position), onClick == null ? 840 : 420,
                    position + 1, getItemCount());
        }
        if (onClick != null) {
            image.setOnClickListener(view -> {
                int current = holder.getBindingAdapterPosition();
                if (current != RecyclerView.NO_POSITION) onClick.open(current);
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull PhotoHolder holder) {
        PostPhotoLoader.clear(holder.image);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return count(post);
    }

    static final class PhotoHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        PhotoHolder(ImageView image) {
            super(image);
            this.image = image;
        }
    }
}
