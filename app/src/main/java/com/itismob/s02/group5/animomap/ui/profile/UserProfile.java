package com.itismob.s02.group5.animomap.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.itismob.s02.group5.animomap.R;

/**
 * The logged-in student's profile details, saved on the phone with SharedPreferences.
 * Prototype: it starts with the sample student shown on the Profile tab.
 * Firebase will store real profiles in MCO3.
 */
public class UserProfile {

    // Name of the SharedPreferences file and the key for each detail
    private static final String PREFS_NAME = "user_profile";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_BIO = "bio";
    private static final String KEY_COLLEGE = "college";
    private static final String KEY_MAJOR = "major";
    private static final String KEY_ID_NUMBER = "id_number";
    private static final String KEY_PHOTO_URI = "photo_uri";

    // Sample username used until the student picks their own
    private static final String SAMPLE_USERNAME = "alexsantos";

    private String fullName;
    private String username;   // saved without the "@"
    private String bio;
    private String college;    // "" when no college is chosen
    private String major;
    private String idNumber;
    private String photoUri;   // file URI of the saved photo, or "" for no photo

    /*
     * LOAD: reads the saved profile. If nothing was saved yet, uses the sample student.
     */
    public static UserProfile load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        UserProfile profile = new UserProfile();
        profile.fullName = prefs.getString(KEY_FULL_NAME, context.getString(R.string.profile_sample_name));
        profile.username = prefs.getString(KEY_USERNAME, SAMPLE_USERNAME);
        profile.bio = prefs.getString(KEY_BIO, context.getString(R.string.profile_sample_bio));
        profile.college = prefs.getString(KEY_COLLEGE, "");
        profile.major = prefs.getString(KEY_MAJOR, "");
        profile.idNumber = prefs.getString(KEY_ID_NUMBER, "");
        profile.photoUri = prefs.getString(KEY_PHOTO_URI, "");
        return profile;
    }

    /*
     * SAVE: writes every detail to SharedPreferences, so it's still there after the app closes.
     */
    public void save(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_USERNAME, username)
                .putString(KEY_BIO, bio)
                .putString(KEY_COLLEGE, college)
                .putString(KEY_MAJOR, major)
                .putString(KEY_ID_NUMBER, idNumber)
                .putString(KEY_PHOTO_URI, photoUri)
                .apply();
    }

    // ---------- Helpers for the Profile screen ----------

    /*
     * Initials for the avatar when there's no photo, e.g. "Alex Santos" becomes "AS".
     */
    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        for (String word : fullName.trim().split("\\s+")) {
            if (!word.isEmpty() && initials.length() < 2) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return initials.toString();
    }

    /*
     * School details for the Profile card, skipping anything left empty.
     * Example: "College of Computer Studies (CCS)" on one line, "BS Information Systems · ID 12345678" below.
     */
    public String getSchoolDetails() {
        String secondLine = major;
        if (!TextUtils.isEmpty(idNumber)) {
            secondLine = TextUtils.isEmpty(secondLine) ? "ID " + idNumber : secondLine + " · ID " + idNumber;
        }

        if (TextUtils.isEmpty(college)) {
            return secondLine;
        }
        if (TextUtils.isEmpty(secondLine)) {
            return college;
        }
        return college + "\n" + secondLine;
    }

    // ---------- Getters and setters ----------

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}