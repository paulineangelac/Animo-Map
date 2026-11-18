package com.itismob.s02.group5.animomap.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.itismob.s02.group5.animomap.R;
import com.itismob.s02.group5.animomap.ui.auth.LoginActivity;

/** Profile screen: shows the student's profile and lets them log out. */
public class ProfileFragment extends Fragment {

    // Views
    private Button btnLogout;

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
        btnLogout = view.findViewById(R.id.btnLogout);

        // Buttons
        btnLogout.setOnClickListener(v -> confirmLogout());
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