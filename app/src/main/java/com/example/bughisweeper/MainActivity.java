package com.example.bughisweeper;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Fixed MainActivity with working custom mode and proper game mode separation
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // UI COMPONENTS
    private Button btnNewGame;
    private Button btnSettings;
    private Button btnHighScores;
    private Button btnHelp;
    private Button btnMathAnalysis;
    private Button btnExit;
    private Button btnLogout;
    private TextView tvVersion;
    private TextView tvWelcomeUser;
    private TextView tvQuickStats;

    // MANAGERS
    private ThemeManager themeManager;
    private DifficultyManager difficultyManager;
    private ScoreManager scoreManager;
    private SharedPreferences authPrefs;

    // Animation objects
    private ObjectAnimator pulseAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity onCreate started");

        try {
            // Apply theme FIRST with error handling
            try {
                themeManager = ThemeManager.getInstance(this);
                themeManager.applyTheme(this);
                Log.d(TAG, "Theme applied successfully");
            } catch (Exception e) {
                Log.e(TAG, "Theme application failed", e);
                // Continue without theme
            }

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout set successfully");

            // Initialize managers with comprehensive error handling
            initializeManagers();

            // Initialize UI components
            initializeViews();
            setupListeners();
            updateUserInterface();

            // Set version text safely
            setVersionText();

            // Add entrance animation
            animateEntrance();

            Log.d(TAG, "MainActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Critical error in MainActivity onCreate", e);
            handleCriticalStartupError(e);
        }
    }

    private void initializeManagers() {
        try {
            difficultyManager = DifficultyManager.getInstance(this);
            Log.d(TAG, "DifficultyManager initialized");
        } catch (Exception e) {
            Log.e(TAG, "DifficultyManager initialization failed", e);
            difficultyManager = null;
        }

        try {
            scoreManager = new ScoreManager(this);
            Log.d(TAG, "ScoreManager initialized");
        } catch (Exception e) {
            Log.e(TAG, "ScoreManager initialization failed", e);
            scoreManager = null;
        }

        try {
            authPrefs = getSharedPreferences("bughisweeper_auth", MODE_PRIVATE);
            Log.d(TAG, "SharedPreferences initialized");
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences initialization failed", e);
            authPrefs = null;
        }
    }

    private void initializeViews() {
        try {
            btnNewGame = findViewById(R.id.btnNewGame);
            btnSettings = findViewById(R.id.btnSettings);
            btnHighScores = findViewById(R.id.btnHighScores);
            btnExit = findViewById(R.id.btnExit);
            tvVersion = findViewById(R.id.tvVersion);

            // Optional views
            btnHelp = findViewById(R.id.btnHelp);
            btnMathAnalysis = findViewById(R.id.btnMathAnalysis);
            btnLogout = findViewById(R.id.btnLogout);
            tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
            tvQuickStats = findViewById(R.id.tvQuickStats);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "View initialization failed", e);
            throw e; // This is critical - we need basic views
        }
    }

    private void setupListeners() {
        try {
            // NEW GAME BUTTON - Most important one
            if (btnNewGame != null) {
                btnNewGame.setOnClickListener(v -> {
                    Log.d(TAG, "New Game button clicked");
                    showGameModeSelection();
                });
            }

            // SETTINGS BUTTON
            if (btnSettings != null) {
                btnSettings.setOnClickListener(v -> {
                    Log.d(TAG, "Settings button clicked");
                    openSettingsSafely();
                });
            }

            // HIGH SCORES BUTTON
            if (btnHighScores != null) {
                btnHighScores.setOnClickListener(v -> {
                    Log.d(TAG, "High Scores button clicked");
                    openHighScoresSafely();
                });
            }

            // HELP BUTTON
            if (btnHelp != null) {
                btnHelp.setOnClickListener(v -> {
                    Log.d(TAG, "Help button clicked");
                    openHelpSafely();
                });
            }

            // MATHEMATICAL ANALYSIS BUTTON
            if (btnMathAnalysis != null) {
                btnMathAnalysis.setOnClickListener(v -> {
                    Log.d(TAG, "Math Analysis button clicked");
                    showMathematicalFeatures();
                });
            }

            // EXIT BUTTON
            if (btnExit != null) {
                btnExit.setOnClickListener(v -> {
                    Log.d(TAG, "Exit button clicked");
                    showExitConfirmation();
                });
            }

            // LOGOUT BUTTON
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    Log.d(TAG, "Logout button clicked");
                    showLogoutConfirmation();
                });
            }

            Log.d(TAG, "Listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Listener setup failed", e);
            // Continue - some buttons might still work
        }
    }

    /**
     * Simple but working game mode selection
     */
    private void showGameModeSelection() {
        try {
            Log.d(TAG, "Showing game mode selection");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("🎮 Select Game Mode");

            String[] gameModes = new String[]{
                    "🎯 Classic Mode - Traditional minesweeper",
                    "⚡ Superpower Mode - Special abilities enabled",
                    "🧮 Math Mode - Probability analysis & AI hints",
                    "⏰ Challenge Mode - Race against the clock",
                    "🎓 Learning Mode - Math + Superpowers",
                    "⚙️ Custom Mode - Choose your own settings"
            };

            builder.setItems(gameModes, (dialog, which) -> {
                Log.d(TAG, "Game mode selected: " + which);
                handleGameModeSelection(which);
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();

        } catch (Exception e) {
            Log.e(TAG, "Game mode selection failed", e);
            // Fallback to simple game
            startGameSafely("easy", false, false, false);
        }
    }

    private void handleGameModeSelection(int which) {
        try {
            switch (which) {
                case 0: // Classic Mode
                    Log.d(TAG, "Starting Classic Mode");
                    showDifficultySelection(false, false, false);
                    break;
                case 1: // Superpower Mode
                    Log.d(TAG, "Starting Superpower Mode");
                    showDifficultySelection(true, false, false);
                    break;
                case 2: // Math Mode
                    Log.d(TAG, "Starting Math Mode");
                    showDifficultySelection(false, true, false);
                    break;
                case 3: // Challenge Mode (TIMED)
                    Log.d(TAG, "Starting Challenge Mode");
                    showDifficultySelection(false, false, true);
                    break;
                case 4: // Learning Mode
                    Log.d(TAG, "Starting Learning Mode");
                    showDifficultySelection(true, true, false);
                    break;
                case 5: // Custom Mode
                    Log.d(TAG, "Starting Custom Mode");
                    showCustomModeDialog();
                    break;
                default:
                    Log.w(TAG, "Unknown game mode: " + which);
                    showDifficultySelection(false, false, false);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Game mode handling failed", e);
            startGameSafely("easy", false, false, false);
        }
    }

    private void showCustomModeDialog() {
        try {
            Log.d(TAG, "Showing custom mode dialog");

            // Create simple input dialog
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            TextView title = new TextView(this);
            title.setText("⚙️ Custom Game Settings");
            title.setTextSize(18);
            title.setPadding(0, 0, 0, 20);
            layout.addView(title);

            // Rows input
            TextView rowsLabel = new TextView(this);
            rowsLabel.setText("Rows (4-50):");
            layout.addView(rowsLabel);

            EditText etRows = new EditText(this);
            etRows.setText("8");
            etRows.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(etRows);

            // Columns input
            TextView colsLabel = new TextView(this);
            colsLabel.setText("Columns (4-50):");
            colsLabel.setPadding(0, 16, 0, 0);
            layout.addView(colsLabel);

            EditText etCols = new EditText(this);
            etCols.setText("8");
            etCols.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(etCols);

            // Mines input
            TextView minesLabel = new TextView(this);
            minesLabel.setText("Mines:");
            minesLabel.setPadding(0, 16, 0, 0);
            layout.addView(minesLabel);

            EditText etMines = new EditText(this);
            etMines.setText("10");
            etMines.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(etMines);

            // Mode selection
            TextView modeLabel = new TextView(this);
            modeLabel.setText("\nGame Features:");
            modeLabel.setTextSize(16);
            layout.addView(modeLabel);

            String[] modeOptions = {
                    "🎯 Basic Custom",
                    "⚡ Custom + Superpowers",
                    "🧮 Custom + Math Analysis",
                    "🎓 Custom + Both Features"
            };

            final int[] selectedFeature = {0};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(layout);

            builder.setNeutralButton("Features", (dialog, which) -> {
                new AlertDialog.Builder(this)
                        .setTitle("Select Features")
                        .setSingleChoiceItems(modeOptions, selectedFeature[0], (d, w) -> {
                            selectedFeature[0] = w;
                            d.dismiss();
                        })
                        .show();
            });

            builder.setPositiveButton("Start Game", (dialog, which) -> {
                try {
                    int rows = Integer.parseInt(etRows.getText().toString());
                    int cols = Integer.parseInt(etCols.getText().toString());
                    int mines = Integer.parseInt(etMines.getText().toString());

                    // Validate inputs
                    if (rows < 4 || rows > 50) {
                        Toast.makeText(this, "Rows must be between 4-50", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (cols < 4 || cols > 50) {
                        Toast.makeText(this, "Columns must be between 4-50", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mines < 1 || mines >= (rows * cols)) {
                        Toast.makeText(this, "Invalid mine count", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean superpowers = selectedFeature[0] == 1 || selectedFeature[0] == 3;
                    boolean mathMode = selectedFeature[0] == 2 || selectedFeature[0] == 3;

                    startCustomGame(rows, cols, mines, superpowers, mathMode);

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();

        } catch (Exception e) {
            Log.e(TAG, "Custom mode dialog failed", e);
            Toast.makeText(this, "Custom mode temporarily unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCustomGame(int rows, int cols, int mines, boolean superpowers, boolean mathMode) {
        try {
            Log.d(TAG, String.format("Starting custom game: %dx%d with %d mines", rows, cols, mines));

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("difficulty", "custom");
            intent.putExtra("custom_rows", rows);
            intent.putExtra("custom_cols", cols);
            intent.putExtra("custom_bugs", mines);
            intent.putExtra("superpowers_enabled", superpowers);
            intent.putExtra("math_mode_enabled", mathMode);
            intent.putExtra("challenge_mode", false);

            startActivity(intent);

            String features = "";
            if (superpowers && mathMode) features = " with superpowers and math analysis";
            else if (superpowers) features = " with superpowers";
            else if (mathMode) features = " with math analysis";

            Toast.makeText(this, String.format("Custom game: %dx%d, %d mines%s",
                    rows, cols, mines, features), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Custom game start failed", e);
            Toast.makeText(this, "Failed to start custom game", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDifficultySelection(boolean superpowers, boolean mathMode, boolean challengeMode) {
        try {
            Log.d(TAG, "Showing difficulty selection");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            String modeDescription = "";
            if (superpowers && mathMode) modeDescription = " (Learning Mode)";
            else if (superpowers) modeDescription = " (Superpower Mode)";
            else if (mathMode) modeDescription = " (Math Mode)";
            else if (challengeMode) modeDescription = " (Challenge Mode - TIMED)";

            builder.setTitle("🎯 Select Difficulty" + modeDescription);

            String[] difficulties;
            if (challengeMode) {
                difficulties = new String[]{
                        "🟢 Easy (8×8, 10 mines) - 5 minute limit",
                        "🟡 Medium (16×16, 40 mines) - 10 minute limit",
                        "🔴 Hard (24×24, 99 mines) - 15 minute limit",
                        "🌟 Expert (30×30, 150 mines) - 20 minute limit"
                };
            } else {
                difficulties = new String[]{
                        "🟢 Easy (8×8, 10 mines)",
                        "🟡 Medium (16×16, 40 mines)",
                        "🔴 Hard (24×24, 99 mines)",
                        "🌟 Expert (30×30, 150 mines)"
                };
            }

            builder.setItems(difficulties, (dialog, which) -> {
                String difficulty;
                switch (which) {
                    case 0: difficulty = "easy"; break;
                    case 1: difficulty = "medium"; break;
                    case 2: difficulty = "hard"; break;
                    case 3: difficulty = "expert"; break;
                    default: difficulty = "easy"; break;
                }

                Log.d(TAG, "Difficulty selected: " + difficulty);
                startGameSafely(difficulty, superpowers, mathMode, challengeMode);
            });

            builder.setNegativeButton("Back", (dialog, which) -> {
                Log.d(TAG, "Back to game mode selection");
                showGameModeSelection();
            });

            builder.show();

        } catch (Exception e) {
            Log.e(TAG, "Difficulty selection failed", e);
            startGameSafely("easy", superpowers, mathMode, challengeMode);
        }
    }

    /**
     * Safe game start method
     */
    private void startGameSafely(String difficulty, boolean superpowers, boolean mathMode, boolean challengeMode) {
        try {
            Log.d(TAG, String.format("Starting game: %s, superpowers=%b, math=%b, challenge=%b",
                    difficulty, superpowers, mathMode, challengeMode));

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("difficulty", difficulty != null ? difficulty : "easy");
            intent.putExtra("superpowers_enabled", superpowers);
            intent.putExtra("math_mode_enabled", mathMode);
            intent.putExtra("challenge_mode", challengeMode);

            startActivity(intent);
            Log.d(TAG, "Game activity started successfully");

            // Show what mode is starting
            String modeDesc = "Classic";
            if (superpowers && mathMode) modeDesc = "Learning Mode";
            else if (superpowers) modeDesc = "Superpower Mode";
            else if (mathMode) modeDesc = "Math Mode";
            else if (challengeMode) modeDesc = "Challenge Mode (TIMED)";

            Toast.makeText(this, "Starting " + modeDesc + " - " + difficulty, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Game start failed", e);
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Game Start Error")
                    .setMessage("Unable to start the game. Please try again.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void openSettingsSafely() {
        try {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Settings activity failed", e);
            showSimpleMessage("Settings", "Settings feature coming soon!");
        }
    }

    private void openHighScoresSafely() {
        try {
            Intent intent = new Intent(this, HighScoresActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "High scores activity failed", e);
            showSimpleMessage("High Scores", "High scores feature coming soon!");
        }
    }

    private void openHelpSafely() {
        try {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Help activity failed", e);
            showHelpDialog();
        }
    }

    private void showMathematicalFeatures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📐 Mathematical Features");
        builder.setMessage(
                "Explore mathematical concepts through gameplay:\n\n" +
                        "🔢 Probability Theory\n" +
                        "🧠 Bayesian Inference\n" +
                        "📊 Information Theory\n" +
                        "🎯 Decision Making\n\n" +
                        "Real-world applications in science, finance, and technology!"
        );
        builder.setPositiveButton("🎮 Try Math Mode", (dialog, id) ->
                showDifficultySelection(false, true, false));
        builder.setNeutralButton("🎓 Learning Mode", (dialog, id) ->
                showDifficultySelection(true, true, false));
        builder.setNegativeButton("Back", null);
        builder.show();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎮 Help & Tutorial")
                .setMessage(
                        "🎯 OBJECTIVE: Clear mines using logic\n\n" +
                                "🎮 CONTROLS:\n" +
                                "• Tap to reveal cells\n" +
                                "• Long press to flag mines\n\n" +
                                "⚡ SUPERPOWERS:\n" +
                                "• 🧊 Freeze Time\n" +
                                "• 🔍 X-Ray Vision\n" +
                                "• ⚡ Lightning Strike\n" +
                                "• 🛡️ Shield Mode\n\n" +
                                "🧮 MATH MODE:\n" +
                                "Shows probability calculations and optimal moves"
                )
                .setPositiveButton("Got it!", null)
                .show();
    }

    private void updateUserInterface() {
        try {
            String currentUser = "Player";
            if (authPrefs != null) {
                currentUser = authPrefs.getString("current_user", "Player");
            }

            if (tvWelcomeUser != null) {
                tvWelcomeUser.setText("Welcome back, " + currentUser + "!");
            }

            if (tvQuickStats != null) {
                tvQuickStats.setText("📊 Ready to play! Select 'New Game' to begin.");
            }

            startNewGameButtonAnimation();
        } catch (Exception e) {
            Log.e(TAG, "UI update failed", e);
        }
    }

    private void setVersionText() {
        try {
            if (tvVersion != null) {
                tvVersion.setText(String.format("v%s", getVersionName()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Version text setting failed", e);
        }
    }

    private void startNewGameButtonAnimation() {
        try {
            if (btnNewGame == null) return;

            pulseAnimator = ObjectAnimator.ofFloat(btnNewGame, "scaleX", 1f, 1.05f, 1f);
            pulseAnimator.setDuration(2000);
            pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            ObjectAnimator pulseY = ObjectAnimator.ofFloat(btnNewGame, "scaleY", 1f, 1.05f, 1f);
            pulseY.setDuration(2000);
            pulseY.setRepeatCount(ObjectAnimator.INFINITE);
            pulseY.setInterpolator(new AccelerateDecelerateInterpolator());

            pulseAnimator.start();
            pulseY.start();
        } catch (Exception e) {
            Log.e(TAG, "Button animation failed", e);
        }
    }

    private void animateEntrance() {
        try {
            animateButtonEntrance(btnNewGame, 0);
            animateButtonEntrance(btnSettings, 100);
            animateButtonEntrance(btnHighScores, 200);
            if (btnHelp != null) animateButtonEntrance(btnHelp, 300);
            if (btnMathAnalysis != null) animateButtonEntrance(btnMathAnalysis, 400);
            if (btnLogout != null) animateButtonEntrance(btnLogout, 500);
            animateButtonEntrance(btnExit, 600);
        } catch (Exception e) {
            Log.e(TAG, "Entrance animation failed", e);
        }
    }

    private void animateButtonEntrance(View view, long delay) {
        try {
            if (view == null) return;

            view.setAlpha(0f);
            view.setTranslationY(50f);

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(delay)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        } catch (Exception e) {
            Log.e(TAG, "Button entrance animation failed", e);
            if (view != null) {
                view.setAlpha(1f);
                view.setTranslationY(0f);
            }
        }
    }

    private void showSimpleMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("🔐 Logout")
                .setMessage("Logout and return to login screen?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        if (authPrefs != null) {
                            authPrefs.edit()
                                    .putBoolean("is_logged_in", false)
                                    .remove("current_user")
                                    .apply();
                        }

                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("👋 Exit Game")
                .setMessage("Thanks for playing Bughisweeper!")
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setNegativeButton("Stay", null)
                .show();
    }

    private void handleCriticalStartupError(Exception e) {
        Log.e(TAG, "Critical startup error", e);

        try {
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Startup Error")
                    .setMessage("The app encountered an error during startup. Some features may be limited.")
                    .setPositiveButton("Continue", null)
                    .show();
        } catch (Exception e2) {
            Toast.makeText(this, "App startup error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Menu creation failed", e);
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int id = item.getItemId();

            if (id == R.id.action_about) {
                showAboutDialog();
                return true;
            } else if (id == R.id.action_backup) {
                showSimpleMessage("Backup", "Backup functionality coming soon!");
                return true;
            } else if (id == R.id.action_math_learning) {
                showMathematicalFeatures();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Menu item selection failed", e);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About Bughisweeper")
                .setMessage(
                        "🎮 Bughisweeper v" + getVersionName() + "\n\n" +
                                "Educational minesweeper with:\n" +
                                "• Mathematical analysis\n" +
                                "• Unique superpowers\n" +
                                "• Multiple themes\n" +
                                "• Custom board sizes\n" +
                                "• Challenge modes\n" +
                                "• Real-world applications\n\n" +
                                "Learn while you play!"
                )
                .setPositiveButton("Cool!", null)
                .show();
    }

    private String getVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (themeManager != null) {
                themeManager.applyTheme(this);
            }
            updateUserInterface();
        } catch (Exception e) {
            Log.e(TAG, "onResume failed", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (pulseAnimator != null) {
                pulseAnimator.cancel();
            }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy cleanup failed", e);
        }
    }
}