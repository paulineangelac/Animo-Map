package com.itismob.s02.group5.animomap.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.itismob.s02.group5.animomap.R;

import java.util.List;

/**
 * Another student's profile: their info and posts only.
 * There's no Edit profile, Log out, or status badge here, so nothing can be changed.
 */
public class OtherProfileActivity extends AppCompatActivity {

    // Key for the student's name, sent by the screen that opens this one
    public static final String EXTRA_AUTHOR_NAME = "extra_author_name";

    // Views
    private ImageButton btnBack;
    private ShapeableImageView ivOtherPhoto;
    private TextView tvOtherInitials, tvOtherName, tvOtherUsername, tvOtherBio, tvOtherDetails;
    private TextView tvOtherPostsHeading, tvOtherNoPosts;
    private RecyclerView rvOtherPosts;

    /*
     * OPEN: call this from any screen that shows a student's name or picture.
     * Example: OtherProfileActivity.open(context, post.getAuthorName());
     */
    public static void open(Context context, String authorName) {
        Intent intent = new Intent(context, OtherProfileActivity.class);
        intent.putExtra(EXTRA_AUTHOR_NAME, authorName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Light status bar icons, since Animo Map always uses a dark background
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_other_profile);

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
        ivOtherPhoto = findViewById(R.id.ivOtherPhoto);
        tvOtherInitials = findViewById(R.id.tvOtherInitials);
        tvOtherName = findViewById(R.id.tvOtherName);
        tvOtherUsername = findViewById(R.id.tvOtherUsername);
        tvOtherBio = findViewById(R.id.tvOtherBio);
        tvOtherDetails = findViewById(R.id.tvOtherDetails);
        tvOtherPostsHeading = findViewById(R.id.tvOtherPostsHeading);
        tvOtherNoPosts = findViewById(R.id.tvOtherNoPosts);
        rvOtherPosts = findViewById(R.id.rvOtherPosts);

        // Buttons
        btnBack.setOnClickListener(v -> finish());

        // Which student to show
        String authorName = getIntent().getStringExtra(EXTRA_AUTHOR_NAME);
        if (TextUtils.isEmpty(authorName)) {
            Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showUser(OtherUsersDataSource.findByName(authorName));
        showPosts(authorName);
    }

    /*
     * SHOW USER: fills the card with the student's photo or initials, name, username, bio, and school.
     */
    private void showUser(OtherUser user) {
        tvOtherName.setText(user.getName());
        tvOtherUsername.setText(getString(R.string.profile_username_format, user.getUsername()));

        // Photo if they have one, otherwise their initials
        if (user.getAvatarResId() != 0) {
            ivOtherPhoto.setImageResource(user.getAvatarResId());
            ivOtherPhoto.setVisibility(View.VISIBLE);
            tvOtherInitials.setVisibility(View.GONE);
        } else {
            tvOtherInitials.setText(user.getInitials());
            tvOtherInitials.setVisibility(View.VISIBLE);
            ivOtherPhoto.setVisibility(View.GONE);
        }

        // Hide the bio and school details if the student left them empty
        showOrHide(tvOtherBio, user.getBio());
        showOrHide(tvOtherDetails, user.getSchoolDetails());
    }

    /*
     * SHOW POSTS: lists the student's posts, or "No posts yet".
     * Reuses MyPostAdapter from My Contributions.
     */
    private void showPosts(String authorName) {
        List<MyPost> posts = OtherUsersDataSource.getPostsBy(authorName);

        tvOtherPostsHeading.setText(getString(R.string.profile_posts_count, posts.size()));
        tvOtherNoPosts.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
        rvOtherPosts.setLayoutManager(new LinearLayoutManager(this));
        rvOtherPosts.setAdapter(new MyPostAdapter(posts));
    }

    // ---------- Helpers ----------

    // Shows the text, or hides the view if there's nothing to show
    private void showOrHide(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(text);
            view.setVisibility(View.VISIBLE);
        }
    }
}