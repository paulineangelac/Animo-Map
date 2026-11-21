package com.itismob.s02.group5.animomap.ui.community;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommunityFragment extends Fragment {
    private static final String STATE_FILTER = "filter";
    private static final String STATE_UPVOTES = "upvotes";

    private final Set<String> activeUpvotes = new HashSet<>();
    private CommunityFeedDataSource.Category selectedCategory =
            CommunityFeedDataSource.Category.RECENT;
    private CommunityFeedAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyState;
    private TextView filterControl;
    private Menu filterOptions;
    private View reviewNotice;

    private final ActivityResultLauncher<Intent> composer =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() != Activity.RESULT_OK || data == null) return;
                String postId = data.getStringExtra(CreateCommunityPostActivity.EXTRA_POST_ID);
                onPostPublished(postId);
            });

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

        if (savedInstanceState != null) {
            String filter = savedInstanceState.getString(STATE_FILTER);
            if (filter != null) selectedCategory = CommunityFeedDataSource.Category.valueOf(filter);
            List<String> upvotes = savedInstanceState.getStringArrayList(STATE_UPVOTES);
            if (upvotes != null) activeUpvotes.addAll(upvotes);
        }
        recyclerView = view.findViewById(R.id.community_recycler);
        emptyState = view.findViewById(R.id.community_empty_state);
        filterControl = view.findViewById(R.id.community_filter);
        reviewNotice = view.findViewById(R.id.community_review_notice);

        // Inflate the definitions here and keep the existing styled popup for display.
        PopupMenu menu = new PopupMenu(requireContext(), filterControl);
        menu.inflate(R.menu.community_feed_filters);
        filterOptions = menu.getMenu();

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
                String postId = post.getId();
                if (activeUpvotes.contains(postId) == isUpvoted) return;
                CommunityFeedDataSource.toggleUpvote(activeUpvotes, postId);
                if (selectedCategory == CommunityFeedDataSource.Category.RECOMMENDED) {
                    showSelectedCategory();
                } else {
                    adapter.refreshUpvote(postId);
                }
            }

            @Override
            public void onPhotoSelected(CommunityPost post, int photoIndex) {
                Intent intent = new Intent(requireContext(), CommunityPhotoViewerActivity.class)
                        .putExtra(CommunityPhotoViewerActivity.EXTRA_POST_ID, post.getId())
                        .putExtra(CommunityPhotoViewerActivity.EXTRA_PHOTO_INDEX, photoIndex);
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);

        view.findViewById(R.id.community_create_post).setOnClickListener(
                v -> composer.launch(new Intent(requireContext(), CreateCommunityPostActivity.class)));
        filterControl.setOnClickListener(v -> showFilterMenu());

        showSelectedCategory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(STATE_FILTER, selectedCategory.name());
        state.putStringArrayList(STATE_UPVOTES, new ArrayList<>(activeUpvotes));
    }

    private void onPostPublished(String postId) {
        CommunityPost post = CommunityFeedDataSource.findPost(postId);
        if (post == null || getView() == null) return;
        if (selectedCategory == CommunityFeedDataSource.Category.POI_CANDIDATES
                && !post.isPoiCandidate()) {
            selectedCategory = CommunityFeedDataSource.Category.RECENT;
        }
        List<CommunityPost> visiblePosts = showSelectedCategory();
        for (int i = 0; i < visiblePosts.size(); i++) {
            if (postId.equals(visiblePosts.get(i).getId())) {
                recyclerView.scrollToPosition(i);
                break;
            }
        }
        showMessage(R.string.community_post_published);
    }

    private List<CommunityPost> showSelectedCategory() {
        MenuItem selectedOption = filterOptions.findItem(selectedCategory.menuItemId);
        selectedOption.setChecked(true);
        filterControl.setText(selectedOption.getTitle());
        reviewNotice.setVisibility(selectedCategory == CommunityFeedDataSource.Category.POI_CANDIDATES
                ? View.VISIBLE : View.GONE);

        List<CommunityPost> visiblePosts = CommunityFeedDataSource.getPostsForCategory(
                CommunityFeedDataSource.getSessionPosts(), selectedCategory, activeUpvotes);
        adapter.updatePosts(visiblePosts, activeUpvotes);

        boolean isEmpty = visiblePosts.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        return visiblePosts;
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
        for (int i = 0; i < filterOptions.size(); i++) {
            MenuItem option = filterOptions.getItem(i);
            TextView row = (TextView) getLayoutInflater().inflate(
                    R.layout.item_community_filter_option, menu, false);
            boolean selected = option.isChecked();
            CharSequence title = option.getTitle();
            row.setText(selected
                    ? getString(R.string.community_filter_option_selected, title) : title);
            row.setTextColor(requireContext().getColor(selected ? R.color.accent_teal : R.color.text_primary));
            row.setContentDescription(selected
                    ? getString(R.string.community_filter_option_accessibility_selected, title) : title);
            row.setOnClickListener(v -> {
                selectFilter(option.getItemId());
                popup.dismiss();
            });
            menu.addView(row);
        }
        popup.showAsDropDown(filterControl, 0, dp(4));
    }

    private void selectFilter(int itemId) {
        for (CommunityFeedDataSource.Category category : CommunityFeedDataSource.Category.values()) {
            if (category.menuItemId == itemId) {
                selectedCategory = category;
                showSelectedCategory();
                return;
            }
        }
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
