package com.itismob.s02.group5.animomap.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.itismob.s02.group5.animomap.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    // Usernames already used by the sample community authors.
    // Prototype: Firebase will check real usernames in MCO3.
    private static final String[] TAKEN_USERNAMES = {"mika_s", "bea_r", "luis_d", "moderator", "animomap"};

    // Lowercase letters, numbers, dots, or underscores, 3 to 20 characters
    private static final String USERNAME_PATTERN = "[a-z0-9._]{3,20}";

    // Views
    private ImageButton btnBack;
    private ShapeableImageView ivEditPhoto;
    private TextView tvEditInitials, tvChangePhoto;
    private EditText etFullName, etUsername, etBio, etMajor, etIdNumber;
    private Spinner spCollege;
    private Button btnSaveProfile;

    // The profile being edited, and the photo chosen on this screen ("" for none)
    private UserProfile profile;
    private String selectedPhotoUri = "";

    // Opens the photo picker; the chosen photo is copied into the app so it still shows later
    private final ActivityResultLauncher<PickVisualMediaRequest> pickPhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    return; // user closed the picker without choosing
                }
                String savedUri = saveImageCopy(uri);
                if (savedUri != null) {
                    selectedPhotoUri = savedUri;
                    showPhoto();
                } else {
                    Toast.makeText(this, "Could not load that photo", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Light status bar icons, since Animo Map always uses a dark background
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_edit_profile);

        // Keep the screen clear of the status bar, navigation bar and keyboard
        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        final int padLeft = root.getPaddingLeft();
        final int padTop = root.getPaddingTop();
        final int padRight = root.getPaddingRight();
        final int padBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(padLeft + bars.left, padTop + bars.top, padRight + bars.right, padBottom + bars.bottom);
            return insets;
        });

        // Instantiate views
        btnBack = findViewById(R.id.btnBack);
        ivEditPhoto = findViewById(R.id.ivEditPhoto);
        tvEditInitials = findViewById(R.id.tvEditInitials);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etBio = findViewById(R.id.etBio);
        spCollege = findViewById(R.id.spCollege);
        etMajor = findViewById(R.id.etMajor);
        etIdNumber = findViewById(R.id.etIdNumber);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // College dropdown, filled from the list in strings_profile.xml
        ArrayAdapter<CharSequence> collegeAdapter = ArrayAdapter.createFromResource(
                this, R.array.dlsu_colleges, android.R.layout.simple_spinner_item);
        collegeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCollege.setAdapter(collegeAdapter);

        // Fill the form with the saved profile
        profile = UserProfile.load(this);
        fillForm();

        // Tapping the photo or "Change photo" opens the photo picker
        View.OnClickListener openPicker = v -> pickPhotoLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
        ivEditPhoto.setOnClickListener(openPicker);
        tvEditInitials.setOnClickListener(openPicker);
        tvChangePhoto.setOnClickListener(openPicker);

        // Buttons
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnBack.setOnClickListener(v -> finish()); // leaves without saving
    }

    /*
     * FILL FORM: puts the saved details into the fields.
     */
    private void fillForm() {
        etFullName.setText(profile.getFullName());
        etUsername.setText(profile.getUsername());
        etBio.setText(profile.getBio());
        etMajor.setText(profile.getMajor());
        etIdNumber.setText(profile.getIdNumber());

        // Select the saved college in the dropdown (position 0 means "Select your college")
        String[] colleges = getResources().getStringArray(R.array.dlsu_colleges);
        for (int i = 1; i < colleges.length; i++) {
            if (colleges[i].equals(profile.getCollege())) {
                spCollege.setSelection(i);
                break;
            }
        }

        selectedPhotoUri = profile.getPhotoUri();
        showPhoto();
    }

    /*
     * SAVE: checks the fields, saves the profile, then returns to the Profile tab.
     */
    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        // Usernames are saved in lowercase and without a leading "@"
        String username = etUsername.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (username.startsWith("@")) {
            username = username.substring(1);
        }
        String bio = etBio.getText().toString().trim();
        String major = etMajor.getText().toString().trim();
        String idNumber = etIdNumber.getText().toString().trim();
        // Position 0 is "Select your college", which means no college
        String college = spCollege.getSelectedItemPosition() == 0
                ? "" : spCollege.getSelectedItem().toString();

        if (TextUtils.isEmpty(fullName)) {
            showError(etFullName, "Enter your full name");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            showError(etUsername, "Enter a username");
            return;
        }
        if (!username.matches(USERNAME_PATTERN)) {
            showError(etUsername, "Use 3–20 lowercase letters, numbers, dots, or underscores");
            return;
        }
        if (isUsernameTaken(username)) {
            showError(etUsername, "That username is taken. Try another.");
            return;
        }
        if (!TextUtils.isEmpty(idNumber) && !idNumber.matches("\\d{8}")) {
            showError(etIdNumber, "ID number must be 8 digits");
            return;
        }

        profile.setFullName(fullName);
        profile.setUsername(username);
        profile.setBio(bio);
        profile.setCollege(college);
        profile.setMajor(major);
        profile.setIdNumber(idNumber);
        profile.setPhotoUri(selectedPhotoUri);
        profile.save(this);

        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        finish(); // the Profile tab refreshes itself in onResume()
    }

    // ---------- Helpers ----------

    /*
     * True if another student already uses this username.
     * Keeping your own current username is always allowed.
     */
    private boolean isUsernameTaken(String username) {
        if (username.equals(profile.getUsername())) {
            return false;
        }
        for (String taken : TAKEN_USERNAMES) {
            if (taken.equals(username)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Shows the chosen photo, or the initials if there's no photo yet.
     */
    private void showPhoto() {
        if (TextUtils.isEmpty(selectedPhotoUri)) {
            ivEditPhoto.setVisibility(View.GONE);
            tvEditInitials.setVisibility(View.VISIBLE);
            tvEditInitials.setText(profile.getInitials());
        } else {
            ivEditPhoto.setImageURI(null); // forces a refresh when the photo changes
            ivEditPhoto.setImageURI(Uri.parse(selectedPhotoUri));
            ivEditPhoto.setVisibility(View.VISIBLE);
            tvEditInitials.setVisibility(View.GONE);
        }
    }

    /*
     * Shows a red error on the field and moves the cursor there.
     */
    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }

    /*
     * Copies the picked photo into the app's private storage (resized to about 512px) and
     * returns its file URI as a string. Picker URIs stop working after the app restarts,
     * so storing our own copy keeps the photo visible. Same approach as Exercise 4.
     */
    private String saveImageCopy(Uri sourceUri) {
        try {
            // 1. Read only the size first
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = getContentResolver().openInputStream(sourceUri)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }

            // 2. Pick a downscale factor so big camera photos don't use too much memory
            int sampleSize = 1;
            while (bounds.outWidth / (sampleSize * 2) >= 512 && bounds.outHeight / (sampleSize * 2) >= 512) {
                sampleSize *= 2;
            }

            // 3. Decode the smaller version
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            Bitmap bitmap;
            try (InputStream in = getContentResolver().openInputStream(sourceUri)) {
                bitmap = BitmapFactory.decodeStream(in, null, options);
            }
            if (bitmap == null) {
                return null;
            }

            // 4. Save it as a JPEG (photos don't need transparency, and JPEG files are smaller)
            File folder = new File(getFilesDir(), "profile_photos");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder, "profile_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }
            return Uri.fromFile(file).toString();
        } catch (IOException | SecurityException e) {
            return null;
        }
    }
}