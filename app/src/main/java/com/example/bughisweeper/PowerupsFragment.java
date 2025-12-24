package com.example.bughisweeper;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;

/**
 * Fragment for superpower management and display
 */
public class PowerupsFragment extends Fragment implements SuperpowerManager.OnSuperpowerListener {

    private Button btnFreeze, btnXRay, btnSonar, btnLightning, btnShield, btnSmartSweep;
    private TextView tvFreezeCooldown, tvXRayCooldown, tvSonarCooldown;
    private TextView tvLightningCooldown, tvSmartSweepCooldown;
    private TextView tvShieldStatus;
    private ProgressBar pbFreeze, pbXRay, pbSonar, pbLightning, pbSmartSweep;

    private SuperpowerManager superpowerManager;
    private Handler cooldownHandler;
    private Runnable cooldownRunnable;

    private OnSuperpowerActivationListener activationListener;

    public interface OnSuperpowerActivationListener {
        void onSuperpowerRequested(SuperpowerManager.SuperpowerType type, int row, int col);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_powerups, container, false);

        initializeViews(view);
        setupButtonListeners();
        setupCooldownLoop();

        return view;
    }

    private void initializeViews(View view) {
        btnFreeze = view.findViewById(R.id.btnFreeze);
        btnXRay = view.findViewById(R.id.btnXRay);
        btnSonar = view.findViewById(R.id.btnSonar);
        btnLightning = view.findViewById(R.id.btnLightning);
        btnShield = view.findViewById(R.id.btnShield);
        btnSmartSweep = view.findViewById(R.id.btnSmartSweep);

        tvFreezeCooldown = view.findViewById(R.id.tvFreezeCooldown);
        tvXRayCooldown = view.findViewById(R.id.tvXRayCooldown);
        tvSonarCooldown = view.findViewById(R.id.tvSonarCooldown);
        tvLightningCooldown = view.findViewById(R.id.tvLightningCooldown);
        tvSmartSweepCooldown = view.findViewById(R.id.tvSmartSweepCooldown);
        tvShieldStatus = view.findViewById(R.id.tvShieldStatus);

        pbFreeze = view.findViewById(R.id.pbFreeze);
        pbXRay = view.findViewById(R.id.pbXRay);
        pbSonar = view.findViewById(R.id.pbSonar);
        pbLightning = view.findViewById(R.id.pbLightning);
        pbSmartSweep = view.findViewById(R.id.pbSmartSweep);
    }

    private void setupButtonListeners() {
        btnFreeze.setOnClickListener(v -> requestSuperpower(SuperpowerManager.SuperpowerType.FREEZE));
        btnXRay.setOnClickListener(v -> requestSuperpower(SuperpowerManager.SuperpowerType.XRAY));
        btnSonar.setOnClickListener(v -> requestSuperpower(SuperpowerManager.SuperpowerType.SONAR));
        btnLightning.setOnClickListener(v -> requestSuperpower(SuperpowerManager.SuperpowerType.LIGHTNING));
        btnShield.setOnClickListener(v -> requestSuperpower(SuperpowerManager.SuperpowerType.SHIELD));
        btnSmartSweep.setOnClickListener(v -> requestSuperpower(SuperpowerManager.SuperpowerType.SMART_SWEEP));
    }

    private void setupCooldownLoop() {
        cooldownHandler = new Handler();
        cooldownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCooldowns();
                cooldownHandler.postDelayed(this, 100); // Update every 100ms for smooth progress
            }
        };
    }

    public void setSuperpowerManager(SuperpowerManager manager) {
        this.superpowerManager = manager;
        manager.setOnSuperpowerListener(this);
    }

    public void setOnSuperpowerActivationListener(OnSuperpowerActivationListener listener) {
        this.activationListener = listener;
    }

    private void requestSuperpower(SuperpowerManager.SuperpowerType type) {
        if (activationListener != null) {
            activationListener.onSuperpowerRequested(type, -1, -1); // Row/col set by game activity
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cooldownHandler != null && cooldownRunnable != null) {
            cooldownHandler.post(cooldownRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cooldownHandler != null && cooldownRunnable != null) {
            cooldownHandler.removeCallbacks(cooldownRunnable);
        }
    }

    private void updateCooldowns() {
        if (superpowerManager == null) return;

        updateSuperpowerButton(btnFreeze, pbFreeze, tvFreezeCooldown,
                superpowerManager.canUseFreeze(),
                superpowerManager.getRemainingFreezeCooldown(), 60000);

        updateSuperpowerButton(btnXRay, pbXRay, tvXRayCooldown,
                superpowerManager.canUseXRay(),
                superpowerManager.getRemainingXRayCooldown(), 45000);

        updateSuperpowerButton(btnSonar, pbSonar, tvSonarCooldown,
                superpowerManager.canUseSonar(),
                superpowerManager.getRemainingSonarCooldown(), 30000);

        updateSuperpowerButton(btnLightning, pbLightning, tvLightningCooldown,
                superpowerManager.canUseLightning(),
                superpowerManager.getRemainingLightningCooldown(), 90000);

        updateSuperpowerButton(btnSmartSweep, pbSmartSweep, tvSmartSweepCooldown,
                superpowerManager.canUseSmartSweep(),
                superpowerManager.getRemainingSmartSweepCooldown(), 20000);

        // Special handling for shield (one-time use)
        if (superpowerManager.canUseShield()) {
            btnShield.setEnabled(true);
            btnShield.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.superpower_ready));
            tvShieldStatus.setText("Ready");
            tvShieldStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.superpower_ready_text));
        } else {
            btnShield.setEnabled(false);
            btnShield.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.superpower_used));
            tvShieldStatus.setText("Used");
            tvShieldStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.superpower_used_text));
        }

        // Show active status
        updateActiveStatus(btnFreeze, superpowerManager.isFreezeActive());
        updateActiveStatus(btnXRay, superpowerManager.isXRayActive());
        updateActiveStatus(btnShield, superpowerManager.isShieldActive());
    }

    private void updateSuperpowerButton(Button button, ProgressBar progressBar, TextView cooldownText,
                                        boolean canUse, long remainingCooldown, long totalCooldown) {
        if (canUse) {
            button.setEnabled(true);
            button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.superpower_ready));
            progressBar.setProgress(100);
            cooldownText.setText("Ready");
            cooldownText.setTextColor(ContextCompat.getColor(getContext(), R.color.superpower_ready_text));
        } else {
            button.setEnabled(false);
            button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.superpower_cooldown));

            int progress = (int) (((totalCooldown - remainingCooldown) * 100) / totalCooldown);
            progressBar.setProgress(progress);

            String timeText = formatCooldownTime(remainingCooldown);
            cooldownText.setText(timeText);
            cooldownText.setTextColor(ContextCompat.getColor(getContext(), R.color.superpower_cooldown_text));
        }
    }

    private void updateActiveStatus(Button button, boolean isActive) {
        if (isActive) {
            // Add pulsing animation for active powers
            ValueAnimator pulseAnimator = ValueAnimator.ofFloat(0.8f, 1.2f);
            pulseAnimator.setDuration(500);
            pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
            pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulseAnimator.addUpdateListener(animation -> {
                float scale = (float) animation.getAnimatedValue();
                button.setScaleX(scale);
                button.setScaleY(scale);
            });
            pulseAnimator.start();
            button.setTag(pulseAnimator); // Store animator for cleanup
        } else {
            // Stop pulsing animation
            ValueAnimator animator = (ValueAnimator) button.getTag();
            if (animator != null) {
                animator.cancel();
                button.setScaleX(1f);
                button.setScaleY(1f);
                button.setTag(null);
            }
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

    // SuperpowerManager.OnSuperpowerListener implementation
    @Override
    public void onSuperpowerActivated(SuperpowerManager.SuperpowerType type) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Show activation feedback
                Button button = getButtonForType(type);
                if (button != null) {
                    // Flash animation
                    ObjectAnimator flash = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.3f, 1f);
                    flash.setDuration(300);
                    flash.start();

                    // Add glow effect
                    button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.superpower_active));
                }

                // Show toast or snackbar with power description
                android.widget.Toast.makeText(getContext(),
                        type.getDisplayName() + " activated!",
                        android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onSuperpowerDeactivated(SuperpowerManager.SuperpowerType type) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Button button = getButtonForType(type);
                if (button != null) {
                    updateActiveStatus(button, false);
                }
            });
        }
    }

    @Override
    public void onCooldownUpdate(SuperpowerManager.SuperpowerType type, long remainingMs) {
        // Handled in updateCooldowns()
    }

    @Override
    public void onMathematicalAnalysisUpdate(double[][] probabilities) {
        // Not needed in this fragment
    }

    private Button getButtonForType(SuperpowerManager.SuperpowerType type) {
        switch (type) {
            case FREEZE: return btnFreeze;
            case XRAY: return btnXRay;
            case SONAR: return btnSonar;
            case LIGHTNING: return btnLightning;
            case SHIELD: return btnShield;
            case SMART_SWEEP: return btnSmartSweep;
            default: return null;
        }
    }

    /**
     * Reset all powerups for new game
     */
    public void resetPowerups() {
        // Stop all animations
        updateActiveStatus(btnFreeze, false);
        updateActiveStatus(btnXRay, false);
        updateActiveStatus(btnSonar, false);
        updateActiveStatus(btnLightning, false);
        updateActiveStatus(btnShield, false);
        updateActiveStatus(btnSmartSweep, false);

        // Reset all progress bars and enable all buttons
        pbFreeze.setProgress(100);
        pbXRay.setProgress(100);
        pbSonar.setProgress(100);
        pbLightning.setProgress(100);
        pbSmartSweep.setProgress(100);

        btnFreeze.setEnabled(true);
        btnXRay.setEnabled(true);
        btnSonar.setEnabled(true);
        btnLightning.setEnabled(true);
        btnShield.setEnabled(true);
        btnSmartSweep.setEnabled(true);

        // Reset colors
        int readyColor = ContextCompat.getColor(getContext(), R.color.superpower_ready);
        btnFreeze.setBackgroundColor(readyColor);
        btnXRay.setBackgroundColor(readyColor);
        btnSonar.setBackgroundColor(readyColor);
        btnLightning.setBackgroundColor(readyColor);
        btnShield.setBackgroundColor(readyColor);
        btnSmartSweep.setBackgroundColor(readyColor);

        // Reset cooldown texts
        tvFreezeCooldown.setText("Ready");
        tvXRayCooldown.setText("Ready");
        tvSonarCooldown.setText("Ready");
        tvLightningCooldown.setText("Ready");
        tvSmartSweepCooldown.setText("Ready");
        tvShieldStatus.setText("Ready");
    }

    /**
     * Show superpower descriptions and mathematical explanations
     */
    public void showSuperpowerInfo(SuperpowerManager.SuperpowerType type) {
        String title = type.getDisplayName();
        String description = type.getDescription();
        String mathematics = getMathematicalExplanation(type);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(description + "\n\n📐 Mathematical Basis:\n" + mathematics)
                .setPositiveButton("Got it!", null)
                .show();
    }

    private String getMathematicalExplanation(SuperpowerManager.SuperpowerType type) {
        switch (type) {
            case FREEZE:
                return "Provides time for complex probability calculations without timer pressure. " +
                        "Use for computing optimal move sequences using decision trees.";

            case XRAY:
                return "Reveals evidence for Bayesian probability updates:\n" +
                        "P(mine|evidence) = P(evidence|mine) × P(mine) / P(evidence)\n" +
                        "Reduces uncertainty in local probability distributions.";

            case SONAR:
                return "Uses wave interference formula: I = Σ(1/d²)\n" +
                        "Provides constraint satisfaction data over 5×5 areas. " +
                        "Intensity inversely proportional to distance squared.";

            case LIGHTNING:
                return "Calculates optimal move using information theory:\n" +
                        "Score = (1-P(mine))×0.7 + entropy×0.3\n" +
                        "Where entropy = -Σ(p×log₂(p)) measures information gain.";

            case SHIELD:
                return "Provides safety net for high-risk moves. Enables exploration " +
                        "of uncertain areas to gather constraint satisfaction data " +
                        "without game termination risk.";

            case SMART_SWEEP:
                return "Applies constraint satisfaction algorithms:\n" +
                        "If adjacent_mines_needed = unrevealed_neighbors, flag all.\n" +
                        "Uses logical deduction to eliminate obvious mine locations.";

            default:
                return "Mathematical analysis not available.";
        }
    }
}