package com.itismob.s02.group5.animomap.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.google.android.material.imageview.ShapeableImageView;
import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.auth.LoginActivity;
import com.itismob.s02.group5.animomap.ui.moderation.ModerationDataSource;
import com.itismob.s02.group5.animomap.ui.moderation.ModeratorActivity;
import com.itismob.s02.group5.animomap.ui.moderation.UserRole;

/** Profile screen: shows the student's profile, opens Edit Profile, and lets them log out. */
public class ProfileFragment extends Fragment {

    // Views
    private ShapeableImageView ivProfilePhoto;
    private TextView tvProfileInitials, tvProfileName, tvProfileUsername, tvProfileBio, tvProfileDetails;
    private Button btnEditProfile, btnLogout;
    private TextView tvPostsHeading, tvReviewsHeading;
    private RecyclerView rvMyPosts, rvMyReviews;
    private View cardModeratorTools;
    private TextView tvModeratorSummary;
    private Button btnOpenModeratorTools;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Instantiate views
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        tvProfileInitials = view.findViewById(R.id.tvProfileInitials);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileBio = view.findViewById(R.id.tvProfileBio);
        tvProfileDetails = view.findViewById(R.id.tvProfileDetails);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        tvPostsHeading = view.findViewById(R.id.tvPostsHeading);
        tvReviewsHeading = view.findViewById(R.id.tvReviewsHeading);
        rvMyPosts = view.findViewById(R.id.rvMyPosts);
        rvMyReviews = view.findViewById(R.id.rvMyReviews);
        cardModeratorTools = view.findViewById(R.id.cardModeratorTools);
        tvModeratorSummary = view.findViewById(R.id.tvModeratorSummary);
        btnOpenModeratorTools = view.findViewById(R.id.btnOpenModeratorTools);

        // Buttons
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> confirmLogout());
        btnOpenModeratorTools.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ModeratorActivity.class)));

        // Lists of the student's posts and reviews
        showMyContributions();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Runs every time Profile comes back into view, so changes saved in Edit Profile show up
        showProfile();
        showModeratorTools();
    }

    /*
     * MODERATOR TOOLS: only moderators see this card. It shows how many items still need review.
     */
    private void showModeratorTools() {
        if (!UserRole.isModerator(requireContext())) {
            cardModeratorTools.setVisibility(View.GONE);
            return;
        }
        tvModeratorSummary.setText(getString(R.string.moderator_card_summary,
                ModerationDataSource.countPendingRecommendations(),
                ModerationDataSource.countOpenReports()));
        cardModeratorTools.setVisibility(View.VISIBLE);
    }

    /*
     * SHOW PROFILE: fills the profile card with the saved details.
     */
    private void showProfile() {
        UserProfile profile = UserProfile.load(requireContext());

        tvProfileName.setText(profile.getFullName());
        tvProfileUsername.setText(getString(R.string.profile_username_format, profile.getUsername()));

        // Hide the bio and school details when they're empty
        tvProfileBio.setText(profile.getBio());
        tvProfileBio.setVisibility(TextUtils.isEmpty(profile.getBio()) ? View.GONE : View.VISIBLE);
        String schoolDetails = profile.getSchoolDetails();
        tvProfileDetails.setText(schoolDetails);
        tvProfileDetails.setVisibility(TextUtils.isEmpty(schoolDetails) ? View.GONE : View.VISIBLE);

        // Show the photo if one was picked, otherwise the initials
        if (TextUtils.isEmpty(profile.getPhotoUri())) {
            ivProfilePhoto.setVisibility(View.GONE);
            tvProfileInitials.setVisibility(View.VISIBLE);
            tvProfileInitials.setText(profile.getInitials());
        } else {
            ivProfilePhoto.setImageURI(null); // forces a refresh when the photo changes
            ivProfilePhoto.setImageURI(Uri.parse(profile.getPhotoUri()));
            ivProfilePhoto.setVisibility(View.VISIBLE);
            tvProfileInitials.setVisibility(View.GONE);
        }
    }

    /*
     * MY CONTRIBUTIONS: lists the student's posts (with review status) and place reviews.
     * Prototype: sample data from MyContributionsDataSource.
     */
    private void showMyContributions() {
        List<MyPost> posts = MyContributionsDataSource.getMyPosts();
        List<MyReview> reviews = MyContributionsDataSource.getMyReviews();

        tvPostsHeading.setText(getString(R.string.profile_posts_count, posts.size()));
        rvMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMyPosts.setAdapter(new MyPostAdapter(posts));

        tvReviewsHeading.setText(getString(R.string.profile_reviews_count, reviews.size()));
        rvMyReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMyReviews.setAdapter(new MyReviewAdapter(reviews));
    }

    /*
     * LOG OUT: asks first, so a stray tap doesn't log the student out.
     */
    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", (dialog, which) -> logOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /*
     * Opens the Login screen and clears every screen behind it,
     * so pressing Back after logging out can't return to the app.
     * Prototype: no account is saved yet, so there's nothing else to clear.
     */
    private void logOut() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}