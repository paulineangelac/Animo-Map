package com.itismob.s02.group5.animomap.ui.community;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.profile.OtherProfileActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommunityFeedAdapter extends RecyclerView.Adapter<CommunityFeedAdapter.PostViewHolder> {

    public interface Listener {
        void onPostSelected(CommunityPost post);

        void onCommentsSelected(CommunityPost post);

        void onUpvoteToggled(CommunityPost post);

        void onPhotoSelected(CommunityPost post, int photoIndex);
    }

    private final Listener listener;
    private final List<CommunityPost> posts = new ArrayList<>();
    private final Set<String> savedPosts = new HashSet<>();

    public CommunityFeedAdapter(Listener listener) {
        this.listener = listener;
    }

    public void updatePosts(List<CommunityPost> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    void refreshPost(String postId) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {
                // Rebind the same holder so its photo carousel stays in place.
                notifyItemChanged(i, postId);
                return;
            }
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

        CommunityPostViews.bindAvatar(holder.authorAvatar, holder.authorPhoto,
                post.getAuthorInitials(), post.getAvatarResourceId(), null);
        holder.authorName.setText(post.getAuthorName());
        holder.postMeta.setText(getPostMeta(context, post));
        CommunityPostViews.bindLocation(holder.locationLabel, post);
        CommunityPostViews.bindTitle(holder.postTitle, post);
        holder.postDescription.setText(post.getDescription());

        CommunityPostViews.bindMapReviewLabel(holder.candidateLabel, post);

        holder.photos.bind(post, index -> listener.onPhotoSelected(post, index));

        boolean isUpvoted = CommunityFeedDataSource.isUpvoted(post);
        int voteCount = CommunityFeedDataSource.getDisplayedVoteCount(post);
        holder.upvoteButton.setText(context.getString(R.string.community_vote_count, voteCount));
        holder.upvoteButton.setSelected(isUpvoted);
        TextViewCompat.setCompoundDrawableTintList(holder.upvoteButton,
                android.content.res.ColorStateList.valueOf(context.getColor(
                        isUpvoted ? R.color.accent_teal : R.color.text_secondary)));
        holder.upvoteButton.setContentDescription(context.getString(
                R.string.community_vote_accessibility, voteCount));
        int commentCount = post.getCommentCount();
        String commentLabel = context.getResources().getQuantityString(
                R.plurals.community_comments_count, commentCount, commentCount);
        holder.commentsButton.setText(commentLabel);
        holder.commentsButton.setContentDescription(context.getString(
                R.string.community_comments_accessibility, commentLabel));
        boolean isSaved = savedPosts.contains(post.getId());
        holder.saveButton.setSelected(isSaved);
        holder.saveButton.setContentDescription(context.getString(
                isSaved ? R.string.community_remove_saved_post : R.string.community_save_post));

        holder.itemView.setOnClickListener(view -> listener.onPostSelected(post));
        holder.commentsButton.setOnClickListener(view -> listener.onCommentsSelected(post));
        holder.upvoteButton.setOnClickListener(view -> listener.onUpvoteToggled(post));
        holder.saveButton.setOnClickListener(view -> {
            if (!savedPosts.add(post.getId())) {
                savedPosts.remove(post.getId());
            }
            boolean saved = savedPosts.contains(post.getId());
            holder.saveButton.setSelected(saved);
            holder.saveButton.setContentDescription(context.getString(
                    saved ? R.string.community_remove_saved_post : R.string.community_save_post));
        });

        // Tapping the author's picture or name opens their profile (View Other User Profiles)
        View.OnClickListener openAuthorProfile = view ->
                OtherProfileActivity.open(context, post.getAuthorName());
        holder.authorAvatar.setOnClickListener(openAuthorProfile);
        holder.authorPhoto.setOnClickListener(openAuthorProfile);
        holder.authorName.setOnClickListener(openAuthorProfile);
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
        final TextView candidateLabel;
        final TextView authorAvatar;
        final ImageView authorPhoto;
        final TextView authorName;
        final TextView postMeta;
        final TextView locationLabel;
        final TextView postTitle;
        final TextView postDescription;
        final PostPhotoCarousel photos;
        final TextView upvoteButton;
        final TextView commentsButton;
        final ImageView saveButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            candidateLabel = itemView.findViewById(R.id.community_candidate_label);
            authorAvatar = itemView.findViewById(R.id.community_author_avatar);
            authorPhoto = itemView.findViewById(R.id.community_author_photo);
            authorName = itemView.findViewById(R.id.community_author_name);
            postMeta = itemView.findViewById(R.id.community_post_meta);
            locationLabel = itemView.findViewById(R.id.community_location_label);
            postTitle = itemView.findViewById(R.id.community_post_title);
            postDescription = itemView.findViewById(R.id.community_post_description);
            photos = new PostPhotoCarousel(itemView);
            upvoteButton = itemView.findViewById(R.id.community_upvote_button);
            commentsButton = itemView.findViewById(R.id.community_comments_button);
            saveButton = itemView.findViewById(R.id.community_save_button);
        }
    }
}
