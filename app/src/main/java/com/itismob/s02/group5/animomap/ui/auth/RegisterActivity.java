package com.itismob.s02.group5.animomap.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.itismob.s02.group5.animomap.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 8;

    // Views
    private ImageButton btnBack;
    private EditText etFullName, etBirthday, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    // Birthday picked in the calendar (null until the user picks one)
    private Calendar selectedBirthday = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Light status bar icons, since Animo Map always uses a dark background
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_register);

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
        etFullName = findViewById(R.id.etFullName);
        etBirthday = findViewById(R.id.etBirthday);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Tapping the birthday field opens a calendar
        etBirthday.setOnClickListener(v -> showBirthdayPicker());

        // Buttons
        btnRegister.setOnClickListener(v -> register());

        // The back arrow and the "Log in" link both return to the Login screen
        btnBack.setOnClickListener(v -> finish());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    /*
     * BIRTHDAY: opens a date picker. Dates in the future can't be chosen.
     */
    private void showBirthdayPicker() {
        Calendar start = Calendar.getInstance();
        if (selectedBirthday != null) {
            start = selectedBirthday; // reopen on the date picked before
        } else {
            start.add(Calendar.YEAR, -18); // start around a typical student's birth year
        }

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedBirthday = Calendar.getInstance();
            selectedBirthday.set(year, month, day);

            SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            etBirthday.setText(format.format(selectedBirthday.getTime()));
            etBirthday.setError(null);
        }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    /*
     * REGISTER: checks every field, then sends the new email back to the Login screen.
     * Prototype: the account is not saved yet (Firebase comes in MCO3).
     */
    private void register() {
        String fullName = etFullName.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(fullName)) {
            showError(etFullName, "Enter your full name");
            return;
        }
        if (TextUtils.isEmpty(birthday)) {
            // The birthday field can't take focus, so a Toast makes the message visible
            etBirthday.setError("Choose your birthday");
            Toast.makeText(this, "Choose your birthday", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            showError(etEmail, "Enter your DLSU email");
            return;
        }
        if (!LoginActivity.isDlsuEmail(email)) {
            showError(etEmail, "Use your @dlsu.edu.ph email");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError(etPassword, "Use at least " + MIN_PASSWORD_LENGTH + " characters");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            showError(etConfirmPassword, "Enter your password again");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, "Passwords don't match");
            return;
        }

        // Send the new email back to LoginActivity, which fills it in for the user
        Intent result = new Intent();
        result.putExtra(LoginActivity.EXTRA_EMAIL, email);
        setResult(RESULT_OK, result);
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
}
