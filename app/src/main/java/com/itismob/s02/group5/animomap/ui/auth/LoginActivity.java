package com.itismob.s02.group5.animomap.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.itismob.s02.group5.animomap.MainActivity;
import com.itismob.s02.group5.animomap.R;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    // Key for passing the new account's email from RegisterActivity back to this screen
    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";

    // Only DLSU student emails can log in, e.g. juan_delacruz@dlsu.edu.ph
    private static final String DLSU_DOMAIN = "@dlsu.edu.ph";

    // Views
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;

    // Opens the Register screen; when an account is created, its email comes back here
    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return; // user went back without creating an account
                }
                String newEmail = result.getData().getStringExtra(EXTRA_EMAIL);
                etEmail.setText(newEmail);
                etPassword.setText("");
                etPassword.requestFocus();
                Toast.makeText(this, "Account created. Log in to continue.", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Light status bar icons, since Animo Map always uses a dark background
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_login);

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
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        // Buttons
        btnLogin.setOnClickListener(v -> logIn());
        tvGoToRegister.setOnClickListener(v ->
                registerLauncher.launch(new Intent(this, RegisterActivity.class)));

        // Pressing "Done" on the keyboard also logs in
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                logIn();
                return true;
            }
            return false;
        });
    }

    /*
     * LOG IN: checks the email and password, then opens the main app (Map tab).
     * Prototype: there is no database yet, so any valid DLSU email can log in.
     */
    private void logIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            showError(etEmail, "Enter your DLSU email");
            return;
        }
        if (!isDlsuEmail(email)) {
            showError(etEmail, "Use your @dlsu.edu.ph email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showError(etPassword, "Enter your password");
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        // Removes Login from the back stack, so pressing Back in the app won't return here
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ---------- Helpers ----------

    /*
     * Shows a red error on the field and moves the cursor there.
     */
    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }

    /*
     * True if the email is a valid address ending in @dlsu.edu.ph.
     * RegisterActivity uses this too, so both screens follow the same rule.
     */
    static boolean isDlsuEmail(String email) {
        String lower = email.trim().toLowerCase(Locale.ROOT);
        return lower.endsWith(DLSU_DOMAIN)
                && lower.length() > DLSU_DOMAIN.length()
                && Patterns.EMAIL_ADDRESS.matcher(lower).matches();
    }
}
