        package com.example.bughisweeper;

        import android.animation.ObjectAnimator;
        import android.animation.ValueAnimator;
        import android.content.Intent;
        import android.graphics.Color;
        import android.graphics.drawable.GradientDrawable;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.SystemClock;
        import android.view.Gravity;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.GridLayout;
        import android.widget.LinearLayout;
        import android.widget.ProgressBar;
        import android.widget.ScrollView;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.widget.SeekBar;
        import android.widget.EditText;

        import androidx.appcompat.app.AlertDialog;
        import androidx.appcompat.app.AppCompatActivity;

        import java.util.HashMap;
        import java.util.Locale;
        import java.util.Map;

/**
 * Enhanced GameActivity with beautiful superpowers UI and proper visibility
 */
public class GameActivity extends AppCompatActivity {

    // Game components - all optional
    private BughisBoard board;
    private MathAnalyzer mathAnalyzer;
    private SuperpowerManager superpowerManager;
    private ThemeManager themeManager;

    // UI Components - all with null safety
    private TextView tvTime;
    private TextView tvBugsLeft;
    private Button btnFlag;
    private BoardView boardView;
    private GridLayout gameGrid;
    private Button[][] cellButtons;

    // Feature panels - all optional
    private LinearLayout llMathControls;
    private ScrollView scrollSuperpowerControls; // Changed to ScrollView
    private LinearLayout llSuperpowerContainer; // Container inside ScrollView
    private TextView tvProbabilityInfo;
    private TextView tvMathInsights;
    private Button btnHint;
    private Button btnReset;
    private Button btnMathView;

    // Superpower UI components
    private Map<SuperpowerManager.SuperpowerType, Button> superpowerButtons;
    private Map<SuperpowerManager.SuperpowerType, TextView> cooldownTexts;
    private Map<SuperpowerManager.SuperpowerType, ProgressBar> cooldownBars;
    private Handler superpowerUpdateHandler;
    private Runnable superpowerUpdateRunnable;

    // Game settings with safe defaults
    private int rows = 8;
    private int cols = 8;
    private int totalBugs = 10;
    private boolean flagMode = false;
    private boolean mathMode = false;
    private boolean superpowersEnabled = false;
    private boolean challengeMode = false;

    // Timer - improved for challenge mode
    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime;
    private long pausedTime = 0;
    private boolean gameActive;
    private boolean gamePaused = false;
    private long timeLimit = 0; // For challenge mode (0 = no limit)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            // Apply theme first - with error handling
            try {
                themeManager = ThemeManager.getInstance(this);
                themeManager.applyTheme(this);
            } catch (Exception e) {
                // Theme failed - continue anyway
            }

            super.onCreate(savedInstanceState);

            // Try to set content view - if it fails, we'll create a minimal layout
            try {
                setContentView(R.layout.activity_game);
            } catch (Exception e) {
                createEnhancedGameLayout();
            }

            // Get settings from intent with COMPREHENSIVE safety
            extractIntentSettings();

            // Setup difficulty (including custom)
            setupDifficulty();

            // Initialize everything with extensive error handling
            initializeViews();
            initializeGame();
            setupTimer();
            setupSuperpowerUpdateLoop();

            // Set up action bar safely
            try {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("🎮 Bughisweeper");
                }
            } catch (Exception e) {
                // Action bar setup failed - not critical
            }

            // Show mode status
            showGameModeStatus();

        } catch (Exception e) {
            // Critical failure - show error and return to main menu
            handleCriticalError(e);
        }
    }

    private void extractIntentSettings() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                String difficulty = intent.getStringExtra("difficulty");
                if (difficulty == null) difficulty = "easy";

                // Set difficulty from intent
                setDifficultyFromString(difficulty);

                mathMode = intent.getBooleanExtra("math_mode_enabled", false);
                superpowersEnabled = intent.getBooleanExtra("superpowers_enabled", false);
                challengeMode = intent.getBooleanExtra("challenge_mode", false);

                // For challenge mode, set time limit
                if (challengeMode) {
                    switch (difficulty.toLowerCase()) {
                        case "easy": timeLimit = 300000; break; // 5 minutes
                        case "medium": timeLimit = 600000; break; // 10 minutes
                        case "hard": timeLimit = 900000; break; // 15 minutes
                        case "expert": timeLimit = 1200000; break; // 20 minutes
                        default: timeLimit = 300000; break;
                    }
                }

                // Custom mode settings
                if ("custom".equals(difficulty)) {
                    rows = intent.getIntExtra("custom_rows", 8);
                    cols = intent.getIntExtra("custom_cols", 8);
                    totalBugs = intent.getIntExtra("custom_bugs", 10);
                }
            }
        } catch (Exception e) {
            // Intent reading failed - use defaults
            rows = 8; cols = 8; totalBugs = 10;
            mathMode = false;
            superpowersEnabled = false;
            challengeMode = false;
            timeLimit = 0;
        }
    }

    private void setDifficultyFromString(String difficulty) {
        try {
            switch (difficulty.toLowerCase()) {
                case "easy":
                    rows = 8; cols = 8; totalBugs = 10;
                    break;
                case "medium":
                    rows = 16; cols = 16; totalBugs = 40;
                    break;
                case "hard":
                    rows = 24; cols = 24; totalBugs = 99;
                    break;
                case "expert":
                    rows = 30; cols = 30; totalBugs = 150;
                    break;
                case "custom":
                    // Custom values will be set from intent extras
                    break;
                default:
                    rows = 8; cols = 8; totalBugs = 10;
            }
        } catch (Exception e) {
            // Difficulty parsing failed - use easy
            rows = 8; cols = 8; totalBugs = 10;
        }
    }

    private void setupDifficulty() {
        // Additional validation
        if (rows < 4) rows = 4;
        if (rows > 50) rows = 50;
        if (cols < 4) cols = 4;
        if (cols > 50) cols = 50;
        if (totalBugs < 1) totalBugs = 1;
        if (totalBugs >= (rows * cols)) totalBugs = (rows * cols) / 4;
    }

    /**
     * Create enhanced game layout with beautiful superpowers UI
     */
    private void createEnhancedGameLayout() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(8, 8, 8, 8);
        mainLayout.setBackgroundColor(0xFFF5F5F5);

        // Header with better styling
        LinearLayout headerLayout = createGameHeader();

        // Game area container with proper proportions
        LinearLayout gameAreaLayout = new LinearLayout(this);
        gameAreaLayout.setOrientation(LinearLayout.HORIZONTAL);
        gameAreaLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        // Create beautiful superpowers panel (left side)
        scrollSuperpowerControls = createSimpleSuperpowersPanel();

        // Game grid (center, larger)
        gameGrid = new GridLayout(this);
        gameGrid.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        gameGrid.setPadding(16, 16, 16, 16);
        gameGrid.setBackgroundColor(0xFFFFFFFF);

        // Math controls panel (right side)
        llMathControls = createMathPanel();

        gameAreaLayout.addView(scrollSuperpowerControls);
        gameAreaLayout.addView(gameGrid);
        gameAreaLayout.addView(llMathControls);

        // Info panel at bottom
        LinearLayout infoLayout = createInfoPanel();

        mainLayout.addView(headerLayout);
        mainLayout.addView(gameAreaLayout);
        mainLayout.addView(infoLayout);

        setContentView(mainLayout);
    }

    private LinearLayout createGameHeader() {
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(16, 16, 16, 16);

        // Create gradient background
        GradientDrawable headerBg = new GradientDrawable();
        headerBg.setColors(new int[]{0xFF1976D2, 0xFF1565C0});
        headerBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        headerBg.setCornerRadius(12);
        headerLayout.setBackground(headerBg);

        tvTime = new TextView(this);
        tvTime.setText("Time: 0:00");
        tvTime.setTextColor(0xFFFFFFFF);
        tvTime.setTextSize(18);
        tvTime.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTime.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        tvBugsLeft = new TextView(this);
        tvBugsLeft.setText("Bugs: 10");
        tvBugsLeft.setTextColor(0xFFFFFFFF);
        tvBugsLeft.setTextSize(18);
        tvBugsLeft.setTypeface(null, android.graphics.Typeface.BOLD);
        tvBugsLeft.setGravity(Gravity.CENTER);
        tvBugsLeft.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        btnFlag = new Button(this);
        btnFlag.setText("🚩");
        btnFlag.setTextSize(24);
        btnFlag.setLayoutParams(new LinearLayout.LayoutParams(100, 80));

        // Style flag button
        GradientDrawable flagBg = new GradientDrawable();
        flagBg.setColor(0xFF2196F3);
        flagBg.setCornerRadius(8);
        btnFlag.setBackground(flagBg);
        btnFlag.setTextColor(0xFFFFFFFF);

        headerLayout.addView(tvTime);
        headerLayout.addView(tvBugsLeft);
        headerLayout.addView(btnFlag);

        return headerLayout;
    }

    private ScrollView createSimpleSuperpowersPanel() {
        try {
            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(180, LinearLayout.LayoutParams.MATCH_PARENT));
            scrollView.setPadding(8, 8, 8, 8);
            scrollView.setBackgroundColor(0xFFE3F2FD);
            scrollView.setVisibility(View.GONE); // Initially hidden

            llSuperpowerContainer = new LinearLayout(this);
            llSuperpowerContainer.setOrientation(LinearLayout.VERTICAL);
            llSuperpowerContainer.setPadding(8, 8, 8, 8);

            scrollView.addView(llSuperpowerContainer);
            return scrollView;
        } catch (Exception e) {
            // Even simple panel creation failed
            Toast.makeText(this, "Superpowers panel creation failed, using menu access", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private LinearLayout createMathPanel() {
        LinearLayout mathPanel = new LinearLayout(this);
        mathPanel.setOrientation(LinearLayout.VERTICAL);
        mathPanel.setPadding(8, 8, 8, 8);
        mathPanel.setLayoutParams(new LinearLayout.LayoutParams(160, LinearLayout.LayoutParams.MATCH_PARENT));
        mathPanel.setVisibility(View.GONE);

        // Create gradient background
        GradientDrawable mathBg = new GradientDrawable();
        mathBg.setColors(new int[]{0xFFE8F5E9, 0xFFC8E6C9});
        mathBg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        mathBg.setCornerRadius(12);
        mathBg.setStroke(2, 0xFF4CAF50);
        mathPanel.setBackground(mathBg);

        return mathPanel;
    }

    private LinearLayout createInfoPanel() {
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setPadding(8, 8, 8, 8);

        GradientDrawable infoBg = new GradientDrawable();
        infoBg.setColor(0xFFFAFAFA);
        infoBg.setCornerRadius(8);
        infoBg.setStroke(1, 0xFFE0E0E0);
        infoLayout.setBackground(infoBg);

        tvProbabilityInfo = new TextView(this);
        tvProbabilityInfo.setTextSize(12);
        tvProbabilityInfo.setVisibility(View.GONE);

        tvMathInsights = new TextView(this);
        tvMathInsights.setTextSize(12);
        tvMathInsights.setVisibility(View.GONE);

        infoLayout.addView(tvProbabilityInfo);
        infoLayout.addView(tvMathInsights);

        return infoLayout;
    }

    private void initializeViews() {
        try {
            // Try to find views safely (if using XML layout)
            if (tvTime == null) tvTime = findViewById(R.id.tvTime);
            if (tvBugsLeft == null) tvBugsLeft = findViewById(R.id.tvBugsLeft);
            if (btnFlag == null) btnFlag = findViewById(R.id.btnFlag);

            // Try BoardView first, fallback to GridLayout
            if (boardView == null) boardView = findViewById(R.id.boardView);
            if (gameGrid == null) gameGrid = findViewById(R.id.gameGrid);

            // Feature panels - try to find existing or use created ones
            if (llMathControls == null) llMathControls = findViewById(R.id.llMathControls);
            if (scrollSuperpowerControls == null) {
                // Try to find existing superpower container
                View existingSuperpowers = findViewById(R.id.fragmentContainerPowerups);
                if (existingSuperpowers instanceof ScrollView) {
                    scrollSuperpowerControls = (ScrollView) existingSuperpowers;
                    // Find the container inside
                    if (scrollSuperpowerControls.getChildCount() > 0) {
                        View child = scrollSuperpowerControls.getChildAt(0);
                        if (child instanceof LinearLayout) {
                            llSuperpowerContainer = (LinearLayout) child;
                        }
                    }
                }
            }

            // Info displays
            if (tvProbabilityInfo == null) tvProbabilityInfo = findViewById(R.id.tvProbabilityInfo);
            if (tvMathInsights == null) tvMathInsights = findViewById(R.id.tvMathInsights);

            setupButtonListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Some UI components unavailable, using simplified interface", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        try {
            // Flag button
            if (btnFlag != null) {
                btnFlag.setOnClickListener(v -> toggleFlagMode());
            }
        } catch (Exception e) {
            // Button listener setup failed - not critical
        }
    }

    private void initializeGame() {
        try {
            // Initialize game board - this is critical
            board = new BughisBoard(rows, cols, totalBugs);

            // Try to initialize mathematical analyzer ONLY if needed
            if (mathMode || superpowersEnabled) {
                try {
                    mathAnalyzer = new MathAnalyzer(this);
                    mathAnalyzer.initializeGame(board);
                } catch (Exception e) {
                    mathAnalyzer = null;
                    Toast.makeText(this, "Mathematical analysis disabled", Toast.LENGTH_SHORT).show();
                }
            }

            // Initialize superpower manager ONLY if enabled and safe
            if (superpowersEnabled) {
                try {
                    initializeSuperpowers();
                } catch (Exception e) {
                    superpowersEnabled = false;
                    Toast.makeText(this, "Superpowers disabled due to error", Toast.LENGTH_SHORT).show();
                }
            }

            // Set up board display with fallback
            try {
                setupBoardDisplay();
            } catch (Exception e) {
                // Even fallback failed - try emergency
                createImprovedGrid();
            }

            // Show appropriate controls
            showGameControls();

            // Update displays
            updateGameInfo();
            updateMathematicalInfo();

            gameActive = true;
            startTime = SystemClock.elapsedRealtime();

        } catch (Exception e) {
            handleGameInitializationError(e);
        }
    }

    private void initializeSuperpowers() throws Exception {
        try {
            // Only try to create superpowers if we have a board
            if (board == null) throw new Exception("No game board available");

            // Try to use BoardView if available, otherwise null
            if (boardView != null) {
                try {
                    superpowerManager = new SuperpowerManager(this, board, boardView);
                } catch (Exception e) {
                    // BoardView failed, try without it
                    superpowerManager = new SuperpowerManager(this, board, null);
                }
            } else {
                // No BoardView, create without it
                superpowerManager = new SuperpowerManager(this, board, null);
            }

            // Try to connect math analyzer if available
            if (mathAnalyzer != null && superpowerManager != null) {
                try {
                    superpowerManager.updateProbabilityAnalysis();
                } catch (Exception e) {
                    // Math connection failed - not critical
                }
            }

            // Create simple superpower controls
            createSimpleSuperpowerButtons();

        } catch (Exception e) {
            superpowerManager = null;
            throw e; // Re-throw to disable superpowers
        }
    }

    private void createSimpleSuperpowerButtons() {
        if (llSuperpowerContainer == null) {
            // Create a simple fallback using Toast messages
            Toast.makeText(this, "⚡ Superpowers ready! Use options menu to activate", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Clear any existing content except title (if there is one)
            llSuperpowerContainer.removeAllViews();

            // Create simple title
            TextView title = new TextView(this);
            title.setText("⚡ SUPERPOWERS");
            title.setTextSize(16);
            title.setTextColor(0xFF1976D2);
            title.setGravity(Gravity.CENTER);
            title.setPadding(8, 8, 8, 16);
            llSuperpowerContainer.addView(title);

            // Initialize collections if null
            if (superpowerButtons == null) superpowerButtons = new HashMap<>();
            if (cooldownTexts == null) cooldownTexts = new HashMap<>();
            if (cooldownBars == null) cooldownBars = new HashMap<>();

            // Create simple buttons for each superpower
            createSimpleSuperpowerButton("🧊 Freeze", SuperpowerManager.SuperpowerType.FREEZE, 0xFF00BCD4);
            createSimpleSuperpowerButton("🔍 X-Ray", SuperpowerManager.SuperpowerType.XRAY, 0xFF4CAF50);
            createSimpleSuperpowerButton("🌊 Sonar", SuperpowerManager.SuperpowerType.SONAR, 0xFF03A9F4);
            createSimpleSuperpowerButton("⚡ Lightning", SuperpowerManager.SuperpowerType.LIGHTNING, 0xFFFFC107);
            createSimpleSuperpowerButton("🛡️ Shield", SuperpowerManager.SuperpowerType.SHIELD, 0xFFFF9800);
            createSimpleSuperpowerButton("🎯 Smart", SuperpowerManager.SuperpowerType.SMART_SWEEP, 0xFF9C27B0);

            Toast.makeText(this, "⚡ Superpowers panel ready!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            // Even simple creation failed - use menu fallback
            Toast.makeText(this, "⚡ Superpowers available via options menu", Toast.LENGTH_LONG).show();
        }
    }

    private void createSimpleSuperpowerButton(String text, SuperpowerManager.SuperpowerType type, int color) {
        try {
            // Create container for this superpower
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(8, 4, 8, 4);

            // Create the main button
            Button button = new Button(this);
            button.setText(text);
            button.setTextSize(12);
            button.setTextColor(0xFFFFFFFF);
            button.setBackgroundColor(color);

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 50);
            button.setLayoutParams(buttonParams);

            // Set click listener
            button.setOnClickListener(v -> activateSuperpower(type));

            // Create simple cooldown text
            TextView cooldownText = new TextView(this);
            cooldownText.setText("Ready");
            cooldownText.setTextSize(10);
            cooldownText.setTextColor(color);
            cooldownText.setGravity(Gravity.CENTER);

            // Create simple progress bar
            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(100);
            progressBar.setProgress(100);
            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 8);
            progressBar.setLayoutParams(progressParams);

            // Add to container
            container.addView(button);
            container.addView(progressBar);
            container.addView(cooldownText);

            // Add to main container
            llSuperpowerContainer.addView(container);

            // Store references
            superpowerButtons.put(type, button);
            cooldownTexts.put(type, cooldownText);
            cooldownBars.put(type, progressBar);

        } catch (Exception e) {
            // Individual button creation failed - skip this one
            Toast.makeText(this, "Failed to create " + text + " button", Toast.LENGTH_SHORT).show();
        }
    }

    private void createSuperpowerCard(SuperpowerInfo info) {
        // Create card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(12, 8, 12, 8);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams cardMargin = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardMargin.setMargins(0, 0, 0, 8);
        card.setLayoutParams(cardMargin);

        // Create gradient background for card
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFFFFFFFF);
        cardBg.setCornerRadius(12);
        cardBg.setStroke(2, info.color);
        card.setBackground(cardBg);

        // Create main button
        Button button = new Button(this);
        button.setText(info.emoji + " " + info.name);
        button.setTextSize(12);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setTextColor(0xFFFFFFFF);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 60));

        // Style button with gradient
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColors(new int[]{info.color, info.color & 0xFFCCCCCC | 0x33000000});
        btnBg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        btnBg.setCornerRadius(8);
        button.setBackground(btnBg);

        // Set click listener
        button.setOnClickListener(v -> activateSuperpower(info.type));

        // Create description text
        TextView description = new TextView(this);
        description.setText(info.description);
        description.setTextSize(10);
        description.setTextColor(0xFF666666);
        description.setGravity(Gravity.CENTER);
        description.setPadding(4, 4, 4, 0);

        // Create progress bar for cooldown
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 8));
        progressBar.setMax(100);
        progressBar.setProgress(100);

        // Style progress bar
        progressBar.getProgressDrawable().setColorFilter(info.color, android.graphics.PorterDuff.Mode.SRC_IN);

        // Create cooldown text
        TextView cooldownText = new TextView(this);
        cooldownText.setText("Ready");
        cooldownText.setTextSize(9);
        cooldownText.setTextColor(info.color);
        cooldownText.setGravity(Gravity.CENTER);
        cooldownText.setTypeface(null, android.graphics.Typeface.BOLD);

        // Add components to card
        card.addView(button);
        card.addView(description);
        card.addView(progressBar);
        card.addView(cooldownText);

        // Store references
        superpowerButtons.put(info.type, button);
        cooldownBars.put(info.type, progressBar);
        cooldownTexts.put(info.type, cooldownText);

        // Add card to container
        llSuperpowerContainer.addView(card);
    }

    private void addStrategyTips() {
        // Add separator
        View separator = new View(this);
        separator.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
        separator.setBackgroundColor(0xFFE0E0E0);
        LinearLayout.LayoutParams sepMargin = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        sepMargin.setMargins(0, 12, 0, 12);
        separator.setLayoutParams(sepMargin);
        llSuperpowerContainer.addView(separator);

        // Add tips
        TextView tips = new TextView(this);
        tips.setText("💡 Pro Tips:\n" +
                "• Use X-Ray near revealed numbers\n" +
                "• Combine Sonar with probability analysis\n" +
                "• Save Lightning for endgame\n" +
                "• Activate Shield before risky moves");
        tips.setTextSize(9);
        tips.setTextColor(0xFF424242);
        tips.setPadding(8, 0, 8, 0);
        tips.setLineSpacing(2, 1.1f);

        LinearLayout.LayoutParams tipsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipsParams.setMargins(0, 0, 0, 8);
        tips.setLayoutParams(tipsParams);

        llSuperpowerContainer.addView(tips);
    }

    private void setupSuperpowerUpdateLoop() {
        if (!superpowersEnabled) return;

        try {
            superpowerUpdateHandler = new Handler();
            superpowerUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    updateSuperpowerUI();
                    if (superpowerUpdateHandler != null) {
                        superpowerUpdateHandler.postDelayed(this, 100); // Update every 100ms for smooth animation
                    }
                }
            };

            if (superpowerUpdateHandler != null) {
                superpowerUpdateHandler.post(superpowerUpdateRunnable);
            }
        } catch (Exception e) {
            // Superpower update loop failed - not critical
        }
    }

    private void updateSuperpowerUI() {
        if (superpowerManager == null || superpowerButtons == null) return;

        try {
            updateSuperpowerButton(SuperpowerManager.SuperpowerType.FREEZE,
                    superpowerManager.canUseFreeze(),
                    superpowerManager.getRemainingFreezeCooldown(), 60000);

            updateSuperpowerButton(SuperpowerManager.SuperpowerType.XRAY,
                    superpowerManager.canUseXRay(),
                    superpowerManager.getRemainingXRayCooldown(), 45000);

            updateSuperpowerButton(SuperpowerManager.SuperpowerType.SONAR,
                    superpowerManager.canUseSonar(),
                    superpowerManager.getRemainingSonarCooldown(), 30000);

            updateSuperpowerButton(SuperpowerManager.SuperpowerType.LIGHTNING,
                    superpowerManager.canUseLightning(),
                    superpowerManager.getRemainingLightningCooldown(), 90000);

            updateSuperpowerButton(SuperpowerManager.SuperpowerType.SMART_SWEEP,
                    superpowerManager.canUseSmartSweep(),
                    superpowerManager.getRemainingSmartSweepCooldown(), 20000);

            // Special handling for shield (one-time use)
            updateShieldButton();

        } catch (Exception e) {
            // UI update failed - not critical
        }
    }

    private void updateSuperpowerButton(SuperpowerManager.SuperpowerType type, boolean canUse,
                                        long remainingCooldown, long totalCooldown) {
        Button button = superpowerButtons.get(type);
        ProgressBar progressBar = cooldownBars.get(type);
        TextView cooldownText = cooldownTexts.get(type);

        if (button == null || progressBar == null || cooldownText == null) return;

        try {
            if (canUse) {
                // Ready state
                button.setEnabled(true);
                button.setAlpha(1.0f);
                progressBar.setProgress(100);
                cooldownText.setText("Ready");
                cooldownText.setTextColor(0xFF4CAF50);

                // Add subtle glow animation for ready buttons
                if (!button.hasOnClickListeners()) {
                    animateReadyButton(button);
                }
            } else {
                // Cooldown state
                button.setEnabled(false);
                button.setAlpha(0.6f);

                int progress = (int) (((totalCooldown - remainingCooldown) * 100) / totalCooldown);
                progressBar.setProgress(progress);

                String timeText = formatCooldownTime(remainingCooldown);
                cooldownText.setText(timeText);
                cooldownText.setTextColor(0xFFFF5722);
            }
        } catch (Exception e) {
            // Button update failed - not critical
        }
    }

    private void updateShieldButton() {
        Button button = superpowerButtons.get(SuperpowerManager.SuperpowerType.SHIELD);
        TextView cooldownText = cooldownTexts.get(SuperpowerManager.SuperpowerType.SHIELD);
        ProgressBar progressBar = cooldownBars.get(SuperpowerManager.SuperpowerType.SHIELD);

        if (button == null || cooldownText == null || progressBar == null) return;

        try {
            if (superpowerManager.canUseShield()) {
                button.setEnabled(true);
                button.setAlpha(1.0f);
                cooldownText.setText("Ready");
                cooldownText.setTextColor(0xFF4CAF50);
                progressBar.setProgress(100);
                animateReadyButton(button);
            } else {
                button.setEnabled(false);
                button.setAlpha(0.4f);
                cooldownText.setText("Used");
                cooldownText.setTextColor(0xFF757575);
                progressBar.setProgress(0);
            }

            // Special animation for active shield
            if (superpowerManager.isShieldActive()) {
                animateActiveButton(button, 0xFFFF9800);
            }
        } catch (Exception e) {
            // Shield button update failed
        }
    }

    private void animateReadyButton(Button button) {
        try {
            // Subtle pulse animation for ready buttons
            ObjectAnimator pulse = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.02f, 1f);
            pulse.setDuration(2000);
            pulse.setRepeatCount(ObjectAnimator.INFINITE);
            pulse.start();

            ObjectAnimator pulseY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.02f, 1f);
            pulseY.setDuration(2000);
            pulseY.setRepeatCount(ObjectAnimator.INFINITE);
            pulseY.start();
        } catch (Exception e) {
            // Animation failed - not critical
        }
    }

    private void animateActiveButton(Button button, int glowColor) {
        try {
            // Glowing animation for active superpowers
            ValueAnimator colorAnimator = ValueAnimator.ofArgb(0xFFFFFFFF, glowColor, 0xFFFFFFFF);
            colorAnimator.setDuration(1000);
            colorAnimator.setRepeatCount(ValueAnimator.INFINITE);
            colorAnimator.addUpdateListener(animation -> {
                try {
                    button.setTextColor((Integer) animation.getAnimatedValue());
                } catch (Exception e) {
                    // Color animation failed
                }
            });
            colorAnimator.start();
        } catch (Exception e) {
            // Active animation failed - not critical
        }
    }

    private String formatCooldownTime(long milliseconds) {
        if (milliseconds <= 0) return "Ready";

        long seconds = milliseconds / 1000;
        if (seconds < 60) {
            return seconds + "s";
        } else {
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format(Locale.getDefault(), "%dm %ds", minutes, seconds);
        }
    }

    private void setupBoardDisplay() throws Exception {
        try {
            if (boardView != null) {
                setupBoardView();
            } else if (gameGrid != null) {
                createImprovedGrid();
            } else {
                throw new Exception("No display component available");
            }
        } catch (Exception e) {
            // Critical error - we need some kind of display
            createImprovedGrid();
        }
    }

    private void setupBoardView() throws Exception {
        try {
            boardView.setBoard(board);
            boardView.setCellActionListener(new BoardView.OnCellActionListener() {
                @Override
                public void onCellRevealed(int row, int col) {
                    handleCellClick(row, col);
                }

                @Override
                public void onCellFlagged(int row, int col) {
                    handleCellLongClick(row, col);
                }
            });
            boardView.post(() -> {
                try {
                    boardView.resetViewport();
                } catch (Exception e) {
                    // Viewport reset failed - not critical
                }
            });
        } catch (Exception e) {
            // BoardView setup failed, switch to grid
            if (gameGrid != null) {
                boardView.setVisibility(View.GONE);
                gameGrid.setVisibility(View.VISIBLE);
                createImprovedGrid();
            } else {
                throw e;
            }
        }
    }

    private void createImprovedGrid() {
        if (gameGrid == null) return;

        try {
            gameGrid.removeAllViews();
            gameGrid.setColumnCount(cols);
            gameGrid.setRowCount(rows);

            cellButtons = new Button[rows][cols];

            // Calculate better button size based on screen and grid size
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            // Reserve space for UI elements and superpowers panel
            int reservedWidth = superpowersEnabled ? 600 : 400; // Extra space for superpowers
            int availableWidth = screenWidth - reservedWidth;
            int availableHeight = screenHeight - 300; // Reserve for header/footer

            // Calculate cell size to fit nicely
            int cellWidth = Math.max(30, Math.min(80, availableWidth / cols));
            int cellHeight = Math.max(30, Math.min(80, availableHeight / rows));
            int cellSize = Math.min(cellWidth, cellHeight);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Button cellButton = createImprovedCellButton(r, c, cellSize);
                    cellButtons[r][c] = cellButton;
                    gameGrid.addView(cellButton);
                }
            }

            updateFallbackCellDisplays();
        } catch (Exception e) {
            // Grid creation failed - use basic version
            createBasicGrid();
        }
    }

    private Button createImprovedCellButton(int row, int col, int size) {
        Button cellButton = new Button(this);
        cellButton.setText("");
        cellButton.setTextSize(Math.max(10, size / 4));
        cellButton.setPadding(2, 2, 2, 2);

        // Better styling with gradient
        GradientDrawable cellBg = new GradientDrawable();
        cellBg.setColor(0xFFBBBBBB);
        cellBg.setCornerRadius(4);
        cellBg.setStroke(1, 0xFF888888);
        cellButton.setBackground(cellBg);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(1, 1, 1, 1);
        cellButton.setLayoutParams(params);

        // Set click listeners
        cellButton.setOnClickListener(v -> handleCellClick(row, col));
        cellButton.setOnLongClickListener(v -> {
            handleCellLongClick(row, col);
            return true;
        });

        return cellButton;
    }

    private void createBasicGrid() {
        // Fallback to very basic grid
        try {
            if (gameGrid == null) return;

            gameGrid.setColumnCount(Math.min(cols, 10));
            gameGrid.setRowCount(Math.min(rows, 10));

            int safeRows = Math.min(rows, 10);
            int safeCols = Math.min(cols, 10);
            cellButtons = new Button[safeRows][safeCols];

            for (int r = 0; r < safeRows; r++) {
                for (int c = 0; c < safeCols; c++) {
                    Button btn = new Button(this);
                    btn.setText("");
                    btn.setTextSize(12);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 50;
                    params.height = 50;
                    params.setMargins(1, 1, 1, 1);
                    btn.setLayoutParams(params);

                    final int finalR = r, finalC = c;
                    btn.setOnClickListener(v -> handleCellClick(finalR, finalC));
                    btn.setOnLongClickListener(v -> {
                        handleCellLongClick(finalR, finalC);
                        return true;
                    });

                    cellButtons[r][c] = btn;
                    gameGrid.addView(btn);
                }
            }

            Toast.makeText(this, "Using basic grid - board size limited", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            handleCriticalError(e);
        }
    }

    private void createImprovedMathControls() {
        try {
            if (llMathControls == null) return;

            llMathControls.removeAllViews();

            // Title
            TextView title = new TextView(this);
            title.setText("📊 MATHEMATICS");
            title.setTextSize(14);
            title.setTextColor(0xFF388E3C);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setGravity(Gravity.CENTER);
            title.setPadding(0, 0, 0, 8);
            llMathControls.addView(title);

            // Math buttons with better styling
            Button btnHintNew = createStyledMathButton("💡 AI Hint", 0xFF4CAF50);
            btnHintNew.setOnClickListener(v -> showMathematicalHint());

            Button btnAnalyze = createStyledMathButton("📊 Analysis", 0xFF2196F3);
            btnAnalyze.setOnClickListener(v -> showDetailedAnalysis());

            Button btnToggle = createStyledMathButton("🧮 Toggle", 0xFFFF9800);
            btnToggle.setOnClickListener(v -> toggleMathView());

            llMathControls.addView(btnHintNew);
            llMathControls.addView(btnAnalyze);
            llMathControls.addView(btnToggle);
        } catch (Exception e) {
            // Math button creation failed
        }
    }

    private Button createStyledMathButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(11);
        button.setTextColor(0xFFFFFFFF);
        button.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 50);
        params.setMargins(0, 4, 0, 4);
        button.setLayoutParams(params);

        // Style with gradient
        GradientDrawable bg = new GradientDrawable();
        bg.setColors(new int[]{color, color & 0xFFCCCCCC | 0x33000000});
        bg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        bg.setCornerRadius(8);
        button.setBackground(bg);

        return button;
    }

    private void activateSuperpower(SuperpowerManager.SuperpowerType type) {
        if (superpowerManager == null) {
            Toast.makeText(this, "Superpowers not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            boolean success = false;
            String message = "";

            switch (type) {
                case FREEZE:
                    success = superpowerManager.activateFreeze();
                    message = success ? "⏰ Time frozen for 10 seconds!" : "🧊 Freeze on cooldown";
                    if (success) {
                        gamePaused = true;
                        // Add visual feedback
                        showSuperpowerEffect("🧊 TIME FROZEN", 0xFF00BCD4);
                        new Handler().postDelayed(() -> {
                            gamePaused = false;
                            Toast.makeText(this, "⏰ Time resumed!", Toast.LENGTH_SHORT).show();
                        }, 10000);
                    }
                    break;
                case LIGHTNING:
                    success = superpowerManager.activateLightning();
                    message = success ? "⚡ Lightning reveals safest cell!" : "⚡ Lightning on cooldown";
                    if (success) {
                        showSuperpowerEffect("⚡ LIGHTNING STRIKE", 0xFFFFC107);
                    }
                    break;
                case SHIELD:
                    success = superpowerManager.activateShield();
                    message = success ? "🛡️ Shield protects from next mine!" : "🛡️ Shield already used";
                    if (success) {
                        showSuperpowerEffect("🛡️ SHIELD ACTIVATED", 0xFFFF9800);
                    }
                    break;
                case XRAY:
                    success = superpowerManager.activateXRay(rows/2, cols/2);
                    message = success ? "🔍 X-Ray reveals nearby cells!" : "🔍 X-Ray on cooldown";
                    if (success) {
                        showSuperpowerEffect("🔍 X-RAY VISION", 0xFF4CAF50);
                    }
                    break;
                case SONAR:
                    success = superpowerManager.activateSonar(rows/2, cols/2);
                    message = success ? "🌊 Sonar shows mine count in area!" : "🌊 Sonar on cooldown";
                    if (success) {
                        showSuperpowerEffect("🌊 SONAR PULSE", 0xFF03A9F4);
                    }
                    break;
                case SMART_SWEEP:
                    success = superpowerManager.activateSmartSweep();
                    message = success ? "🎯 Smart Sweep flags obvious mines!" : "🎯 Smart Sweep on cooldown";
                    if (success) {
                        showSuperpowerEffect("🎯 SMART SWEEP", 0xFF9C27B0);
                    }
                    break;
                default:
                    message = "🚧 Superpower not implemented yet";
                    break;
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            if (success) {
                updateDisplay();
                updateGameInfo();
                // Trigger haptic feedback if available
                try {
                    android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(100);
                    }
                } catch (Exception e) {
                    // Vibration failed - not critical
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Superpower failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuperpowerEffect(String text, int color) {
        try {
            // Create overlay effect
            TextView effectText = new TextView(this);
            effectText.setText(text);
            effectText.setTextSize(24);
            effectText.setTextColor(color);
            effectText.setTypeface(null, android.graphics.Typeface.BOLD);
            effectText.setGravity(Gravity.CENTER);
            effectText.setShadowLayer(5, 0, 0, Color.BLACK);

            // Add to main layout temporarily
            ViewGroup mainLayout = (ViewGroup) findViewById(android.R.id.content);
            if (mainLayout != null) {
                mainLayout.addView(effectText);

                // Animate effect
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(effectText, "alpha", 0f, 1f);
                fadeIn.setDuration(300);

                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(effectText, "alpha", 1f, 0f);
                fadeOut.setStartDelay(1500);
                fadeOut.setDuration(500);

                ObjectAnimator scale = ObjectAnimator.ofFloat(effectText, "scaleX", 0.5f, 1.2f, 1f);
                scale.setDuration(300);

                ObjectAnimator scaleY = ObjectAnimator.ofFloat(effectText, "scaleY", 0.5f, 1.2f, 1f);
                scaleY.setDuration(300);

                fadeIn.start();
                scale.start();
                scaleY.start();

                fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        mainLayout.removeView(effectText);
                    }
                });
                fadeOut.start();
            }
        } catch (Exception e) {
            // Effect animation failed - not critical
        }
    }

    private void showGameControls() {
        try {
            // Show math controls if needed
            if (mathMode && llMathControls != null) {
                llMathControls.setVisibility(View.VISIBLE);
                createImprovedMathControls();
            }

            // Show superpower panel if enabled - FIXED!
            if (superpowersEnabled) {
                if (scrollSuperpowerControls != null) {
                    scrollSuperpowerControls.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "⚡ Superpowers panel activated!", Toast.LENGTH_SHORT).show();
                } else {
                    // Fallback: create superpowers section in an existing area
                    createFallbackSuperpowersArea();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "⚡ Superpowers available via options menu", Toast.LENGTH_LONG).show();
        }
    }

    private void createFallbackSuperpowersArea() {
        try {
            // Try to add superpowers to the math panel or create a simple overlay
            if (llMathControls != null) {
                TextView spTitle = new TextView(this);
                spTitle.setText("⚡ SUPERPOWERS (Tap to use)");
                spTitle.setTextSize(12);
                spTitle.setBackgroundColor(0xFF2196F3);
                spTitle.setTextColor(0xFFFFFFFF);
                spTitle.setPadding(8, 8, 8, 8);
                spTitle.setOnClickListener(v -> showSuperpowerMenu());

                llMathControls.addView(spTitle, 0); // Add at top
                llMathControls.setVisibility(View.VISIBLE);

                Toast.makeText(this, "⚡ Superpowers available - tap the blue bar!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            // Even fallback failed
            Toast.makeText(this, "⚡ Use options menu for superpowers", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuperpowerMenu() {
        if (superpowerManager == null) {
            Toast.makeText(this, "Superpowers not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] superpowers = {
                "🧊 Freeze Time (Pause timer)",
                "🔍 X-Ray Vision (Reveal cells)",
                "🌊 Sonar Pulse (Count mines)",
                "⚡ Lightning Strike (Find safest cell)",
                "🛡️ Shield Mode (Survive mine hit)",
                "🎯 Smart Sweep (Auto-flag mines)"
        };

        SuperpowerManager.SuperpowerType[] types = {
                SuperpowerManager.SuperpowerType.FREEZE,
                SuperpowerManager.SuperpowerType.XRAY,
                SuperpowerManager.SuperpowerType.SONAR,
                SuperpowerManager.SuperpowerType.LIGHTNING,
                SuperpowerManager.SuperpowerType.SHIELD,
                SuperpowerManager.SuperpowerType.SMART_SWEEP
        };

        new AlertDialog.Builder(this)
                .setTitle("⚡ Select Superpower")
                .setItems(superpowers, (dialog, which) -> {
                    activateSuperpower(types[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Continue with rest of the methods...
    private void handleCellClick(int row, int col) {
        if (!gameActive || gamePaused) return;

        try {
            // Bounds checking
            if (row < 0 || row >= rows || col < 0 || col >= cols) return;

            // Analyze move mathematically if available
            if (mathAnalyzer != null) {
                try {
                    mathAnalyzer.analyzeMove(row, col,
                            flagMode ? MathAnalyzer.MoveType.FLAG : MathAnalyzer.MoveType.REVEAL);
                } catch (Exception e) {
                    // Math analysis failed - continue anyway
                }
            }

            if (flagMode) {
                board.toggleFlag(row, col);
            } else {
                BughisBoard.RevealResult result = board.revealCell(row, col);

                if (result == BughisBoard.RevealResult.BUG_HIT) {
                    // Check shield
                    if (superpowerManager != null && superpowerManager.handleMineHitWithShield()) {
                        showSuperpowerEffect("🛡️ SHIELD SAVED YOU!", 0xFFFF9800);
                        Toast.makeText(this, "🛡️ Shield absorbed the hit!", Toast.LENGTH_LONG).show();
                    } else {
                        gameOver(false);
                        return;
                    }
                }
            }

            // Update mathematical analysis if available
            if (mathAnalyzer != null) {
                try {
                    mathAnalyzer.updateCompleteAnalysis();
                } catch (Exception e) {
                    // Math update failed - not critical
                }
            }

            updateDisplay();
            updateGameInfo();
            updateMathematicalInfo();

            // Check win condition
            if (board.getGameState() == BughisBoard.GameState.WON) {
                gameOver(true);
            }

            // Check time limit for challenge mode
            if (challengeMode && timeLimit > 0) {
                long elapsed = SystemClock.elapsedRealtime() - startTime - pausedTime;
                if (elapsed >= timeLimit) {
                    gameOver(false);
                    return;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Move failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCellLongClick(int row, int col) {
        if (!gameActive || gamePaused) return;

        try {
            board.toggleFlag(row, col);

            if (mathAnalyzer != null) {
                try {
                    mathAnalyzer.analyzeMove(row, col, MathAnalyzer.MoveType.FLAG);
                    mathAnalyzer.updateCompleteAnalysis();
                } catch (Exception e) {
                    // Math analysis failed - not critical
                }
            }

            updateDisplay();
            updateGameInfo();
            updateMathematicalInfo();
        } catch (Exception e) {
            // Flag toggle failed - not critical
        }
    }

    private void updateDisplay() {
        try {
            if (boardView != null && boardView.getVisibility() == View.VISIBLE) {
                boardView.invalidate();
            } else if (cellButtons != null) {
                updateFallbackCellDisplays();
            }
        } catch (Exception e) {
            // Display update failed - not critical
        }
    }

    private void updateFallbackCellDisplays() {
        if (cellButtons == null || board == null) return;

        try {
            for (int r = 0; r < cellButtons.length && r < rows; r++) {
                for (int c = 0; c < cellButtons[r].length && c < cols; c++) {
                    Cell cell = board.getCell(r, c);
                    Button button = cellButtons[r][c];
                    if (cell != null && button != null) {
                        updateCellAppearance(button, cell);
                    }
                }
            }
        } catch (Exception e) {
            // Cell display update failed - not critical
        }
    }

    private void updateCellAppearance(Button button, Cell cell) {
        try {
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(4);
            bg.setStroke(1, 0xFF888888);

            if (cell.isFlagged()) {
                button.setText("🚩");
                bg.setColor(0xFFFF5722);
                button.setTextColor(0xFFFFFFFF);
            } else if (cell.isRevealed()) {
                if (cell.hasBug()) {
                    button.setText("💣");
                    bg.setColor(0xFFFF0000);
                    button.setTextColor(0xFFFFFFFF);
                } else {
                    int adjacent = cell.getAdjacentBugs();
                    button.setText(adjacent > 0 ? String.valueOf(adjacent) : "");
                    bg.setColor(0xFFDDDDDD);

                    if (adjacent > 0) {
                        int[] colors = {0xFF0000FF, 0xFF008000, 0xFFFF0000, 0xFF800080,
                                0xFF800000, 0xFF008080, 0xFF000000, 0xFF808080};
                        button.setTextColor(colors[Math.min(adjacent - 1, colors.length - 1)]);
                    }
                }
            } else {
                button.setText("");
                bg.setColor(0xFFBBBBBB);
                button.setTextColor(0xFF000000);
            }

            button.setBackground(bg);
        } catch (Exception e) {
            // Cell appearance update failed - use default
            button.setText("?");
            button.setBackgroundColor(0xFF888888);
        }
    }

    private void updateGameInfo() {
        try {
            if (tvTime != null && gameActive) {
                long elapsed = SystemClock.elapsedRealtime() - startTime - pausedTime;

                if (challengeMode && timeLimit > 0) {
                    // Show countdown for challenge mode
                    long remaining = Math.max(0, timeLimit - elapsed);
                    long minutes = remaining / 60000;
                    long seconds = (remaining % 60000) / 1000;
                    tvTime.setText(String.format(Locale.getDefault(), "⏰ Time: %02d:%02d", minutes, seconds));

                    // Change color as time runs out
                    if (remaining < 60000) { // Last minute
                        tvTime.setTextColor(0xFFFF0000);
                    } else if (remaining < 300000) { // Last 5 minutes
                        tvTime.setTextColor(0xFFFF9800);
                    } else {
                        tvTime.setTextColor(0xFFFFFFFF);
                    }
                } else {
                    // Show elapsed time for normal modes
                    long minutes = elapsed / 60000;
                    long seconds = (elapsed % 60000) / 1000;
                    tvTime.setText(String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds));
                }
            }

            if (tvBugsLeft != null && board != null) {
                int remainingBugs = totalBugs - board.getFlaggedCells();
                tvBugsLeft.setText(String.format(Locale.getDefault(), "Bugs Left: %d", remainingBugs));
            }
        } catch (Exception e) {
            // Game info update failed - not critical
        }
    }

    private void updateMathematicalInfo() {
        if (mathAnalyzer == null) return;

        try {
            MathAnalyzer.GameStatistics stats = mathAnalyzer.getCurrentStats();

            if (tvProbabilityInfo != null) {
                String probInfo = String.format(Locale.getDefault(),
                        "📊 Progress: %d%% | Risk: %.1f%%",
                        stats.progressPercentage,
                        stats.averageProbability * 100);
                tvProbabilityInfo.setText(probInfo);
                tvProbabilityInfo.setVisibility(View.VISIBLE);
            }

            if (tvMathInsights != null) {
                double winProb = mathAnalyzer.calculateWinProbability();
                String insights = String.format(Locale.getDefault(),
                        "🧮 Win Probability: %.1f%%",
                        winProb * 100);
                tvMathInsights.setText(insights);
                tvMathInsights.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            // Math info update failed - not critical
        }
    }

    private void toggleFlagMode() {
        flagMode = !flagMode;

        if (btnFlag != null) {
            try {
                GradientDrawable flagBg = new GradientDrawable();
                flagBg.setColor(flagMode ? 0xFFFF9800 : 0xFF2196F3);
                flagBg.setCornerRadius(8);
                btnFlag.setBackground(flagBg);
                btnFlag.setTextColor(0xFFFFFFFF);
                btnFlag.setText(flagMode ? "🚩 ON" : "🚩");
            } catch (Exception e) {
                // Button update failed
            }
        }

        Toast.makeText(this, flagMode ? "🚩 Flag mode ON" : "👆 Reveal mode ON", Toast.LENGTH_SHORT).show();
    }

    private void toggleMathView() {
        mathMode = !mathMode;

        if (mathMode && mathAnalyzer == null) {
            try {
                mathAnalyzer = new MathAnalyzer(this);
                mathAnalyzer.initializeGame(board);
            } catch (Exception e) {
                mathMode = false;
                Toast.makeText(this, "Mathematical analysis not available", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showGameControls();
        updateDisplay();
        updateMathematicalInfo();

        Toast.makeText(this, mathMode ? "🧮 Math mode ON" : "🎮 Math mode OFF", Toast.LENGTH_SHORT).show();
    }

    private void showMathematicalHint() {
        if (mathAnalyzer == null) {
            Toast.makeText(this, "Enable math mode for AI hints", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MathAnalyzer.OptimalMove hint = mathAnalyzer.getOptimalMove();

            if (hint == null || hint.row < 0 || hint.col < 0) {
                Toast.makeText(this, "No optimal move available", Toast.LENGTH_SHORT).show();
                return;
            }

            String hintMessage = String.format(Locale.getDefault(),
                    "🎯 AI HINT\n\n" +
                            "Suggested: Row %d, Col %d\n" +
                            "Safety: %d/100\n" +
                            "Risk: %.1f%%\n\n" +
                            "Reasoning: %s",
                    hint.row + 1, hint.col + 1,
                    hint.safetyScore,
                    hint.probability * 100,
                    hint.reasoning != null ? hint.reasoning : "Mathematical analysis suggests this is the optimal move.");

            new AlertDialog.Builder(this)
                    .setTitle("💡 Mathematical Hint")
                    .setMessage(hintMessage)
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Highlight Cell", (dialog, which) -> {
                        // TODO: Highlight the suggested cell
                        Toast.makeText(this, "Cell highlighting not implemented yet", Toast.LENGTH_SHORT).show();
                    })
                    .show();

        } catch (Exception e) {
            Toast.makeText(this, "Hint calculation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDetailedAnalysis() {
        if (mathAnalyzer == null) {
            Toast.makeText(this, "Mathematical analysis not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MathAnalyzer.GameStatistics stats = mathAnalyzer.getCurrentStats();
            double winProb = mathAnalyzer.calculateWinProbability();

            String analysis = String.format(Locale.getDefault(),
                    "📊 DETAILED GAME ANALYSIS\n\n" +
                            "🎯 Progress: %d%%\n" +
                            "🏆 Win Probability: %.1f%%\n" +
                            "⚠️ Average Risk: %.1f%%\n" +
                            "✅ Cells Revealed: %d\n" +
                            "🚩 Flags Placed: %d\n" +
                            "🎲 Total Entropy: %.2f bits\n\n" +
                            "📈 Mathematical Insights:\n" +
                            "• Current strategy efficiency: %s\n" +
                            "• Information gain potential: %s\n" +
                            "• Risk assessment: %s",
                    stats.progressPercentage,
                    winProb * 100,
                    stats.averageProbability * 100,
                    stats.revealedCells,
                    stats.flaggedCells,
                    stats.totalEntropy,
                    getEfficiencyRating(winProb),
                    getInformationRating(stats.totalEntropy),
                    getRiskRating(stats.averageProbability));

            new AlertDialog.Builder(this)
                    .setTitle("📈 Mathematical Analysis Report")
                    .setMessage(analysis)
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Learn More", (dialog, which) -> showMathEducation())
                    .show();

        } catch (Exception e) {
            Toast.makeText(this, "Analysis generation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String getEfficiencyRating(double winProb) {
        if (winProb > 0.8) return "Excellent";
        if (winProb > 0.6) return "Good";
        if (winProb > 0.4) return "Fair";
        return "Needs improvement";
    }

    private String getInformationRating(double entropy) {
        if (entropy > 50) return "High";
        if (entropy > 20) return "Medium";
        return "Low";
    }

    private String getRiskRating(double avgProb) {
        if (avgProb < 0.2) return "Low risk";
        if (avgProb < 0.5) return "Moderate risk";
        return "High risk";
    }

    private void showMathEducation() {
        try {
            Intent intent = new Intent(this, MathEducationActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Math education not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("🔄 Reset Game")
                .setMessage("Restart the current game? Your progress will be lost.")
                .setPositiveButton("Yes, Reset", (dialog, which) -> resetGame())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetGame() {
        try {
            gameActive = false;
            gamePaused = false;

            if (board != null) {
                board.reset();
            }

            if (mathAnalyzer != null) {
                mathAnalyzer.initializeGame(board);
            }

            if (superpowerManager != null) {
                superpowerManager.reset();
            }

            updateDisplay();
            updateGameInfo();
            updateMathematicalInfo();

            gameActive = true;
            startTime = SystemClock.elapsedRealtime();
            pausedTime = 0;

            Toast.makeText(this, "🎮 Game reset!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Reset failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void gameOver(boolean won) {
        try {
            gameActive = false;
            gamePaused = false;

            // Reveal all cells
            if (board != null) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        Cell cell = board.getCell(r, c);
                        if (cell != null && !cell.isRevealed()) {
                            cell.setRevealed(true);
                        }
                    }
                }
            }

            updateDisplay();

            // Calculate game time
            long gameTime = SystemClock.elapsedRealtime() - startTime - pausedTime;
            long minutes = gameTime / 60000;
            long seconds = (gameTime % 60000) / 1000;

            String message;
            String title;

            if (won) {
                title = "🏆 Victory!";
                message = String.format(Locale.getDefault(),
                        "🎉 CONGRATULATIONS!\n\n" +
                                "⏱️ Time: %02d:%02d\n" +
                                "🎯 Difficulty: %dx%d (%d mines)\n" +
                                "🎮 Mode: %s\n\n" +
                                "Excellent work!",
                        minutes, seconds, rows, cols, totalBugs,
                        getModeDescription());
            } else {
                title = challengeMode && timeLimit > 0 ? "⏰ Time's Up!" : "💔 Game Over";
                message = String.format(Locale.getDefault(),
                        "%s\n\n" +
                                "⏱️ Time survived: %02d:%02d\n" +
                                "🎯 Difficulty: %dx%d (%d mines)\n" +
                                "🎮 Mode: %s\n\n" +
                                "Better luck next time!",
                        challengeMode && timeLimit > 0 ? "Time ran out!" : "Mine hit!",
                        minutes, seconds, rows, cols, totalBugs,
                        getModeDescription());
            }

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("🔄 Play Again", (dialog, which) -> resetGame())
                    .setNeutralButton("🎮 New Game", (dialog, which) -> showNewGameOptions())
                    .setNegativeButton("📊 Menu", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();

        } catch (Exception e) {
            // Game over handling failed - just finish
            finish();
        }
    }

    private String getModeDescription() {
        StringBuilder mode = new StringBuilder();
        if (challengeMode) mode.append("Challenge");
        if (superpowersEnabled) mode.append(mode.length() > 0 ? " + Superpowers" : "Superpowers");
        if (mathMode) mode.append(mode.length() > 0 ? " + Math" : "Math");
        if (mode.length() == 0) mode.append("Classic");
        return mode.toString();
    }

    private void showNewGameOptions() {
        String[] options = {
                "🎯 Same Settings",
                "⚙️ Custom Mode",
                "🔄 Different Difficulty",
                "📊 Main Menu"
        };

        new AlertDialog.Builder(this)
                .setTitle("🎮 New Game Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: resetGame(); break;
                        case 1: showCustomModeDialog(); break;
                        case 2: showDifficultySelection(); break;
                        case 3: finish(); break;
                    }
                })
                .show();
    }

    private void showCustomModeDialog() {
        View customView = getLayoutInflater().inflate(R.layout.dialog_custom_difficulty, null);
        if (customView == null) {
            showSimpleCustomDialog();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙️ Custom Game Settings");
        builder.setView(customView);

        SeekBar sbRows = customView.findViewById(R.id.sbRows);
        SeekBar sbCols = customView.findViewById(R.id.sbCols);
        SeekBar sbBugs = customView.findViewById(R.id.sbBugs);
        TextView tvRowsValue = customView.findViewById(R.id.tvRowsValue);
        TextView tvColsValue = customView.findViewById(R.id.tvColsValue);
        TextView tvBugsValue = customView.findViewById(R.id.tvBugsValue);

        // Set current values
        sbRows.setProgress(rows - 4);
        sbCols.setProgress(cols - 4);
        sbBugs.setProgress(totalBugs - 1);

        tvRowsValue.setText(String.valueOf(rows));
        tvColsValue.setText(String.valueOf(cols));
        tvBugsValue.setText(String.valueOf(totalBugs));

        // Update listeners
        sbRows.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newRows = progress + 4;
                tvRowsValue.setText(String.valueOf(newRows));
                updateMaxBugs(sbBugs, tvBugsValue, newRows, sbCols.getProgress() + 4);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbCols.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newCols = progress + 4;
                tvColsValue.setText(String.valueOf(newCols));
                updateMaxBugs(sbBugs, tvBugsValue, sbRows.getProgress() + 4, newCols);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbBugs.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBugsValue.setText(String.valueOf(progress + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setPositiveButton("Start Game", (dialog, which) -> {
            int newRows = sbRows.getProgress() + 4;
            int newCols = sbCols.getProgress() + 4;
            int newBugs = sbBugs.getProgress() + 1;
            startCustomGame(newRows, newCols, newBugs);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateMaxBugs(SeekBar sbBugs, TextView tvBugsValue, int rows, int cols) {
        int maxBugs = Math.max(1, (rows * cols) / 3);
        sbBugs.setMax(maxBugs - 1);
        if (sbBugs.getProgress() >= maxBugs) {
            sbBugs.setProgress(maxBugs - 1);
            tvBugsValue.setText(String.valueOf(maxBugs));
        }
    }

    private void showSimpleCustomDialog() {
        View dialogView = new LinearLayout(this);
        ((LinearLayout) dialogView).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) dialogView).setPadding(32, 32, 32, 32);

        EditText etRows = new EditText(this);
        etRows.setHint("Rows (4-50)");
        etRows.setText(String.valueOf(rows));

        EditText etCols = new EditText(this);
        etCols.setHint("Columns (4-50)");
        etCols.setText(String.valueOf(cols));

        EditText etBugs = new EditText(this);
        etBugs.setHint("Mines (1-" + ((rows * cols) / 3) + ")");
        etBugs.setText(String.valueOf(totalBugs));

        ((LinearLayout) dialogView).addView(etRows);
        ((LinearLayout) dialogView).addView(etCols);
        ((LinearLayout) dialogView).addView(etBugs);

        new AlertDialog.Builder(this)
                .setTitle("⚙️ Custom Settings")
                .setView(dialogView)
                .setPositiveButton("Start", (dialog, which) -> {
                    try {
                        int newRows = Integer.parseInt(etRows.getText().toString());
                        int newCols = Integer.parseInt(etCols.getText().toString());
                        int newBugs = Integer.parseInt(etBugs.getText().toString());
                        startCustomGame(newRows, newCols, newBugs);
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startCustomGame(int newRows, int newCols, int newBugs) {
        // Validate inputs
        newRows = Math.max(4, Math.min(50, newRows));
        newCols = Math.max(4, Math.min(50, newCols));
        newBugs = Math.max(1, Math.min((newRows * newCols) / 3, newBugs));

        // Update settings
        this.rows = newRows;
        this.cols = newCols;
        this.totalBugs = newBugs;

        // Restart game with new settings
        resetGame();

        Toast.makeText(this, String.format(Locale.getDefault(),
                        "Custom game: %dx%d with %d mines", newRows, newCols, newBugs),
                Toast.LENGTH_LONG).show();
    }

    private void showDifficultySelection() {
        String[] difficulties = {
                "🟢 Easy (8×8, 10 mines)",
                "🟡 Medium (16×16, 40 mines)",
                "🔴 Hard (24×24, 99 mines)",
                "🌟 Expert (30×30, 150 mines)"
        };

        new AlertDialog.Builder(this)
                .setTitle("🎯 Select Difficulty")
                .setItems(difficulties, (dialog, which) -> {
                    switch (which) {
                        case 0: rows = 8; cols = 8; totalBugs = 10; break;
                        case 1: rows = 16; cols = 16; totalBugs = 40; break;
                        case 2: rows = 24; cols = 24; totalBugs = 99; break;
                        case 3: rows = 30; cols = 30; totalBugs = 150; break;
                    }
                    resetGame();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGameModeStatus() {
        try {
            StringBuilder status = new StringBuilder("🎮 Game started!\n");

            status.append("📏 Size: ").append(rows).append("×").append(cols).append("\n");
            status.append("💣 Mines: ").append(totalBugs).append("\n");

            if (superpowersEnabled) status.append("⚡ Superpowers enabled\n");
            if (mathMode) status.append("🧮 Math analysis enabled\n");
            if (challengeMode) {
                long minutes = timeLimit / 60000;
                status.append("⏰ Challenge mode: ").append(minutes).append(" minutes\n");
            }

            Toast.makeText(this, status.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "🎮 Game started!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTimer() {
        try {
            timerHandler = new Handler();
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (gameActive && !gamePaused) {
                        updateGameInfo();
                        updateMathematicalInfo();
                        if (timerHandler != null) {
                            timerHandler.postDelayed(this, 1000);
                        }
                    }
                }
            };
            if (timerHandler != null) {
                timerHandler.post(timerRunnable);
            }
        } catch (Exception e) {
            // Timer setup failed - not critical
        }
    }

    private void handleCriticalError(Exception e) {
        try {
            String errorMsg = "Critical error: " + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();

            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Error")
                    .setMessage("The game encountered a critical error and will return to the main menu.\n\nError: " + e.getMessage())
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        } catch (Exception e2) {
            // Even error handling failed - just finish
            finish();
        }
    }

    private void handleGameInitializationError(Exception e) {
        try {
            Toast.makeText(this, "Game initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();

            // Try to create a minimal working game
            if (board == null) {
                board = new BughisBoard(8, 8, 10);
                rows = 8; cols = 8; totalBugs = 10;
            }

            // Disable advanced features
            mathMode = false;
            superpowersEnabled = false;
            mathAnalyzer = null;
            superpowerManager = null;

            // Create basic display
            createBasicGrid();

            gameActive = true;
            startTime = SystemClock.elapsedRealtime();

            Toast.makeText(this, "🎮 Basic game mode activated", Toast.LENGTH_SHORT).show();
        } catch (Exception e2) {
            handleCriticalError(e2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.game_menu, menu);

            // Add superpower menu items if enabled as backup
            if (superpowersEnabled && superpowerManager != null) {
                menu.add(0, 1001, 0, "🧊 Freeze Time");
                menu.add(0, 1002, 0, "🔍 X-Ray Vision");
                menu.add(0, 1003, 0, "⚡ Lightning Strike");
                menu.add(0, 1004, 0, "🛡️ Shield Mode");
                menu.add(0, 1005, 0, "🌊 Sonar Pulse");
                menu.add(0, 1006, 0, "🎯 Smart Sweep");
            }

            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();

            if (id == android.R.id.home) {
                showExitConfirmation();
                return true;
            } else if (id == R.id.action_reset) {
                showResetConfirmation();
                return true;
            } else if (id == R.id.action_new_game) {
                showNewGameOptions();
                return true;
            } else if (id == R.id.action_toggle_math_view) {
                toggleMathView();
                return true;
            } else if (id == R.id.action_math_hint) {
                showMathematicalHint();
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Menu action failed", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Game")
                .setMessage("Exit current game? Your progress will be lost.")
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setNegativeButton("Stay", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameActive && !challengeMode) { // Don't pause challenge mode
            gamePaused = true;
            pausedTime += SystemClock.elapsedRealtime();
            Toast.makeText(this, "⏸️ Game paused", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gamePaused) {
            pausedTime = SystemClock.elapsedRealtime() - pausedTime;
            gamePaused = false;
            if (timerHandler != null && timerRunnable != null) {
                timerHandler.post(timerRunnable);
            }
            Toast.makeText(this, "▶️ Game resumed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (timerHandler != null && timerRunnable != null) {
                timerHandler.removeCallbacks(timerRunnable);
            }
            if (superpowerUpdateHandler != null && superpowerUpdateRunnable != null) {
                superpowerUpdateHandler.removeCallbacks(superpowerUpdateRunnable);
            }
        } catch (Exception e) {
            // Cleanup failed - not critical
        }
    }

    // Helper class for superpower information
    private static class SuperpowerInfo {
        final SuperpowerManager.SuperpowerType type;
        final String emoji;
        final String name;
        final String description;
        final int color;

        SuperpowerInfo(SuperpowerManager.SuperpowerType type, String emoji, String name, String description, int color) {
            this.type = type;
            this.emoji = emoji;
            this.name = name;
            this.description = description;
            this.color = color;
        }
    }
}