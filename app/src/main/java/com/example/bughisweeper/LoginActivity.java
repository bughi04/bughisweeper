package com.example.bughisweeper;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Login activity with password constraints and validation
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnToggleMode;
    private TextView tvPasswordStrength;
    private TextView tvPasswordRequirements;
    private ProgressBar pbPasswordStrength;

    private boolean isRegisterMode = false;
    private SharedPreferences prefs;

    // Password constraints
    private static final int MIN_LENGTH = 10;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern NUMBERS = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    private static final Pattern CONSECUTIVE_CHARS = Pattern.compile("(.)\\1{2,}");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("bughisweeper_auth", MODE_PRIVATE);

        // Check if user is already logged in
        if (prefs.getBoolean("is_logged_in", false)) {
            navigateToMainActivity();
            return;
        }

        initializeViews();
        setupListeners();
        updateUI();
    }

    private void initializeViews() {
        // Try to find views, but don't crash if they don't exist
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnToggleMode = findViewById(R.id.btnToggleMode);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        tvPasswordRequirements = findViewById(R.id.tvPasswordRequirements);
        pbPasswordStrength = findViewById(R.id.pbPasswordStrength);

        // Set password requirements text if view exists
        if (tvPasswordRequirements != null) {
            tvPasswordRequirements.setText(
                    "Password must contain:\n" +
                            "• At least 10 characters\n" +
                            "• At least 2 uppercase letters\n" +
                            "• At least 2 lowercase letters\n" +
                            "• At least 2 numbers\n" +
                            "• At least 2 special characters (!@#$%^&*)\n" +
                            "• No more than 2 consecutive identical characters"
            );
        }
    }

    private void setupListeners() {
        if (etPassword != null) {
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePasswordStrength(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptLogin();
                }
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attemptRegister();
                }
            });
        }

        if (btnToggleMode != null) {
            btnToggleMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleMode();
                }
            });
        }
    }

    private void updatePasswordStrength(String password) {
        if (tvPasswordStrength == null || pbPasswordStrength == null || tvPasswordRequirements == null) {
            return; // Skip if views don't exist
        }

        PasswordValidation validation = validatePassword(password);

        // Update strength meter
        int strength = calculatePasswordStrength(password);
        ObjectAnimator.ofInt(pbPasswordStrength, "progress", strength)
                .setDuration(300)
                .start();

        // Update strength text and color
        String strengthText;
        int color;

        if (strength < 20) {
            strengthText = "Very Weak";
            color = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        } else if (strength < 40) {
            strengthText = "Weak";
            color = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
        } else if (strength < 60) {
            strengthText = "Fair";
            color = ContextCompat.getColor(this, android.R.color.holo_orange_light);
        } else if (strength < 80) {
            strengthText = "Good";
            color = ContextCompat.getColor(this, android.R.color.holo_green_light);
        } else {
            strengthText = "Strong";
            color = ContextCompat.getColor(this, android.R.color.holo_green_dark);
        }

        tvPasswordStrength.setText("Password Strength: " + strengthText);
        tvPasswordStrength.setTextColor(color);

        // API 21 compatible way to set progress bar color
        pbPasswordStrength.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

        // Show specific validation errors
        if (!password.isEmpty() && !validation.isValid) {
            StringBuilder errors = new StringBuilder("Missing requirements:\n");
            if (!validation.hasMinLength) errors.append("• Minimum 10 characters\n");
            if (!validation.hasUppercase) errors.append("• At least 2 uppercase letters\n");
            if (!validation.hasLowercase) errors.append("• At least 2 lowercase letters\n");
            if (!validation.hasNumbers) errors.append("• At least 2 numbers\n");
            if (!validation.hasSpecialChars) errors.append("• At least 2 special characters\n");
            if (!validation.noConsecutiveChars) errors.append("• No more than 2 consecutive identical characters\n");

            tvPasswordRequirements.setText(errors.toString());
            tvPasswordRequirements.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else if (validation.isValid) {
            tvPasswordRequirements.setText("✓ All requirements met!");
            tvPasswordRequirements.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
    }

    private PasswordValidation validatePassword(String password) {
        PasswordValidation validation = new PasswordValidation();

        validation.hasMinLength = password.length() >= MIN_LENGTH;
        validation.hasUppercase = countMatches(password, UPPERCASE) >= 2;
        validation.hasLowercase = countMatches(password, LOWERCASE) >= 2;
        validation.hasNumbers = countMatches(password, NUMBERS) >= 2;
        validation.hasSpecialChars = countMatches(password, SPECIAL_CHARS) >= 2;
        validation.noConsecutiveChars = !CONSECUTIVE_CHARS.matcher(password).find();

        validation.isValid = validation.hasMinLength &&
                validation.hasUppercase &&
                validation.hasLowercase &&
                validation.hasNumbers &&
                validation.hasSpecialChars &&
                validation.noConsecutiveChars;

        return validation;
    }

    private int countMatches(String text, Pattern pattern) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (pattern.matcher(String.valueOf(text.charAt(i))).matches()) {
                count++;
            }
        }
        return count;
    }

    private int calculatePasswordStrength(String password) {
        if (password.isEmpty()) return 0;

        int score = 0;

        // Length bonus
        score += Math.min(password.length() * 4, 40);

        // Character variety bonus
        if (UPPERCASE.matcher(password).find()) score += 10;
        if (LOWERCASE.matcher(password).find()) score += 10;
        if (NUMBERS.matcher(password).find()) score += 10;
        if (SPECIAL_CHARS.matcher(password).find()) score += 15;

        // Complexity bonus - API 21 compatible way
        int uniqueChars = 0;
        boolean[] seen = new boolean[256]; // ASCII characters
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c < 256 && !seen[c]) {
                seen[c] = true;
                uniqueChars++;
            }
        }
        score += uniqueChars * 2;

        // Penalty for consecutive characters
        if (CONSECUTIVE_CHARS.matcher(password).find()) score -= 20;

        return Math.max(0, Math.min(100, score));
    }

    private void attemptLogin() {
        if (etUsername == null || etPassword == null) {
            Toast.makeText(this, "Login form not properly initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String storedPasswordHash = prefs.getString("password_" + username, null);
        if (storedPasswordHash != null && storedPasswordHash.equals(hashPassword(password))) {
            // Successful login
            prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("current_user", username)
                    .apply();

            Toast.makeText(this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
            navigateToMainActivity();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();

            // Add login attempt animation if password field exists
            if (etPassword != null) {
                ObjectAnimator shake = ObjectAnimator.ofFloat(etPassword, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
                shake.setDuration(500);
                shake.start();
            }
        }
    }

    private void attemptRegister() {
        if (etUsername == null || etPassword == null || etConfirmPassword == null) {
            Toast.makeText(this, "Registration form not properly initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validatePassword(password).isValid) {
            Toast.makeText(this, "Password does not meet requirements", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username already exists
        if (prefs.contains("password_" + username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register new user
        prefs.edit()
                .putString("password_" + username, hashPassword(password))
                .putBoolean("is_logged_in", true)
                .putString("current_user", username)
                .apply();

        Toast.makeText(this, "Account created successfully! Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
        navigateToMainActivity();
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        updateUI();

        // Add smooth transition animation
        View container = findViewById(android.R.id.content);
        if (container != null) {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(container, "alpha", 1f, 0f);
            fadeOut.setDuration(150);
            fadeOut.start();

            fadeOut.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                    if (animation.getAnimatedFraction() == 1f) {
                        updateUI();
                        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(container, "alpha", 0f, 1f);
                        fadeIn.setDuration(150);
                        fadeIn.start();
                    }
                }
            });
        }
    }

    private void updateUI() {
        if (btnLogin == null || btnRegister == null || btnToggleMode == null) {
            return; // Skip if buttons don't exist
        }

        if (isRegisterMode) {
            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.VISIBLE);
            btnToggleMode.setText("Already have an account? Login");

            if (etConfirmPassword != null) etConfirmPassword.setVisibility(View.VISIBLE);
            if (tvPasswordStrength != null) tvPasswordStrength.setVisibility(View.VISIBLE);
            if (tvPasswordRequirements != null) tvPasswordRequirements.setVisibility(View.VISIBLE);
            if (pbPasswordStrength != null) pbPasswordStrength.setVisibility(View.VISIBLE);
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.GONE);
            btnToggleMode.setText("Don't have an account? Register");

            if (etConfirmPassword != null) etConfirmPassword.setVisibility(View.GONE);
            if (tvPasswordStrength != null) tvPasswordStrength.setVisibility(View.GONE);
            if (tvPasswordRequirements != null) tvPasswordRequirements.setVisibility(View.GONE);
            if (pbPasswordStrength != null) pbPasswordStrength.setVisibility(View.GONE);
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

        // Add transition animation
        try {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } catch (Exception e) {
            // Ignore if animations don't exist
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (not recommended in production)
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent going back from login screen
        moveTaskToBack(true);
    }

    private static class PasswordValidation {
        boolean isValid = false;
        boolean hasMinLength = false;
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumbers = false;
        boolean hasSpecialChars = false;
        boolean noConsecutiveChars = true;
    }
}