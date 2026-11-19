package com.itismob.s02.group5.animomap.ui.profile;

import android.text.TextUtils;

/**
 * Another student's public profile info.
 * There are no setters, so nothing here can be changed from their profile screen.
 */
public class OtherUser {

    private final String name;
    private final String username;
    private final String bio;
    private final String college;
    private final String program;
    private final int avatarResId;   // 0 means no photo, so the initials are shown

    public OtherUser(String name, String username, String bio,
                     String college, String program, int avatarResId) {
        this.name = name;
        this.username = username;
        this.bio = bio;
        this.college = college;
        this.program = program;
        this.avatarResId = avatarResId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getBio() {
        return bio;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    /*
     * INITIALS: first letters of the first two words. Example: "Mika S." becomes "MS".
     */
    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        for (String word : name.trim().split("\\s+")) {
            if (!word.isEmpty() && initials.length() < 2) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return initials.toString();
    }

    /*
     * SCHOOL DETAILS: college on one line and program below, skipping anything left empty.
     */
    public String getSchoolDetails() {
        if (TextUtils.isEmpty(college)) {
            return program;
        }
        if (TextUtils.isEmpty(program)) {
            return college;
        }
        return college + "\n" + program;
    }
}