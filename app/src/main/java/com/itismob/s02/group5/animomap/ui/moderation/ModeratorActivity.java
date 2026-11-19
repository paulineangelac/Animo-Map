package com.itismob.s02.group5.animomap.ui.moderation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.profile.MyPost;

import java.util.List;

/**
 * Moderator tools: approve or reject New Place Recommendations, and act on reported posts.
 * Only opens for a moderator. Everyone else is sent back.
 */
public class ModeratorActivity extends AppCompatActivity {

    // Views
    private ImageButton btnBack;
    private TextView tvRecommendationsCount, tvReportsCount;
    private RecyclerView rvRecommendations, rvReports;

    // The two lists and their adapters
    private List<PlaceRecommendation> recommendations;
    private List<ReportedPost> reports;
    private RecommendationReviewAdapter recommendationAdapter;
    private ReportedPostAdapter reportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only moderators can use this screen
        if (!UserRole.isModerator(this)) {
            Toast.makeText(this, "Only moderators can open this screen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Light status bar icons, since Animo Map always uses a dark background
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_moderator);

        // Keep the screen clear of the status bar and navigation bar
        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        final int padLeft = root.getPaddingLeft();
        final int padTop = root.getPaddingTop();
        final int padRight = root.getPaddingRight();
        final int padBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(padLeft + bars.left, padTop + bars.top, padRight + bars.right, padBottom + bars.bottom);
            return insets;
        });

        // Instantiate views
        btnBack = findViewById(R.id.btnBack);
        tvRecommendationsCount = findViewById(R.id.tvRecommendationsCount);
        tvReportsCount = findViewById(R.id.tvReportsCount);
        rvRecommendations = findViewById(R.id.rvRecommendations);
        rvReports = findViewById(R.id.rvReports);

        // Buttons
        btnBack.setOnClickListener(v -> finish());

        // New Place Recommendations list
        recommendations = ModerationDataSource.getRecommendations();
        recommendationAdapter = new RecommendationReviewAdapter(recommendations,
                new RecommendationReviewAdapter.Listener() {
                    @Override
                    public void onApprove(PlaceRecommendation recommendation) {
                        approve(recommendation);
                    }

                    @Override
                    public void onReject(PlaceRecommendation recommendation) {
                        confirmReject(recommendation);
                    }
                });
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this));
        rvRecommendations.setAdapter(recommendationAdapter);

        // Reported posts list
        reports = ModerationDataSource.getReports();
        reportAdapter = new ReportedPostAdapter(reports, new ReportedPostAdapter.Listener() {
            @Override
            public void onKeep(ReportedPost report) {
                keepPost(report);
            }

            @Override
            public void onRemove(ReportedPost report) {
                confirmRemove(report);
            }
        });
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(reportAdapter);

        showCounts();
    }

    /*
     * APPROVE: marks the recommendation as Approved.
     */
    private void approve(PlaceRecommendation recommendation) {
        recommendation.setStatus(MyPost.STATUS_APPROVED);
        recommendationAdapter.notifyItemChanged(recommendations.indexOf(recommendation));
        showCounts();
        Toast.makeText(this, "Recommendation approved", Toast.LENGTH_SHORT).show();
    }

    /*
     * REJECT: asks first, then marks the recommendation as Rejected.
     */
    private void confirmReject(PlaceRecommendation recommendation) {
        new AlertDialog.Builder(this)
                .setTitle("Reject recommendation")
                .setMessage("Reject \"" + recommendation.getTitle() + "\"?")
                .setPositiveButton("Reject", (dialog, which) -> {
                    recommendation.setStatus(MyPost.STATUS_REJECTED);
                    recommendationAdapter.notifyItemChanged(recommendations.indexOf(recommendation));
                    showCounts();
                    Toast.makeText(this, "Recommendation rejected", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /*
     * KEEP POST: dismisses the report. The post stays up.
     */
    private void keepPost(ReportedPost report) {
        report.setStatus(ReportedPost.STATUS_KEPT);
        reportAdapter.notifyItemChanged(reports.indexOf(report));
        showCounts();
        Toast.makeText(this, "Report dismissed. The post stays up.", Toast.LENGTH_SHORT).show();
    }

    /*
     * REMOVE POST: asks first, then marks the post as Removed.
     */
    private void confirmRemove(ReportedPost report) {
        new AlertDialog.Builder(this)
                .setTitle("Remove post")
                .setMessage("Remove \"" + report.getPostTitle() + "\" from Community?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    report.setStatus(ReportedPost.STATUS_REMOVED);
                    reportAdapter.notifyItemChanged(reports.indexOf(report));
                    showCounts();
                    Toast.makeText(this, "Post removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------- Helpers ----------

    // Updates "n waiting for review" under each heading
    private void showCounts() {
        tvRecommendationsCount.setText(getString(R.string.moderation_waiting_count,
                ModerationDataSource.countPendingRecommendations()));
        tvReportsCount.setText(getString(R.string.moderation_waiting_count,
                ModerationDataSource.countOpenReports()));
    }
}