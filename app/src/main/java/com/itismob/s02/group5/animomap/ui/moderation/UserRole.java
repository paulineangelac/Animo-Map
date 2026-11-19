package com.itismob.s02.group5.animomap.ui.moderation;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 * Remembers whether the person who logged in is a moderator.
 * Prototype: there is one demo moderator account. In MCO3 the role will come from Firebase.
 */
public final class UserRole {

    // Log in with this email to see the moderator tools
    public static final String MODERATOR_EMAIL = "moderator@dlsu.edu.ph";

    private static final String PREFS_NAME = "user_role";
    private static final String KEY_IS_MODERATOR = "is_moderator";

    private UserRole() {
    }

    /*
     * SAVE LOGIN: called every time someone logs in. Only the moderator email gets the moderator role.
     */
    public static void saveLogin(Context context, String email) {
        boolean isModerator = email.trim().toLowerCase(Locale.ROOT).equals(MODERATOR_EMAIL);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_MODERATOR, isModerator).apply();
    }

    /*
     * IS MODERATOR: true if the person logged in is a moderator.
     */
    public static boolean isModerator(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_MODERATOR, false);
    }
}