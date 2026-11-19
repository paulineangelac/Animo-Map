package com.itismob.s02.group5.animomap.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;

import java.util.List;

/**
 * Shows the student's posts on Profile, with a status badge on New Place Recommendations.
 */
public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.PostViewHolder> {

    private final List<MyPost> posts;

    public MyPostAdapter(List<MyPost> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        MyPost post = posts.get(position);
        Context context = holder.itemView.getContext();

        holder.tvPostTitle.setText(post.getTitle());
        holder.tvPostMeta.setText(context.getString(R.string.my_item_meta,
                post.getCategory(), post.getPostedAgo()));
        showStatus(holder.tvPostStatus, post.getStatus());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /*
     * STATUS BADGE: yellow for Pending, teal for Approved, red for Rejected.
     * Regular posts don't need moderator review, so their badge stays hidden.
     */
    private void showStatus(TextView badge, String status) {
        int labelRes;
        int textColorRes;
        int backgroundColorRes;

        switch (status) {
            case MyPost.STATUS_PENDING:
                labelRes = R.string.status_pending;
                textColorRes = R.color.status_pending_text;
                backgroundColorRes = R.color.status_pending_bg;
                break;
            case MyPost.STATUS_APPROVED:
                labelRes = R.string.status_approved;
                textColorRes = R.color.status_approved_text;
                backgroundColorRes = R.color.status_approved_bg;
                break;
            case MyPost.STATUS_REJECTED:
                labelRes = R.string.status_rejected;
                textColorRes = R.color.status_rejected_text;
                backgroundColorRes = R.color.status_rejected_bg;
                break;
            default:
                badge.setVisibility(View.GONE);
                return;
        }

        Context context = badge.getContext();
        badge.setText(labelRes);
        badge.setTextColor(ContextCompat.getColor(context, textColorRes));
        badge.setBackgroundTintList(ContextCompat.getColorStateList(context, backgroundColorRes));
        badge.setVisibility(View.VISIBLE);
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPostTitle, tvPostMeta, tvPostStatus;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvPostMeta = itemView.findViewById(R.id.tvPostMeta);
            tvPostStatus = itemView.findViewById(R.id.tvPostStatus);
        }
    }
}