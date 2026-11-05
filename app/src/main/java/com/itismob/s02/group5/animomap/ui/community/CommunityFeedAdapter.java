package com.itismob.s02.group5.animomap.ui.community;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CommunityFeedAdapter extends RecyclerView.Adapter<CommunityFeedAdapter.PostViewHolder> {

    public interface Listener {
        void onPostSelected(CommunityPost post);

        void onCommentsSelected(CommunityPost post);

        void onUpvoteToggled(CommunityPost post, boolean isUpvoted);
    }

    private final Listener listener;
    private final List<CommunityPost> posts = new ArrayList<>();
    private Set<String> activeUpvotes;

    public CommunityFeedAdapter(Listener listener) {
        this.listener = listener;
    }

    public void updatePosts(List<CommunityPost> newPosts, Set<String> upvotes) {
        posts.clear();
        posts.addAll(newPosts);
        activeUpvotes = upvotes;
        notifyDataSetChanged();
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
        holder.authorName.setText(post.getAuthorName());
        holder.postMeta.setText(getPostMeta(context, post));
        holder.locationLabel.setText(post.getLocationName());
        holder.locationLabel.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_community_location, 0, 0, 0);
        holder.postTitle.setText(post.getTitle());
        holder.postDescription.setText(post.getDescription());

        if (post.isPoiCandidate()) {
            holder.candidateLabel.setVisibility(View.VISIBLE);
            holder.postCard.setStrokeColor(context.getColor(R.color.accent_teal));
            holder.postCard.setStrokeWidth(context.getResources().getDimensionPixelSize(
                    R.dimen.community_candidate_stroke_width));
        } else {
            holder.candidateLabel.setVisibility(View.GONE);
            holder.postCard.setStrokeColor(context.getColor(R.color.surface_elevated));
            holder.postCard.setStrokeWidth(context.getResources().getDimensionPixelSize(
                    R.dimen.community_card_stroke_width));
        }

        if (post.getImageResourceId() == 0) {
            holder.postImage.setVisibility(View.GONE);
            holder.postImage.setImageDrawable(null);
        } else {
            holder.postImage.setVisibility(View.VISIBLE);
            holder.postImage.setImageResource(post.getImageResourceId());
        }

        boolean isUpvoted = activeUpvotes != null && activeUpvotes.contains(post.getId());
        int voteCount = post.getUpvoteCount() + (isUpvoted ? 1 : 0);
        holder.upvoteButton.setText("↑  " + context.getString(
                R.string.community_vote_count, voteCount));
        holder.upvoteButton.setSelected(isUpvoted);
        holder.upvoteButton.setContentDescription(context.getString(
                R.string.community_vote_accessibility, voteCount));
        holder.commentsButton.setText(context.getString(
                R.string.community_comments_count, post.getCommentCount()));
        holder.commentsButton.setContentDescription(context.getString(
                R.string.community_comments_accessibility, post.getCommentCount()));

        holder.itemView.setOnClickListener(view -> listener.onPostSelected(post));
        holder.commentsButton.setOnClickListener(view -> listener.onCommentsSelected(post));
        holder.upvoteButton.setOnClickListener(view ->
                listener.onUpvoteToggled(post, !isUpvoted));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private String getPostMeta(Context context, CommunityPost post) {
        long elapsedMinutes = Math.max(0L,
                (System.currentTimeMillis() - post.getCreatedAtMillis()) / (60L * 1000L));
        String time;
        if (elapsedMinutes < 60) {
            time = context.getString(R.string.community_time_minutes, Math.max(1L, elapsedMinutes));
        } else if (elapsedMinutes < 24 * 60) {
            time = context.getString(R.string.community_time_hours, elapsedMinutes / 60);
        } else {
            time = context.getString(R.string.community_time_days, elapsedMinutes / (24 * 60));
        }

        if (post.getStudentAffiliation() == null || post.getStudentAffiliation().trim().isEmpty()) {
            return time;
        }
        return String.format(Locale.getDefault(), "%s · %s", time, post.getStudentAffiliation());
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView postCard;
        final TextView candidateLabel;
        final TextView authorAvatar;
        final TextView authorName;
        final TextView postMeta;
        final TextView locationLabel;
        final TextView postTitle;
        final TextView postDescription;
        final ImageView postImage;
        final TextView upvoteButton;
        final TextView commentsButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postCard = itemView.findViewById(R.id.community_post_card);
            candidateLabel = itemView.findViewById(R.id.community_candidate_label);
            authorAvatar = itemView.findViewById(R.id.community_author_avatar);
            authorName = itemView.findViewById(R.id.community_author_name);
            postMeta = itemView.findViewById(R.id.community_post_meta);
            locationLabel = itemView.findViewById(R.id.community_location_label);
            postTitle = itemView.findViewById(R.id.community_post_title);
            postDescription = itemView.findViewById(R.id.community_post_description);
            postImage = itemView.findViewById(R.id.community_post_image);
            upvoteButton = itemView.findViewById(R.id.community_upvote_button);
            commentsButton = itemView.findViewById(R.id.community_comments_button);
        }
    }
}
