package com.itismob.s02.group5.animomap.ui.moderation;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.profile.MyPost;

import java.util.List;

/**
 * Lists New Place Recommendations for moderators.
 * Pending ones show Approve and Reject. Decided ones show their status badge instead.
 */
public class RecommendationReviewAdapter
        extends RecyclerView.Adapter<RecommendationReviewAdapter.RecommendationViewHolder> {

    // ModeratorActivity handles the taps, so it can confirm, save the decision, and update the counts
    public interface Listener {
        void onApprove(PlaceRecommendation recommendation);

        void onReject(PlaceRecommendation recommendation);
    }

    private final List<PlaceRecommendation> recommendations;
    private final Listener listener;

    public RecommendationReviewAdapter(List<PlaceRecommendation> recommendations, Listener listener) {
        this.recommendations = recommendations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation_review, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        PlaceRecommendation recommendation = recommendations.get(position);
        Context context = holder.itemView.getContext();

        holder.tvRecTitle.setText(recommendation.getTitle());
        holder.tvRecMeta.setText(context.getString(R.string.moderation_submitted_by,
                recommendation.getAuthorName(), recommendation.getSubmittedAgo()));
        holder.tvRecDescription.setText(recommendation.getDescription());

        // Hide the location if the student didn't add one
        if (TextUtils.isEmpty(recommendation.getLocation())) {
            holder.tvRecLocation.setVisibility(View.GONE);
        } else {
            holder.tvRecLocation.setText(recommendation.getLocation());
            holder.tvRecLocation.setVisibility(View.VISIBLE);
        }

        // Badge and buttons depend on the status
        if (recommendation.isPending()) {
            showBadge(holder.tvRecStatus, R.string.status_pending,
                    R.color.status_pending_text, R.color.status_pending_bg);
            holder.layoutRecActions.setVisibility(View.VISIBLE);
        } else if (MyPost.STATUS_APPROVED.equals(recommendation.getStatus())) {
            showBadge(holder.tvRecStatus, R.string.status_approved,
                    R.color.status_approved_text, R.color.status_approved_bg);
            holder.layoutRecActions.setVisibility(View.GONE);
        } else {
            showBadge(holder.tvRecStatus, R.string.status_rejected,
                    R.color.status_rejected_text, R.color.status_rejected_bg);
            holder.layoutRecActions.setVisibility(View.GONE);
        }

        // Buttons
        holder.btnApproveRec.setOnClickListener(v -> listener.onApprove(recommendation));
        holder.btnRejectRec.setOnClickListener(v -> listener.onReject(recommendation));
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    // ---------- Helpers ----------

    // Sets the badge's text and colors (same look as the badges on Profile)
    private void showBadge(TextView badge, int labelRes, int textColorRes, int backgroundColorRes) {
        Context context = badge.getContext();
        badge.setText(labelRes);
        badge.setTextColor(ContextCompat.getColor(context, textColorRes));
        badge.setBackgroundTintList(ContextCompat.getColorStateList(context, backgroundColorRes));
    }

    static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        final TextView tvRecStatus, tvRecTitle, tvRecMeta, tvRecLocation, tvRecDescription;
        final View layoutRecActions;
        final Button btnRejectRec, btnApproveRec;

        RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecStatus = itemView.findViewById(R.id.tvRecStatus);
            tvRecTitle = itemView.findViewById(R.id.tvRecTitle);
            tvRecMeta = itemView.findViewById(R.id.tvRecMeta);
            tvRecLocation = itemView.findViewById(R.id.tvRecLocation);
            tvRecDescription = itemView.findViewById(R.id.tvRecDescription);
            layoutRecActions = itemView.findViewById(R.id.layoutRecActions);
            btnRejectRec = itemView.findViewById(R.id.btnRejectRec);
            btnApproveRec = itemView.findViewById(R.id.btnApproveRec);
        }
    }
}