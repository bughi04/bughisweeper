package com.example.bughisweeper;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bughisweeper.R;
import com.example.bughisweeper.ScoreManager;
import com.example.bughisweeper.DifficultyManager;
import com.example.bughisweeper.Score;
import com.example.bughisweeper.ScoreAdapter;
import com.example.bughisweeper.ThemeManager;

import java.util.List;

/**
 * Activity for displaying high scores
 */
public class HighScoresActivity extends AppCompatActivity {

    private Spinner spinnerDifficulty;
    private ListView listViewScores;
    private TextView tvNoScores;

    private ThemeManager themeManager;
    private ScoreManager scoreManager;
    private DifficultyManager difficultyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        // Set up back button in action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.high_scores);

        // Initialize managers
        scoreManager = new ScoreManager(this);
        difficultyManager = DifficultyManager.getInstance(this);

        // Initialize views
        initializeViews();
        setupDifficultySpinner();
    }

    /**
     * Initialize view references
     */
    private void initializeViews() {
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        listViewScores = findViewById(R.id.listViewScores);
        tvNoScores = findViewById(R.id.tvNoScores);
    }

    /**
     * Set up difficulty spinner
     */
    private void setupDifficultySpinner() {
        // Create difficulty options
        String[] difficulties = new String[] {
                "All Difficulties",
                getString(R.string.easy),
                getString(R.string.medium),
                getString(R.string.hard),
                getString(R.string.custom)
        };

        // Map display names to difficulty constants
        final String[] difficultyValues = new String[] {
                null, // All difficulties
                DifficultyManager.DIFFICULTY_EASY,
                DifficultyManager.DIFFICULTY_MEDIUM,
                DifficultyManager.DIFFICULTY_HARD,
                DifficultyManager.DIFFICULTY_CUSTOM
        };

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);

        // Set selection listener
        spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadScores(difficultyValues[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Default selection
        spinnerDifficulty.setSelection(0);
    }

    /**
     * Load scores for a specific difficulty
     * @param difficulty Difficulty level (null for all difficulties)
     */
    private void loadScores(String difficulty) {
        // Get scores from database
        List<Score> scores = scoreManager.getHighScores(difficulty, 20);

        if (scores.isEmpty()) {
            // No scores to display
            tvNoScores.setVisibility(View.VISIBLE);
            listViewScores.setVisibility(View.GONE);
        } else {
            // Display scores
            tvNoScores.setVisibility(View.GONE);
            listViewScores.setVisibility(View.VISIBLE);

            // Create adapter
            ScoreAdapter adapter = new ScoreAdapter(this, scores);
            listViewScores.setAdapter(adapter);
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
}