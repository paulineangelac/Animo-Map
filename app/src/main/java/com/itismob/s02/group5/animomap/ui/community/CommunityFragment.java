package com.itismob.s02.group5.animomap.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
            CommunityFeedDataSource.Category.RECOMMENDED;
    private CommunityFeedAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyState;
    private TextView recommendedCategory;
    private TextView recentCategory;
    private TextView candidatesCategory;

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
        recommendedCategory = view.findViewById(R.id.category_recommended);
        recentCategory = view.findViewById(R.id.category_recent);
        candidatesCategory = view.findViewById(R.id.category_candidates);

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

        recommendedCategory.setOnClickListener(v -> selectCategory(
                CommunityFeedDataSource.Category.RECOMMENDED));
        recentCategory.setOnClickListener(v -> selectCategory(
                CommunityFeedDataSource.Category.RECENT));
        candidatesCategory.setOnClickListener(v -> selectCategory(
                CommunityFeedDataSource.Category.POI_CANDIDATES));

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

        updateCategoryAppearance(recommendedCategory,
                selectedCategory == CommunityFeedDataSource.Category.RECOMMENDED);
        updateCategoryAppearance(recentCategory,
                selectedCategory == CommunityFeedDataSource.Category.RECENT);
        updateCategoryAppearance(candidatesCategory,
                selectedCategory == CommunityFeedDataSource.Category.POI_CANDIDATES);

        List<CommunityPost> visiblePosts = CommunityFeedDataSource.getPostsForCategory(
                allPosts, selectedCategory, activeUpvotes);
        adapter.updatePosts(visiblePosts, activeUpvotes);

        boolean isEmpty = visiblePosts.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateCategoryAppearance(TextView category, boolean selected) {
        category.setSelected(selected);
        category.setTextColor(requireContext().getColor(
                selected ? R.color.bg_primary : R.color.text_secondary));
    }

    private void showMessage(int messageResourceId) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, messageResourceId, Snackbar.LENGTH_SHORT).show();
        }
    }
}
