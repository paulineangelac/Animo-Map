package com.itismob.s02.group5.animomap.ui.community;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.itismob.s02.group5.animomap.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommunityFragment extends Fragment {

    private List<CommunityPost> allPosts;
    private final Set<String> activeUpvotes = new HashSet<>();
    private CommunityFeedDataSource.Category selectedCategory =
            CommunityFeedDataSource.Category.RECENT;
    private CommunityFeedAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyState;
    private TextView filterControl;
    private View reviewNotice;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.community_recycler);
        emptyState = view.findViewById(R.id.community_empty_state);
        filterControl = view.findViewById(R.id.community_filter);
        reviewNotice = view.findViewById(R.id.community_review_notice);

        allPosts = CommunityFeedDataSource.getSamplePosts();
        adapter = new CommunityFeedAdapter(new CommunityFeedAdapter.Listener() {
            @Override
            public void onPostSelected(CommunityPost post) {
                showMessage(R.string.community_post_details_unavailable);
            }

            @Override
            public void onCommentsSelected(CommunityPost post) {
                showMessage(R.string.community_comments_unavailable);
            }

            @Override
            public void onUpvoteToggled(CommunityPost post, boolean isUpvoted) {
                boolean wasUpvoted = activeUpvotes.contains(post.getId());
                if (wasUpvoted != isUpvoted) {
                    CommunityFeedDataSource.toggleUpvote(activeUpvotes, post.getId());
                }
                showSelectedCategory();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);

        view.findViewById(R.id.community_create_post).setOnClickListener(
                v -> showMessage(R.string.community_create_unavailable));
        filterControl.setOnClickListener(v -> showFilterMenu());

        showSelectedCategory();
    }

    private void selectCategory(CommunityFeedDataSource.Category category) {
        selectedCategory = category;
        showSelectedCategory();
    }

    private void showSelectedCategory() {
        if (adapter == null || allPosts == null) {
            return;
        }

        filterControl.setText(filterLabel(selectedCategory));
        reviewNotice.setVisibility(selectedCategory == CommunityFeedDataSource.Category.POI_CANDIDATES
                ? View.VISIBLE : View.GONE);

        List<CommunityPost> visiblePosts = CommunityFeedDataSource.getPostsForCategory(
                allPosts, selectedCategory, activeUpvotes);
        adapter.updatePosts(visiblePosts, activeUpvotes);

        boolean isEmpty = visiblePosts.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private int filterLabel(CommunityFeedDataSource.Category category) {
        switch (category) {
            case RECOMMENDED: return R.string.community_filter_relevant;
            case ALL_POSTS: return R.string.community_filter_all;
            case POI_CANDIDATES: return R.string.community_filter_candidates;
            default: return R.string.community_filter_recent;
        }
    }

    private void showFilterMenu() {
        LinearLayout menu = new LinearLayout(requireContext());
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(4), dp(4), dp(4), dp(4));
        menu.setBackgroundResource(R.drawable.bg_community_dropdown);
        PopupWindow popup = new PopupWindow(menu, dp(208), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(8));
        CommunityFeedDataSource.Category[] options = {
                CommunityFeedDataSource.Category.RECENT,
                CommunityFeedDataSource.Category.RECOMMENDED,
                CommunityFeedDataSource.Category.ALL_POSTS,
                CommunityFeedDataSource.Category.POI_CANDIDATES
        };
        for (CommunityFeedDataSource.Category option : options) {
            TextView row = new TextView(requireContext());
            boolean selected = option == selectedCategory;
            row.setText(getString(filterLabel(option)) + (selected ? "  ✓" : ""));
            row.setTextColor(requireContext().getColor(selected ? R.color.accent_teal : R.color.text_primary));
            row.setTextSize(14);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), 0, dp(12), 0);
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            row.setContentDescription(getString(filterLabel(option)) + (selected ? ", selected" : ""));
            row.setOnClickListener(v -> { selectCategory(option); popup.dismiss(); });
            menu.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        }
        popup.showAsDropDown(filterControl, filterControl.getWidth() - dp(208), dp(4));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showMessage(int messageResourceId) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, messageResourceId, Snackbar.LENGTH_SHORT).show();
        }
    }
}
