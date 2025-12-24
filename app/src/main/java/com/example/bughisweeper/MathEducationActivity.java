package com.example.bughisweeper;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Safe MathEducationActivity with crash protection
 */
public class MathEducationActivity extends AppCompatActivity {

    // UI Components - all optional to prevent crashes
    private EditText etMines, etCells, etRevealed;
    private SeekBar sbMines, sbCells;
    private TextView tvBasicProb, tvBayesianResult, tvEntropyResult, tvExplanation;
    private Button btnCalculate, btnLearnBayesian, btnLearnEntropy, btnRealWorldExample;
    private MathVisualizationView mathViz;

    // Math components
    private DecimalFormat df = new DecimalFormat("0.000");
    private DecimalFormat pf = new DecimalFormat("0.0%");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_math_education);
        } catch (Exception e) {
            // If layout doesn't exist, create a simple fallback
            createFallbackLayout();
        }

        initializeViews();
        setupListeners();
        setupInitialValues();

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("🧮 Mathematical Learning");
        }
    }

    /**
     * Create a simple fallback layout if the main layout fails
     */
    private void createFallbackLayout() {
        setContentView(android.R.layout.activity_list_item);

        // Show a simple message
        Toast.makeText(this, "🧮 Mathematical Learning Mode\nAdvanced interface loading...", Toast.LENGTH_LONG).show();

        // Create minimal interface programmatically
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("🧮 Interactive Mathematical Learning");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        TextView description = new TextView(this);
        description.setText("This section teaches probability theory, Bayesian inference, and information theory through interactive examples.\n\nFull interface coming soon!");
        description.setTextSize(16);
        layout.addView(description);

        Button backButton = new Button(this);
        backButton.setText("← Back to Game");
        backButton.setOnClickListener(v -> finish());
        layout.addView(backButton);

        setContentView(layout);
    }

    private void initializeViews() {
        // Safely try to find views - don't crash if they don't exist
        try {
            etMines = findViewById(R.id.etMines);
            etCells = findViewById(R.id.etCells);
            etRevealed = findViewById(R.id.etRevealed);
            sbMines = findViewById(R.id.sbMines);
            sbCells = findViewById(R.id.sbCells);

            tvBasicProb = findViewById(R.id.tvBasicProb);
            tvBayesianResult = findViewById(R.id.tvBayesianResult);
            tvEntropyResult = findViewById(R.id.tvEntropyResult);
            tvExplanation = findViewById(R.id.tvExplanation);

            btnCalculate = findViewById(R.id.btnCalculate);
            btnLearnBayesian = findViewById(R.id.btnLearnBayesian);
            btnLearnEntropy = findViewById(R.id.btnLearnEntropy);
            btnRealWorldExample = findViewById(R.id.btnRealWorldExample);

            mathViz = findViewById(R.id.mathVisualization);
        } catch (Exception e) {
            // Views not found - that's okay, we'll work with what we have
        }
    }

    private void setupListeners() {
        // Only set up listeners for views that exist
        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> calculateProbabilities());
        }

        if (btnLearnBayesian != null) {
            btnLearnBayesian.setOnClickListener(v -> showBayesianExample());
        }

        if (btnLearnEntropy != null) {
            btnLearnEntropy.setOnClickListener(v -> showEntropyExample());
        }

        if (btnRealWorldExample != null) {
            btnRealWorldExample.setOnClickListener(v -> showRealWorldApplications());
        }

        // Seekbar listeners
        if (sbMines != null) {
            sbMines.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && etMines != null) {
                        etMines.setText(String.valueOf(progress + 1));
                        calculateProbabilities();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (sbCells != null) {
            sbCells.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && etCells != null) {
                        etCells.setText(String.valueOf(progress + 10));
                        calculateProbabilities();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
    }

    private void setupInitialValues() {
        if (etMines != null) etMines.setText("10");
        if (etCells != null) etCells.setText("64");
        if (etRevealed != null) etRevealed.setText("0");
        if (sbMines != null) sbMines.setProgress(9); // 10 - 1
        if (sbCells != null) sbCells.setProgress(54); // 64 - 10

        calculateProbabilities();
    }

    private void calculateProbabilities() {
        try {
            int mines = etMines != null ? Integer.parseInt(etMines.getText().toString()) : 10;
            int totalCells = etCells != null ? Integer.parseInt(etCells.getText().toString()) : 64;
            int revealed = etRevealed != null ? Integer.parseInt(etRevealed.getText().toString()) : 0;

            if (mines >= totalCells) {
                Toast.makeText(this, "Mines must be less than total cells", Toast.LENGTH_SHORT).show();
                return;
            }

            if (revealed >= totalCells) {
                Toast.makeText(this, "Revealed cells cannot exceed total cells", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculate basic probability
            int unrevealed = totalCells - revealed;
            double basicProb = unrevealed > 0 ? (double) mines / unrevealed : 0;

            // Calculate Bayesian probability (simplified example)
            double bayesianProb = calculateBayesianExample(mines, totalCells, revealed);

            // Calculate entropy
            double entropy = calculateEntropy(basicProb);

            // Update displays
            updateProbabilityDisplays(basicProb, bayesianProb, entropy, mines, totalCells, revealed);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error in calculation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateBayesianExample(int mines, int totalCells, int revealed) {
        // Simplified Bayesian calculation for educational purposes
        int unrevealed = totalCells - revealed;
        double priorProb = (double) mines / totalCells;

        // Simulate evidence: assume we found some safe cells
        double evidenceFactor = revealed > 0 ? 1.0 - (0.1 * revealed / totalCells) : 1.0;

        // Bayesian update (simplified)
        double posteriorProb = (priorProb * evidenceFactor) /
                (priorProb * evidenceFactor + (1 - priorProb) * (1 - evidenceFactor));

        return Math.max(0, Math.min(1, posteriorProb));
    }

    private double calculateEntropy(double probability) {
        if (probability <= 0 || probability >= 1) {
            return 0;
        }
        // Shannon entropy: H = -p*log2(p) - (1-p)*log2(1-p)
        return -(probability * log2(probability) + (1 - probability) * log2(1 - probability));
    }

    private double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    private void updateProbabilityDisplays(double basic, double bayesian, double entropy,
                                           int mines, int totalCells, int revealed) {
        // Basic probability
        if (tvBasicProb != null) {
            String basicText = String.format(Locale.getDefault(),
                    "📊 Basic Probability\n" +
                            "P(mine) = %d ÷ %d = %s\n" +
                            "Formula: remaining_mines ÷ unrevealed_cells",
                    mines, (totalCells - revealed), pf.format(basic));
            tvBasicProb.setText(basicText);
        }

        // Bayesian probability
        if (tvBayesianResult != null) {
            String bayesianText = String.format(Locale.getDefault(),
                    "🧠 Bayesian Inference\n" +
                            "Updated probability: %s\n" +
                            "Accounts for evidence from revealed cells",
                    pf.format(bayesian));
            tvBayesianResult.setText(bayesianText);
        }

        // Entropy
        if (tvEntropyResult != null) {
            String entropyText = String.format(Locale.getDefault(),
                    "📈 Information Theory\n" +
                            "Shannon Entropy: %s bits\n" +
                            "Information content per reveal",
                    df.format(entropy));
            tvEntropyResult.setText(entropyText);
        }

        // Educational explanation
        updateExplanation(basic, entropy);
    }

    private void updateExplanation(double probability, double entropy) {
        if (tvExplanation == null) return;

        StringBuilder explanation = new StringBuilder();
        explanation.append("🎓 Mathematical Insights:\n\n");

        // Probability interpretation
        if (probability < 0.2) {
            explanation.append("• Low risk situation - good for learning safe moves\n");
        } else if (probability < 0.5) {
            explanation.append("• Moderate risk - perfect for strategy development\n");
        } else {
            explanation.append("• High risk situation - requires careful analysis\n");
        }

        // Entropy interpretation
        if (entropy > 0.8) {
            explanation.append("• High information gain expected from next move\n");
        } else if (entropy > 0.5) {
            explanation.append("• Moderate information gain available\n");
        } else {
            explanation.append("• Low uncertainty - situation is relatively clear\n");
        }

        // Educational content
        explanation.append("\n🔬 Real-world Applications:\n");
        explanation.append("• Medical diagnosis probability\n");
        explanation.append("• Financial risk assessment\n");
        explanation.append("• Quality control in manufacturing\n");
        explanation.append("• Machine learning uncertainty\n");

        tvExplanation.setText(explanation.toString());
    }

    private void showBayesianExample() {
        String example =
                "🧠 BAYESIAN INFERENCE EXAMPLE\n\n" +

                        "Problem: You're a doctor diagnosing a rare disease.\n\n" +

                        "Given Information:\n" +
                        "• Disease affects 1% of population (prior probability)\n" +
                        "• Test is 95% accurate for positive cases\n" +
                        "• Test is 90% accurate for negative cases\n\n" +

                        "Question: If test is positive, what's the probability the patient has the disease?\n\n" +

                        "Bayesian Formula:\n" +
                        "P(Disease|Positive) = P(Positive|Disease) × P(Disease) ÷ P(Positive)\n\n" +

                        "Calculation:\n" +
                        "P(Disease|Positive) = 0.95 × 0.01 ÷ 0.1085 = 8.76%\n\n" +

                        "Surprising Result: Even with a positive test, there's only an 8.76% chance of having the disease!\n\n" +

                        "This is exactly how Bughisweeper works - we update probabilities based on evidence from revealed numbers.";

        showEducationalDialog("Bayesian Inference", example);
    }

    private void showEntropyExample() {
        String example =
                "📈 INFORMATION THEORY EXAMPLE\n\n" +

                        "Shannon Entropy measures uncertainty and information content.\n\n" +

                        "Formula: H = -Σ(p × log₂(p))\n\n" +

                        "Examples:\n" +
                        "• Fair coin flip: H = 1 bit (maximum uncertainty)\n" +
                        "• Biased coin (90% heads): H = 0.47 bits\n" +
                        "• Certain outcome: H = 0 bits (no uncertainty)\n\n" +

                        "In Minesweeper:\n" +
                        "• High entropy cells provide more information when revealed\n" +
                        "• Low entropy cells are more predictable\n" +
                        "• Optimal strategy: balance safety with information gain\n\n" +

                        "Real-world Applications:\n" +
                        "• Data compression algorithms\n" +
                        "• Communication systems\n" +
                        "• Machine learning feature selection\n" +
                        "• Cryptography and security";

        showEducationalDialog("Information Theory", example);
    }

    private void showRealWorldApplications() {
        String applications =
                "🌐 REAL-WORLD APPLICATIONS\n\n" +

                        "🏥 MEDICAL DIAGNOSIS:\n" +
                        "• Probability of disease given symptoms\n" +
                        "• Bayesian networks for diagnosis\n" +
                        "• Information theory in medical imaging\n\n" +

                        "💰 FINANCE:\n" +
                        "• Risk assessment for investments\n" +
                        "• Fraud detection algorithms\n" +
                        "• Portfolio optimization\n\n" +

                        "🏭 MANUFACTURING:\n" +
                        "• Quality control statistics\n" +
                        "• Predictive maintenance\n" +
                        "• Process optimization\n\n" +

                        "🤖 ARTIFICIAL INTELLIGENCE:\n" +
                        "• Machine learning uncertainty\n" +
                        "• Feature selection using entropy\n" +
                        "• Bayesian neural networks\n\n" +

                        "🔒 CYBERSECURITY:\n" +
                        "• Intrusion detection systems\n" +
                        "• Cryptographic key generation\n" +
                        "• Risk analysis for vulnerabilities\n\n" +

                        "The mathematical skills you learn in Bughisweeper directly apply to solving real-world problems!";

        showEducationalDialog("Real-World Applications", applications);
    }

    private void showEducationalDialog(String title, String content) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("📚 " + title)
                .setMessage(content)
                .setPositiveButton("Understand!", null)
                .setNeutralButton("Try Interactive Example", (dialog, which) -> {
                    // Reset with educational values
                    if (etMines != null) etMines.setText("15");
                    if (etCells != null) etCells.setText("50");
                    if (etRevealed != null) etRevealed.setText("10");
                    calculateProbabilities();
                    Toast.makeText(MathEducationActivity.this,
                            "Try changing the values to see how probabilities change!",
                            Toast.LENGTH_LONG).show();
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }
}