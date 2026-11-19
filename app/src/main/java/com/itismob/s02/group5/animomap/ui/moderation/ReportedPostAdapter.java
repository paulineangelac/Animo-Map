package com.itismob.s02.group5.animomap.ui.moderation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;

import java.util.List;

/**
 * Lists reported posts for moderators.
 * Open reports show Keep post and Remove post. Handled ones show their status badge instead.
 */
public class ReportedPostAdapter extends RecyclerView.Adapter<ReportedPostAdapter.ReportViewHolder> {

    // ModeratorActivity handles the taps, so it can confirm, save the decision, and update the counts
    public interface Listener {
        void onKeep(ReportedPost report);

        void onRemove(ReportedPost report);
    }

    private final List<ReportedPost> reports;
    private final Listener listener;

    public ReportedPostAdapter(List<ReportedPost> reports, Listener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reported_post, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportedPost report = reports.get(position);
        Context context = holder.itemView.getContext();

        holder.tvReportTitle.setText(report.getPostTitle());
        holder.tvReportMeta.setText(context.getString(R.string.moderation_posted_by,
                report.getAuthorName(), report.getReportCount()));
        holder.tvReportReason.setText(context.getString(R.string.moderation_reason, report.getReason()));

        // Badge and buttons depend on the status
        switch (report.getStatus()) {
            case ReportedPost.STATUS_KEPT:
                showBadge(holder.tvReportStatus, R.string.report_status_kept,
                        R.color.status_approved_text, R.color.status_approved_bg);
                holder.layoutReportActions.setVisibility(View.GONE);
                break;
            case ReportedPost.STATUS_REMOVED:
                showBadge(holder.tvReportStatus, R.string.report_status_removed,
                        R.color.status_rejected_text, R.color.status_rejected_bg);
                holder.layoutReportActions.setVisibility(View.GONE);
                break;
            default:
                showBadge(holder.tvReportStatus, R.string.report_status_open,
                        R.color.status_pending_text, R.color.status_pending_bg);
                holder.layoutReportActions.setVisibility(View.VISIBLE);
                break;
        }

        // Buttons
        holder.btnKeepPost.setOnClickListener(v -> listener.onKeep(report));
        holder.btnRemovePost.setOnClickListener(v -> listener.onRemove(report));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    // ---------- Helpers ----------

    // Sets the badge's text and colors (same look as the badges on Profile)
    private void showBadge(TextView badge, int labelRes, int textColorRes, int backgroundColorRes) {
        Context context = badge.getContext();
        badge.setText(labelRes);
        badge.setTextColor(ContextCompat.getColor(context, textColorRes));
        badge.setBackgroundTintList(ContextCompat.getColorStateList(context, backgroundColorRes));
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        final TextView tvReportStatus, tvReportTitle, tvReportMeta, tvReportReason;
        final View layoutReportActions;
        final Button btnKeepPost, btnRemovePost;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportStatus = itemView.findViewById(R.id.tvReportStatus);
            tvReportTitle = itemView.findViewById(R.id.tvReportTitle);
            tvReportMeta = itemView.findViewById(R.id.tvReportMeta);
            tvReportReason = itemView.findViewById(R.id.tvReportReason);
            layoutReportActions = itemView.findViewById(R.id.layoutReportActions);
            btnKeepPost = itemView.findViewById(R.id.btnKeepPost);
            btnRemovePost = itemView.findViewById(R.id.btnRemovePost);
        }
    }
}