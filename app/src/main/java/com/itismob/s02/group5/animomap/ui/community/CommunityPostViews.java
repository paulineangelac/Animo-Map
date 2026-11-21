package com.itismob.s02.group5.animomap.ui.community;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

final class CommunityPostViews {
    private CommunityPostViews() { }

    static void bindAvatar(TextView initialsView, ImageView photo, String initials,
                           int photoResource, String photoUri) {
        initialsView.setText(initials);
        photo.setImageResource(photoResource);
        if (!TextUtils.isEmpty(photoUri)) photo.setImageURI(Uri.parse(photoUri));
        boolean hasPhoto = photo.getDrawable() != null;
        photo.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);
        initialsView.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
    }

    static void bindLocation(TextView label, CommunityPost post) {
        PostLocation location = post.getLocation();
        String text = location == null ? post.getLocationName() : location.label(label.getContext());
        label.setText(text);
        label.setVisibility(text == null || text.trim().isEmpty() ? View.GONE : View.VISIBLE);
    }

    static void bindTitle(TextView title, CommunityPost post) {
        title.setText(post.getTitle());
        title.setVisibility(TextUtils.isEmpty(post.getTitle()) ? View.GONE : View.VISIBLE);
    }

    static void bindMapReviewLabel(View label, CommunityPost post) {
        label.setVisibility(post.getMapReviewStatus() == CommunityPost.MapReviewStatus.PENDING
                ? View.VISIBLE : View.GONE);
    }
}
