package com.example.bughisweeper;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Enhanced Settings Activity with video section and improved features
 */
public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerTheme;
    private Switch switchSound;
    private Switch switchVibration;
    private Button btnResetScores;
    private Button btnWatchVideos;  // NEW: Video button
    private Button btnAppInfo;      // NEW: App info button

    private ThemeManager themeManager;
    private ScoreManager scoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply current theme
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }

        // Initialize score manager
        try {
            scoreManager = new ScoreManager(this);
        } catch (Exception e) {
            scoreManager = null;
        }

        // Initialize UI components
        initializeViews();
        setupThemeSpinner();
        setupSwitches();
        setupButtons();
    }

    /**
     * Initialize view references
     */
    private void initializeViews() {
        spinnerTheme = findViewById(R.id.spinnerTheme);
        switchSound = findViewById(R.id.switchSound);
        switchVibration = findViewById(R.id.switchVibration);
        btnResetScores = findViewById(R.id.btnResetScores);

        // Try to find new buttons - they might not exist in current layout
        btnWatchVideos = findViewById(R.id.btnWatchVideos);
        btnAppInfo = findViewById(R.id.btnAppInfo);

        // If buttons don't exist in layout, create them programmatically
        if (btnWatchVideos == null || btnAppInfo == null) {
            createAdditionalButtons();
        }
    }

    /**
     * Create additional buttons programmatically if not in layout
     */
    private void createAdditionalButtons() {
        // Find the parent layout to add buttons to
        android.view.ViewGroup parent = (android.view.ViewGroup) btnResetScores.getParent();

        if (btnWatchVideos == null) {
            btnWatchVideos = new Button(this);
            btnWatchVideos.setText("📹 Watch Tutorial Videos");
            btnWatchVideos.setLayoutParams(btnResetScores.getLayoutParams());
            parent.addView(btnWatchVideos, parent.indexOfChild(btnResetScores));
        }

        if (btnAppInfo == null) {
            btnAppInfo = new Button(this);
            btnAppInfo.setText("ℹ️ App Information");
            btnAppInfo.setLayoutParams(btnResetScores.getLayoutParams());
            parent.addView(btnAppInfo, parent.indexOfChild(btnResetScores));
        }
    }

    /**
     * Set up theme spinner
     */
    private void setupThemeSpinner() {
        // Get theme names
        String[] themeValues = themeManager.getAvailableThemes();
        String[] themeNames = new String[themeValues.length];

        // Convert theme values to display names
        for (int i = 0; i < themeValues.length; i++) {
            themeNames[i] = getString(themeManager.getThemeDisplayNameResId(themeValues[i]));
        }

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, themeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);

        // Set current theme
        String currentTheme = themeManager.getCurrentTheme();
        for (int i = 0; i < themeValues.length; i++) {
            if (themeValues[i].equals(currentTheme)) {
                spinnerTheme.setSelection(i);
                break;
            }
        }

        // Set selection listener
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTheme = themeValues[position];
                if (!selectedTheme.equals(themeManager.getCurrentTheme())) {
                    themeManager.setTheme(selectedTheme);
                    recreate(); // Restart activity to apply theme
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    /**
     * Set up switch controls
     */
    private void setupSwitches() {
        // TODO: Implement sound and vibration settings with SharedPreferences
        // For now, just use hardcoded values
        switchSound.setChecked(true);
        switchVibration.setChecked(true);

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Save sound setting to SharedPreferences
            Toast.makeText(SettingsActivity.this,
                    "Sound " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Save vibration setting to SharedPreferences
            Toast.makeText(SettingsActivity.this,
                    "Vibration " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Set up all buttons including new ones
     */
    private void setupButtons() {
        // Reset scores button
        btnResetScores.setOnClickListener(v -> showResetScoresConfirmation());

        // NEW: Watch videos button
        if (btnWatchVideos != null) {
            btnWatchVideos.setOnClickListener(v -> showVideoSection());
        }

        // NEW: App info button
        if (btnAppInfo != null) {
            btnAppInfo.setOnClickListener(v -> showAppInformation());
        }
    }

    /**
     * Show reset scores confirmation
     */
    private void showResetScoresConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_scores)
                .setMessage(R.string.confirm_reset)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (scoreManager != null) {
                        int count = scoreManager.deleteAllScores();
                        Toast.makeText(SettingsActivity.this,
                                "Deleted " + count + " scores",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingsActivity.this,
                                "Score management not available",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    /**
     * NEW: Show video tutorial section
     */
    /**
     * NEW: Show video tutorial section with actual video playback
     */
    private void showVideoSection() {
        String[] videoOptions = {
                "🎮 Demo Video - Basic Gameplay (1:47)",
                "📊 Mathematical Concepts Tutorial",
                "⚡ Superpowers Guide",
                "🧠 Advanced Strategy Tips",
                "🌐 Real-World Applications"
        };

        String[] videoDescriptions = {
                "Watch the complete demo showing basic gameplay mechanics and features",
                "Learn probability theory, Bayesian inference, and information theory",
                "Master all six superpowers and their strategic applications",
                "Advanced techniques for optimal play and mathematical analysis",
                "See how Bughisweeper skills apply to real-world problems"
        };

        new AlertDialog.Builder(this)
                .setTitle("📹 Tutorial Videos")
                .setItems(videoOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Play the actual demo video
                            playDemoVideo();
                            break;
                        case 1:
                            showVideoComingSoon("Mathematical Concepts Tutorial");
                            break;
                        case 2:
                            showVideoComingSoon("Superpowers Guide");
                            break;
                        case 3:
                            showVideoComingSoon("Advanced Strategy Tips");
                            break;
                        case 4:
                            showVideoComingSoon("Real-World Applications");
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("ℹ️ About Videos", (dialog, which) -> showVideoInfo())
                .show();
    }

    /**
     * Play the demo video using VideoPlayerActivity
     */
    private void playDemoVideo() {
        try {
            Intent videoIntent = new Intent(this, VideoPlayerActivity.class);
            videoIntent.putExtra("video_name", "demo_video");
            videoIntent.putExtra("video_title", "🎮 Bughisweeper Demo");
            videoIntent.putExtra("video_description",
                    "Complete gameplay demonstration showing basic mechanics, " +
                            "mathematical analysis, and superpower usage. Duration: 1:47");

            startActivity(videoIntent);

            Toast.makeText(this, "▶️ Loading demo video...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            // Fallback if VideoPlayerActivity fails
            showVideoPlaybackError();
        }
    }

    /**
     * Show info about video tutorials
     */
    private void showVideoInfo() {
        new AlertDialog.Builder(this)
                .setTitle("📹 About Tutorial Videos")
                .setMessage(
                        "🎬 VIDEO TUTORIAL SYSTEM\n\n" +
                                "📱 FEATURES:\n" +
                                "• HD quality playback\n" +
                                "• Fullscreen mode\n" +
                                "• Media controls (play/pause/seek)\n" +
                                "• Replay functionality\n" +
                                "• Optimized for mobile viewing\n\n" +
                                "🎯 AVAILABLE CONTENT:\n" +
                                "✅ Demo Video - Ready to watch!\n" +
                                "🔄 Mathematical Tutorials - In production\n" +
                                "🔄 Superpowers Guide - In production\n" +
                                "🔄 Strategy Tips - In production\n" +
                                "🔄 Real-World Applications - In production\n\n" +
                                "📊 TECHNICAL SPECS:\n" +
                                "• Format: MP4 (H.264)\n" +
                                "• Resolution: 910×422\n" +
                                "• Duration: 1:47\n" +
                                "• Size: 1.8 MB\n\n" +
                                "🎓 EDUCATIONAL VALUE:\n" +
                                "These videos bridge theory and practice, showing " +
                                "how mathematical concepts apply to gameplay and real-world scenarios."
                )
                .setPositiveButton("📺 Watch Demo Now", (dialog, which) -> playDemoVideo())
                .setNegativeButton("Close", null)
                .show();
    }

    /**
     * Show coming soon message for other videos
     */
    private void showVideoComingSoon(String videoTitle) {
        new AlertDialog.Builder(this)
                .setTitle("🎬 " + videoTitle)
                .setMessage(
                        "This video is currently in production!\n\n" +
                                "📋 PLANNED CONTENT:\n" +
                                "• High-quality animations\n" +
                                "• Expert narration\n" +
                                "• Interactive examples\n" +
                                "• Real-world applications\n\n" +
                                "🔔 You'll be notified when it's ready!\n\n" +
                                "In the meantime, try the demo video or " +
                                "explore the interactive mathematical learning mode."
                )
                .setPositiveButton("📺 Watch Demo Instead", (dialog, which) -> playDemoVideo())
                .setNeutralButton("🎮 Try Interactive Learning", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(this, MathEducationActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Interactive learning temporarily unavailable", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Handle video playback errors
     */
    private void showVideoPlaybackError() {
        new AlertDialog.Builder(this)
                .setTitle("❌ Video Playback Error")
                .setMessage(
                        "Unable to play the demo video.\n\n" +
                                "🔧 TROUBLESHOOTING:\n" +
                                "• Ensure the video file is in res/raw/\n" +
                                "• Check device media player support\n" +
                                "• Verify VideoPlayerActivity is declared in manifest\n\n" +
                                "📱 DEVICE REQUIREMENTS:\n" +
                                "• Android 5.0+ (API 21)\n" +
                                "• H.264 codec support\n" +
                                "• Sufficient storage space\n\n" +
                                "Alternative: Try the interactive learning mode instead."
                )
                .setPositiveButton("🎓 Try Interactive Learning", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(this, MathEducationActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Interactive learning also unavailable", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /**
     * NEW: Show comprehensive app information
     */
    private void showAppInformation() {
        String appInfo = String.format(
                "🎮 BUGHISWEEPER\n" +
                        "Version: %s\n\n" +
                        "📋 APP FEATURES:\n" +
                        "• Educational minesweeper gameplay\n" +
                        "• Advanced mathematical analysis\n" +
                        "• Unique superpower system\n" +
                        "• Multiple visual themes\n" +
                        "• Comprehensive tutorial system\n" +
                        "• Real-world learning applications\n\n" +
                        "🧮 MATHEMATICAL CONCEPTS:\n" +
                        "• Probability Theory & Statistics\n" +
                        "• Bayesian Inference\n" +
                        "• Information Theory (Shannon Entropy)\n" +
                        "• Decision Theory & Optimization\n" +
                        "• Constraint Satisfaction Problems\n\n" +
                        "⚡ UNIQUE SUPERPOWERS:\n" +
                        "• 🧊 Freeze Time - Strategic planning\n" +
                        "• 🔍 X-Ray Vision - Safe cell revelation\n" +
                        "• 🌊 Sonar Pulse - Area analysis\n" +
                        "• ⚡ Lightning Strike - Optimal move finder\n" +
                        "• 🛡️ Shield Mode - Mine protection\n" +
                        "• 🎯 Smart Sweep - Automatic flagging\n\n" +
                        "🎨 VISUAL THEMES:\n" +
                        "• Classic - Traditional appearance\n" +
                        "• Dark - Modern dark interface\n" +
                        "• Forest - Nature-inspired colors\n" +
                        "• Ocean - Aquatic blue tones\n" +
                        "• Space - Cosmic purple theme\n\n" +
                        "🌐 REAL-WORLD APPLICATIONS:\n" +
                        "Skills learned apply to:\n" +
                        "• Medical diagnosis and treatment\n" +
                        "• Financial risk management\n" +
                        "• Engineering optimization\n" +
                        "• Data science and machine learning\n" +
                        "• Quality control and testing\n" +
                        "• Research and development\n\n" +
                        "🎓 EDUCATIONAL VALUE:\n" +
                        "This app bridges the gap between abstract mathematical concepts and practical problem-solving skills, making complex theories accessible through interactive gameplay.\n\n" +
                        "📞 SUPPORT & FEEDBACK:\n" +
                        "We value your input for continuous improvement!",
                getVersionName()
        );

        new AlertDialog.Builder(this)
                .setTitle("ℹ️ App Information")
                .setMessage(appInfo)
                .setPositiveButton("📹 Watch Videos", (dialog, which) -> showVideoSection())
                .setNeutralButton("🌟 Rate App", (dialog, which) -> {
                    Toast.makeText(this, "Thank you! Rating feature coming soon.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("✉️ Send Feedback", (dialog, which) -> showFeedbackOptions())
                .show();
    }

    /**
     * Show feedback and support options
     */
    private void showFeedbackOptions() {
        String[] feedbackOptions = {
                "🐛 Report a Bug",
                "💡 Suggest a Feature",
                "📚 Request Tutorial Topic",
                "🎮 Gameplay Feedback",
                "🧮 Mathematical Content Feedback",
                "⚡ Superpower Ideas",
                "🎨 Theme Suggestions"
        };

        new AlertDialog.Builder(this)
                .setTitle("📝 Feedback & Support")
                .setItems(feedbackOptions, (dialog, which) -> {
                    String feedbackType = feedbackOptions[which];
                    showFeedbackForm(feedbackType);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show feedback form for specific type
     */
    private void showFeedbackForm(String feedbackType) {
        new AlertDialog.Builder(this)
                .setTitle("📧 " + feedbackType)
                .setMessage(
                        "Thank you for wanting to provide feedback!\n\n" +
                                "Selected Category: " + feedbackType + "\n\n" +
                                "📧 FEEDBACK SUBMISSION:\n" +
                                "A feedback form will open where you can:\n" +
                                "• Describe your suggestions in detail\n" +
                                "• Attach screenshots if relevant\n" +
                                "• Specify your device and app version\n" +
                                "• Choose to be contacted for follow-up\n\n" +
                                "🔒 PRIVACY:\n" +
                                "Your feedback is confidential and helps improve the app for everyone.\n\n" +
                                "⚡ QUICK FEEDBACK:\n" +
                                "For immediate input, you can also use the in-app rating system."
                )
                .setPositiveButton("📝 Open Feedback Form", (dialog, which) -> {
                    // TODO: Implement actual feedback form
                    Toast.makeText(this, "Feedback form will open here. Thank you for your interest!", Toast.LENGTH_LONG).show();
                })
                .setNeutralButton("⭐ Quick Rating", (dialog, which) -> {
                    showQuickRating();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show quick rating dialog
     */
    private void showQuickRating() {
        String[] ratings = {"⭐", "⭐⭐", "⭐⭐⭐", "⭐⭐⭐⭐", "⭐⭐⭐⭐⭐"};
        String[] descriptions = {"Poor", "Fair", "Good", "Very Good", "Excellent"};

        new AlertDialog.Builder(this)
                .setTitle("⭐ Rate Your Experience")
                .setSingleChoiceItems(ratings, -1, (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Thank you for rating us " + ratings[which] + " (" + descriptions[which] + ")!", Toast.LENGTH_LONG).show();

                    if (which >= 3) { // 4 or 5 stars
                        Toast.makeText(this, "🎉 We're glad you're enjoying the app!", Toast.LENGTH_SHORT).show();
                    } else { // 1-3 stars
                        new AlertDialog.Builder(this)
                                .setTitle("💬 Help Us Improve")
                                .setMessage("We'd love to make the app better for you! Would you like to tell us what we can improve?")
                                .setPositiveButton("Yes, Give Feedback", (d, w) -> showFeedbackOptions())
                                .setNegativeButton("Maybe Later", null)
                                .show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show text-based help as alternative to videos
     */
    private void showTextBasedHelp() {
        String helpContent =
                "📖 QUICK HELP GUIDE\n\n" +
                        "🎯 OBJECTIVE:\n" +
                        "Clear the minefield without hitting any bugs using logic and mathematics.\n\n" +
                        "🎮 BASIC CONTROLS:\n" +
                        "• Tap to reveal cells\n" +
                        "• Long press to flag suspected bugs\n" +
                        "• Use pinch gestures to zoom\n" +
                        "• Toggle flag mode with flag button\n\n" +
                        "🔢 READING NUMBERS:\n" +
                        "Each number shows how many bugs are in the 8 adjacent cells.\n\n" +
                        "📊 MATHEMATICAL MODE:\n" +
                        "• Shows probability percentages on cells\n" +
                        "• Green = Safe, Red = Dangerous\n" +
                        "• Provides AI hints for optimal moves\n" +
                        "• Real-time Bayesian calculations\n\n" +
                        "⚡ SUPERPOWERS (if enabled):\n" +
                        "• 🧊 Freeze Time: Pause timer for planning\n" +
                        "• 🔍 X-Ray: Reveal 3 cells safely\n" +
                        "• 🌊 Sonar: Count mines in area\n" +
                        "• ⚡ Lightning: Auto-reveal safest cell\n" +
                        "• 🛡️ Shield: Survive one mine hit\n" +
                        "• 🎯 Smart Sweep: Auto-flag obvious mines\n\n" +
                        "🎓 LEARNING TIP:\n" +
                        "Start with mathematical mode to understand probability concepts, then try superpowers for advanced gameplay!";

        new AlertDialog.Builder(this)
                .setTitle("📚 Text Help Guide")
                .setMessage(helpContent)
                .setPositiveButton("Got it!", null)
                .setNeutralButton("📹 Still Want Videos?", (dialog, which) -> showVideoSection())
                .show();
    }

    /**
     * Get app version name
     */
    private String getVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button in action bar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme in case it was changed
        if (themeManager != null) {
            themeManager.applyTheme(this);
        }
    }
}