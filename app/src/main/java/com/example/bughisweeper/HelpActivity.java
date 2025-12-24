package com.example.bughisweeper;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Simple Help activity with educational content
 */
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Help & Tutorial");
        }

        // For now, show a comprehensive help dialog instead of complex layouts
        showHelpDialog();
    }

    private void showHelpDialog() {
        String helpContent =
                "🎮 BUGHISWEEPER HELP\n\n" +

                        "🎯 OBJECTIVE:\n" +
                        "Clear the minefield without hitting any bugs! Use logical deduction and mathematical probability.\n\n" +

                        "🎮 BASIC CONTROLS:\n" +
                        "• Tap to reveal a cell\n" +
                        "• Long press to flag a suspected bug\n" +
                        "• Use two fingers to zoom in/out\n" +
                        "• Toggle flag mode with the flag button\n\n" +

                        "🔢 READING NUMBERS:\n" +
                        "Each number shows how many bugs are in the 8 adjacent cells.\n\n" +

                        "📊 DIFFICULTY LEVELS:\n" +
                        "• Easy: 8×8 grid, 10 bugs\n" +
                        "• Medium: 16×16 grid, 40 bugs\n" +
                        "• Hard: 24×24 grid, 99 bugs\n" +
                        "• Custom: Configure your own settings\n\n" +

                        "⚡ SUPERPOWERS:\n" +
                        "• 🧊 Freeze Time: Pause timer for strategic planning\n" +
                        "• 🔍 X-Ray Vision: Reveal adjacent cells safely\n" +
                        "• 🌊 Sonar Pulse: Count mines in 5×5 area\n" +
                        "• ⚡ Lightning Strike: Auto-reveal safest cell\n" +
                        "• 🛡️ Shield Mode: Survive one mine hit\n" +
                        "• 🎯 Smart Sweep: Auto-flag obvious mines\n\n" +

                        "📐 MATHEMATICAL CONCEPTS:\n" +
                        "• Probability Theory & Bayesian Inference\n" +
                        "• Information Theory & Shannon Entropy\n" +
                        "• Constraint Satisfaction Algorithms\n" +
                        "• Statistical Analysis & Risk Assessment\n\n" +

                        "🧠 STRATEGIES:\n" +
                        "• Start with corners/edges for more info\n" +
                        "• Use constraint solving before guessing\n" +
                        "• Track probabilities mentally\n" +
                        "• Combine superpowers strategically\n\n" +

                        "🌐 REAL-WORLD APPLICATIONS:\n" +
                        "Skills learned apply to finance, medicine, AI, and more!";

        new AlertDialog.Builder(this)
                .setTitle("📖 Complete Help Guide")
                .setMessage(helpContent)
                .setPositiveButton("Got it!", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        finish(); // Close help activity
                    }
                })
                .setNeutralButton("Keep Open", null)
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Add transition if available
        try {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } catch (Exception e) {
            // Ignore if animations don't exist
        }
    }
}