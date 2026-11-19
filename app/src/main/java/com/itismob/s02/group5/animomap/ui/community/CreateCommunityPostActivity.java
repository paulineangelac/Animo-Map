package com.itismob.s02.group5.animomap.ui.community;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.itismob.s02.group5.animomap.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class CreateCommunityPostActivity extends AppCompatActivity {
    static final String EXTRA_POST_ID = "community.post_id";
    private static final String STATE_TYPE = "post_type";

    private final EnumMap<CommunityPostType, Integer> chipIds =
            new EnumMap<>(CommunityPostType.class);

    private View root;
    private View poiHelper;
    private EditText description;
    private EditText placeName;
    private TextView locationLabel;
    private View locationPreview;
    private LinearLayout photoPreviews;
    private HorizontalScrollView photoScroll;
    private ChipGroup categoryChips;
    private DraftPhotoViewModel draftPhotos;
    private CommunityPostType postType;
    private PostLocation location;
    private boolean submitted;

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(
                            DraftPhotoViewModel.MAX_PHOTOS),
                    this::addPhotos);
    private final ActivityResultLauncher<PickVisualMediaRequest> lastPhotoPicker =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) addPhotos(Collections.singletonList(uri));
            });
    private final ActivityResultLauncher<Intent> locationPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (result.getResultCode() != RESULT_OK || data == null) return;
                PostLocation selected = PostLocation.fromBundle(
                        data.getBundleExtra(CommunityLocationPickerActivity.EXTRA_LOCATION));
                if (selected != null) {
                    location = selected;
                    showLocation();
                }
            });

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        CommunityWindow.enableEdgeToEdge(this);
        setContentView(R.layout.activity_create_community_post);
        root = findViewById(R.id.create_post_root);
        CommunityWindow.applyInsets(root, true);
        poiHelper = findViewById(R.id.create_post_poi_helper);
        description = findViewById(R.id.create_post_description);
        placeName = findViewById(R.id.create_post_place_name);
        photoPreviews = findViewById(R.id.create_post_photos);
        photoScroll = findViewById(R.id.create_post_photo_scroll);
        locationPreview = findViewById(R.id.create_post_location_preview);
        locationLabel = findViewById(R.id.create_post_location_label);
        categoryChips = findViewById(R.id.create_post_categories);
        draftPhotos = new ViewModelProvider(this).get(DraftPhotoViewModel.class);

        if (state != null) {
            String savedType = state.getString(STATE_TYPE);
            if (savedType != null) postType = CommunityPostType.valueOf(savedType);
            location = PostLocation.fromBundle(
                    state.getBundle(CommunityLocationPickerActivity.EXTRA_LOCATION));
        }

        setupCategories();
        showCategory();
        draftPhotos.previews().observe(this, this::showPhotoPreviews);
        showLocation();
        findViewById(R.id.create_post_back).setOnClickListener(view -> finish());
        findViewById(R.id.create_post_submit).setOnClickListener(view -> submit());
        findViewById(R.id.create_post_add_photos).setOnClickListener(view -> openPhotoPicker());
        findViewById(R.id.create_post_add_location).setOnClickListener(view -> openLocationPicker());
        locationLabel.setOnClickListener(view -> openLocationPicker());
        findViewById(R.id.create_post_remove_location).setOnClickListener(view -> {
            location = null;
            showLocation();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        if (postType != null) state.putString(STATE_TYPE, postType.name());
        if (location != null) {
            state.putBundle(CommunityLocationPickerActivity.EXTRA_LOCATION, location.toBundle());
        }
        // Android restores the EditText values and cursor positions by view ID.
    }

    private void setupCategories() {
        int[][] states = {{android.R.attr.state_checked}, {}};
        ColorStateList backgrounds = new ColorStateList(states, new int[] {
                getColor(R.color.nav_active_indicator), getColor(R.color.surface_elevated)
        });
        ColorStateList labels = new ColorStateList(states, new int[] {
                getColor(R.color.highlight_selected), getColor(R.color.text_secondary)
        });
        for (CommunityPostType type : CommunityPostType.values()) {
            Chip chip = new Chip(this);
            chip.setId(View.generateViewId());
            chip.setSaveEnabled(false);
            chip.setText(type.labelRes);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setEnsureMinTouchTargetSize(true);
            chip.setChipBackgroundColor(backgrounds);
            chip.setTextColor(labels);
            chip.setChipStrokeWidth(0);
            categoryChips.addView(chip);
            chipIds.put(type, chip.getId());
            chip.setOnClickListener(view -> {
                postType = type;
                showCategory();
            });
        }
        Integer selectedChipId = postType == null ? null : chipIds.get(postType);
        if (selectedChipId != null) categoryChips.check(selectedChipId);
    }

    private void showCategory() {
        int visibility = postType == CommunityPostType.POI_RECOMMENDATION ? View.VISIBLE : View.GONE;
        poiHelper.setVisibility(visibility);
        placeName.setVisibility(visibility);
    }

    private void openPhotoPicker() {
        int remaining = DraftPhotoViewModel.MAX_PHOTOS - draftPhotos.photos().size();
        if (remaining == 0) {
            showPhotoLimit();
            return;
        }
        PickVisualMediaRequest.Builder request = new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE);
        if (remaining == 1) {
            lastPhotoPicker.launch(request.build());
        } else {
            photoPicker.launch(request.setMaxItems(remaining).build());
        }
    }

    private void addPhotos(List<Uri> picked) {
        if (draftPhotos.add(getApplicationContext().getContentResolver(), picked)) {
            showPhotoLimit();
        }
    }

    private void showPhotoPreviews(List<DraftPhotoViewModel.Photo> photos) {
        for (int i = 0; i < photoPreviews.getChildCount(); i++) {
            PostPhotoLoader.clear(photoPreviews.getChildAt(i).findViewById(R.id.community_preview_photo));
        }
        photoPreviews.removeAllViews();
        photoScroll.setVisibility(photos.isEmpty() ? View.GONE : View.VISIBLE);
        for (int i = 0; i < photos.size(); i++) {
            DraftPhotoViewModel.Photo photo = photos.get(i);
            View preview = getLayoutInflater().inflate(
                    R.layout.item_community_photo_preview, photoPreviews, false);
            ImageView image = preview.findViewById(R.id.community_preview_photo);
            image.setClipToOutline(true);
            if (photo.image != null) {
                PostPhotoLoader.load(image, photo.image, 104, i + 1, photos.size());
            } else {
                image.setImageResource(R.drawable.ic_community_photo);
                image.setScaleType(ImageView.ScaleType.CENTER);
                int hint = photo.job == null ? R.string.community_photo_unavailable
                        : R.string.community_photo_loading;
                image.setContentDescription(getString(hint, i + 1, photos.size()));
            }
            View remove = preview.findViewById(R.id.community_preview_remove);
            remove.setContentDescription(getString(R.string.community_remove_photo, i + 1));
            remove.setOnClickListener(view -> draftPhotos.remove(photo));
            photoPreviews.addView(preview);
        }
    }

    private void showPhotoLimit() {
        showFeedback(getString(R.string.community_max_photos, DraftPhotoViewModel.MAX_PHOTOS));
    }

    private void showFeedback(CharSequence message) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_SHORT);
        snackbar.setAnchorView(R.id.create_post_actions);
        snackbar.setAnchorViewLayoutListenerEnabled(true);
        snackbar.show();
    }

    private void openLocationPicker() {
        Intent intent = new Intent(this, CommunityLocationPickerActivity.class);
        if (location != null) {
            intent.putExtra(CommunityLocationPickerActivity.EXTRA_LOCATION, location.toBundle());
        }
        locationPicker.launch(intent);
    }

    private void showLocation() {
        locationPreview.setVisibility(location == null ? View.GONE : View.VISIBLE);
        locationLabel.setText(location == null ? null : location.label(this));
    }

    private void submit() {
        if (submitted) return;
        String body = CommunityFeedDataSource.trimDraftText(description.getText().toString());
        String title = CommunityFeedDataSource.trimDraftText(placeName.getText().toString());
        int error = CommunityFeedDataSource.validateDraft(postType, body, title, location != null);
        if (error != 0) {
            if (error == R.string.community_require_description) description.requestFocus();
            if (error == R.string.community_require_place_name) placeName.requestFocus();
            showFeedback(getString(error));
            return;
        }

        List<byte[]> photos = new ArrayList<>();
        for (DraftPhotoViewModel.Photo photo : draftPhotos.photos()) {
            if (photo.image == null) {
                int message = photo.job == null ? R.string.community_photo_remove_unavailable
                        : R.string.community_photos_preparing;
                showFeedback(getString(message));
                return;
            }
            photos.add(photo.image);
        }
        submitted = true;
        CommunityPost post = CommunityFeedDataSource.submitPost(
                getString(R.string.profile_sample_username),
                getString(R.string.community_prototype_initials), postType,
                title, body, location, photos);
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_POST_ID, post.getId()));
        finish();
    }
}
