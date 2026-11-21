package com.itismob.s02.group5.animomap.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.profile.UserProfile;

import java.util.Collections;
import java.util.List;

public class CommunityPostDetailsActivity extends AppCompatActivity {
    static final String EXTRA_POST_ID = "community.details.post_id";
    static final String EXTRA_OPEN_COMMENTS = "community.details.open_comments";
    private static final String STATE_NEWEST_FIRST = "newest_first";
    private static final String STATE_COMMENT_ADDED = "comment_added";

    private CommunityPost post;
    private UserProfile profile;
    private View root;
    private EditText commentInput;
    private LinearLayoutManager layout;
    private CommunityPostDetailsAdapter adapter;
    private boolean newestFirst = true;
    private boolean commentAdded;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        post = CommunityFeedDataSource.findPost(getIntent().getStringExtra(EXTRA_POST_ID));
        if (post == null) {
            Toast.makeText(this, R.string.community_details_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CommunityWindow.enableEdgeToEdge(this);
        setContentView(R.layout.activity_community_post_details);
        root = findViewById(R.id.post_details_root);
        CommunityWindow.applyInsets(root, true);
        commentInput = findViewById(R.id.post_details_comment_input);
        profile = UserProfile.load(this);

        if (state != null) {
            newestFirst = state.getBoolean(STATE_NEWEST_FIRST, true);
            commentAdded = state.getBoolean(STATE_COMMENT_ADDED);
            if (commentAdded) setCommentResult();
        }

        RecyclerView list = findViewById(R.id.post_details_list);
        layout = new LinearLayoutManager(this);
        adapter = new CommunityPostDetailsAdapter(post, profile,
                this::showSortMenu, this::showPostMenu);
        list.setLayoutManager(layout);
        list.setAdapter(adapter);
        refreshComments();

        findViewById(R.id.post_details_back).setOnClickListener(view -> finish());
        findViewById(R.id.post_details_send).setOnClickListener(view -> submitComment());
        // RecyclerView restores the user's own position after recreation.
        if (state == null && getIntent().getBooleanExtra(EXTRA_OPEN_COMMENTS, false)) {
            scrollToComments();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(STATE_NEWEST_FIRST, newestFirst);
        state.putBoolean(STATE_COMMENT_ADDED, commentAdded);
    }

    private void refreshComments() {
        List<CommunityComment> comments = CommunityFeedDataSource.getComments(post.getId());
        if (!newestFirst && comments.size() > 1) Collections.reverse(comments);
        adapter.showComments(comments, newestFirst);
    }

    private void showSortMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(R.string.community_filter_recent)
                .setCheckable(true).setChecked(newestFirst)
                .setOnMenuItemClickListener(item -> {
                    selectSort(true);
                    return true;
                });
        menu.getMenu().add(R.string.community_comments_oldest)
                .setCheckable(true).setChecked(!newestFirst)
                .setOnMenuItemClickListener(item -> {
                    selectSort(false);
                    return true;
                });
        menu.getMenu().setGroupCheckable(Menu.NONE, true, true);
        menu.show();
    }

    private void selectSort(boolean sortNewestFirst) {
        if (newestFirst == sortNewestFirst) return;
        newestFirst = sortNewestFirst;
        refreshComments();
        scrollToComments();
    }

    private void submitComment() {
        boolean added = CommunityFeedDataSource.addComment(post.getId(),
                profile.getFullName(), profile.getUsername(), profile.getInitials(),
                profile.getPhotoUri(), commentInput.getText().toString());
        if (!added) {
            commentInput.requestFocus();
            commentInput.setError(getString(R.string.community_comment_required));
            return;
        }

        commentAdded = true;
        setCommentResult();
        commentInput.setText("");
        commentInput.setError(null);
        getSystemService(InputMethodManager.class)
                .hideSoftInputFromWindow(commentInput.getWindowToken(), 0);
        root.requestFocus();
        refreshComments();
        if (newestFirst) {
            scrollToComments();
        } else {
            layout.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
        }
    }

    private void setCommentResult() {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_POST_ID, post.getId()));
    }

    private void scrollToComments() {
        layout.scrollToPositionWithOffset(CommunityPostDetailsAdapter.COMMENTS_POSITION, 0);
    }

    private void showPostMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(R.string.community_report_post).setOnMenuItemClickListener(item -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.community_report_post)
                    .setMessage(R.string.community_report_confirmation)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.community_report_confirm, (dialog, which) ->
                            Snackbar.make(root, R.string.community_report_acknowledgement,
                                            Snackbar.LENGTH_SHORT)
                                    .setAnchorView(R.id.post_details_composer)
                                    .show())
                    .show();
            return true;
        });
        menu.show();
    }
}
