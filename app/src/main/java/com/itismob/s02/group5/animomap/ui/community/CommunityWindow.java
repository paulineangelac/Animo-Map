package com.itismob.s02.group5.animomap.ui.community;

import android.graphics.Color;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

final class CommunityWindow {
    private CommunityWindow() { }

    static void enableEdgeToEdge(AppCompatActivity activity) {
        EdgeToEdge.enable(activity, SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
    }

    static void applyInsets(View root, boolean includeKeyboard) {
        int types = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();
        final int insetTypes = includeKeyboard ? types | WindowInsetsCompat.Type.ime() : types;
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets padding = insets.getInsets(insetTypes);
            view.setPadding(padding.left, padding.top, padding.right, padding.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
