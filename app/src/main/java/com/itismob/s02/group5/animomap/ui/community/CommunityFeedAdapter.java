package com.itismob.s02.group5.animomap.ui.community;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommunityFeedAdapter extends RecyclerView.Adapter<CommunityFeedAdapter.PostViewHolder> {

    public interface Listener {
        void onPostSelected(CommunityPost post);

        void onCommentsSelected(CommunityPost post);

        void onUpvoteToggled(CommunityPost post, boolean isUpvoted);

        void onPhotoSelected(CommunityPost post, int photoIndex);
    }

    private final Listener listener;
    private final List<CommunityPost> posts = new ArrayList<>();
    private final Set<String> savedPosts = new HashSet<>();
    private Set<String> activeUpvotes;

    public CommunityFeedAdapter(Listener listener) {
        this.listener = listener;
    }

    public void updatePosts(List<CommunityPost> newPosts, Set<String> upvotes) {
        int oldCount = posts.size();
        posts.clear();
        posts.addAll(newPosts);
        activeUpvotes = upvotes;
        int newCount = posts.size();
        int sharedCount = Math.min(oldCount, newCount);
        if (sharedCount > 0) notifyItemRangeChanged(0, sharedCount);
        if (newCount > oldCount) {
            notifyItemRangeInserted(oldCount, newCount - oldCount);
        } else if (oldCount > newCount) {
            notifyItemRangeRemoved(newCount, oldCount - newCount);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = posts.get(position);
        Context context = holder.itemView.getContext();

        holder.authorAvatar.setText(post.getAuthorInitials());
        boolean hasAvatar = post.getAvatarResourceId() != 0;
        holder.authorPhoto.setVisibility(hasAvatar ? View.VISIBLE : View.GONE);
        holder.authorAvatar.setVisibility(hasAvatar ? View.GONE : View.VISIBLE);
        holder.authorPhoto.setImageResource(post.getAvatarResourceId());
        holder.authorName.setText(post.getAuthorName());
        holder.postMeta.setText(getPostMeta(context, post));
        boolean hasLocation = post.getLocationName() != null && !post.getLocationName().trim().isEmpty();
        holder.locationLabel.setVisibility(hasLocation ? View.VISIBLE : View.GONE);
        holder.locationLabel.setText(post.getLocation() == null ? post.getLocationName()
                : post.getLocation().label(context));
        holder.locationLabel.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_community_location, 0, 0, 0);
        holder.postTitle.setText(post.getTitle());
        holder.postTitle.setVisibility(post.getTitle() == null ? View.GONE : View.VISIBLE);
        holder.postDescription.setText(post.getDescription());

        if (post.getMapReviewStatus() == CommunityPost.MapReviewStatus.PENDING) {
            holder.candidateLabel.setVisibility(View.VISIBLE);
            holder.postCard.setStrokeColor(context.getColor(R.color.community_card_border));
            holder.postCard.setStrokeWidth(context.getResources().getDimensionPixelSize(
                    R.dimen.community_candidate_stroke_width));
        } else {
            holder.candidateLabel.setVisibility(View.GONE);
            holder.postCard.setStrokeColor(context.getColor(R.color.community_card_border));
            holder.postCard.setStrokeWidth(context.getResources().getDimensionPixelSize(
                    R.dimen.community_card_stroke_width));
        }

        holder.bindPhotos(post, index -> listener.onPhotoSelected(post, index));

        boolean isUpvoted = activeUpvotes != null && activeUpvotes.contains(post.getId());
        int voteCount = post.getUpvoteCount() + (isUpvoted ? 1 : 0);
        holder.upvoteButton.setText(context.getString(R.string.community_vote_count, voteCount));
        holder.upvoteButton.setSelected(isUpvoted);
        TextViewCompat.setCompoundDrawableTintList(holder.upvoteButton,
                android.content.res.ColorStateList.valueOf(context.getColor(
                        isUpvoted ? R.color.accent_teal : R.color.text_secondary)));
        holder.upvoteButton.setContentDescription(context.getString(
                R.string.community_vote_accessibility, voteCount));
        holder.commentsButton.setText(context.getString(
                R.string.community_comments_count, post.getCommentCount()));
        holder.commentsButton.setContentDescription(context.getString(
                R.string.community_comments_accessibility, post.getCommentCount()));
        boolean isSaved = savedPosts.contains(post.getId());
        holder.saveButton.setSelected(isSaved);
        holder.saveButton.setColorFilter(context.getColor(
                isSaved ? R.color.community_saved_icon : R.color.text_secondary));
        holder.saveButton.setContentDescription(context.getString(
                isSaved ? R.string.community_remove_saved_post : R.string.community_save_post));

        holder.itemView.setOnClickListener(view -> listener.onPostSelected(post));
        holder.commentsButton.setOnClickListener(view -> listener.onCommentsSelected(post));
        holder.upvoteButton.setOnClickListener(view ->
                listener.onUpvoteToggled(post, !isUpvoted));
        holder.saveButton.setOnClickListener(view -> {
            if (!savedPosts.add(post.getId())) {
                savedPosts.remove(post.getId());
            }
            boolean saved = savedPosts.contains(post.getId());
            holder.saveButton.setSelected(saved);
            holder.saveButton.setColorFilter(context.getColor(
                    saved ? R.color.community_saved_icon : R.color.text_secondary));
            holder.saveButton.setContentDescription(context.getString(
                    saved ? R.string.community_remove_saved_post : R.string.community_save_post));
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private String getPostMeta(Context context, CommunityPost post) {
        long elapsedMinutes = Math.max(0L,
                (System.currentTimeMillis() - post.getCreatedAtMillis()) / (60L * 1000L));
        String time;
        if (elapsedMinutes == 0) {
            time = context.getString(R.string.community_time_now);
        } else if (elapsedMinutes < 60) {
            time = context.getString(R.string.community_time_minutes, elapsedMinutes);
        } else if (elapsedMinutes < 24 * 60) {
            time = context.getString(R.string.community_time_hours, elapsedMinutes / 60);
        } else {
            time = context.getString(R.string.community_time_days, elapsedMinutes / (24 * 60));
        }

        CommunityPostType type = post.getPostType();
        if (post.getStudentAffiliation() != null && !post.getStudentAffiliation().trim().isEmpty()) {
            time = context.getString(R.string.community_meta_pair, time, post.getStudentAffiliation());
        }
        return type == null || type == CommunityPostType.POI_RECOMMENDATION ? time
                : context.getString(R.string.community_meta_pair, time, context.getString(type.labelRes));
    }

    public static final class PostViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView postCard;
        final TextView candidateLabel;
        final TextView authorAvatar;
        final ImageView authorPhoto;
        final TextView authorName;
        final TextView postMeta;
        final TextView locationLabel;
        final TextView postTitle;
        final TextView postDescription;
        final View photoArea;
        final RecyclerView photos;
        final TextView photoCount;
        final LinearLayoutManager photoLayout;
        private String photoPostId;
        final TextView upvoteButton;
        final TextView commentsButton;
        final ImageView saveButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postCard = itemView.findViewById(R.id.community_post_card);
            candidateLabel = itemView.findViewById(R.id.community_candidate_label);
            authorAvatar = itemView.findViewById(R.id.community_author_avatar);
            authorPhoto = itemView.findViewById(R.id.community_author_photo);
            authorName = itemView.findViewById(R.id.community_author_name);
            postMeta = itemView.findViewById(R.id.community_post_meta);
            locationLabel = itemView.findViewById(R.id.community_location_label);
            postTitle = itemView.findViewById(R.id.community_post_title);
            postDescription = itemView.findViewById(R.id.community_post_description);
            photoArea = itemView.findViewById(R.id.community_post_photo_area);
            photoArea.setClipToOutline(true);
            photos = itemView.findViewById(R.id.community_post_photos);
            photoCount = itemView.findViewById(R.id.community_post_photo_count);
            photoLayout = new LinearLayoutManager(itemView.getContext(), RecyclerView.HORIZONTAL, false);
            photos.setLayoutManager(photoLayout);
            photos.setNestedScrollingEnabled(false);
            photos.setSaveEnabled(false);
            PagerSnapHelper snap = new PagerSnapHelper();
            snap.attachToRecyclerView(photos);
            photos.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView view, int state) {
                    if (state != RecyclerView.SCROLL_STATE_IDLE) return;
                    View photo = snap.findSnapView(photoLayout);
                    if (photo != null) updateCounter(photoLayout.getPosition(photo));
                }
            });
            upvoteButton = itemView.findViewById(R.id.community_upvote_button);
            commentsButton = itemView.findViewById(R.id.community_comments_button);
            saveButton = itemView.findViewById(R.id.community_save_button);
        }

        void bindPhotos(CommunityPost post, PostPhotoPagerAdapter.PhotoClick onPhotoClick) {
            if (post.getId().equals(photoPostId)) return;
            photoPostId = post.getId();
            int count = PostPhotoPagerAdapter.count(post);
            photoArea.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
            photoCount.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
            photos.stopScroll();
            photos.setAdapter(count == 0 ? null : new PostPhotoPagerAdapter(post, onPhotoClick));
            photoLayout.scrollToPositionWithOffset(0, 0);
            updateCounter(0);
        }

        private void updateCounter(int index) {
            RecyclerView.Adapter<?> adapter = photos.getAdapter();
            if (adapter == null || index == RecyclerView.NO_POSITION) return;
            photoCount.setText(itemView.getContext().getString(
                    R.string.community_photo_index, index + 1, adapter.getItemCount()));
        }
    }
}
