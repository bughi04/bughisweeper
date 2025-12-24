package com.example.bughisweeper;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for playing tutorial videos
 */
public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private ProgressBar progressBar;
    private TextView tvVideoTitle;
    private TextView tvVideoDescription;
    private Button btnFullscreen;
    private Button btnReplay;

    private String videoTitle;
    private String videoDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Apply theme safely
        try {
            ThemeManager themeManager = ThemeManager.getInstance(this);
            themeManager.applyTheme(this);
        } catch (Exception e) {
            // Theme failed - continue anyway
        }

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("📹 Tutorial Video");
        }

        // Get video info from intent
        extractVideoInfo();

        // Initialize views
        initializeViews();
        setupVideoPlayer();
    }

    private void extractVideoInfo() {
        Intent intent = getIntent();
        if (intent != null) {
            videoTitle = intent.getStringExtra("video_title");
            videoDescription = intent.getStringExtra("video_description");

            // Set defaults if not provided
            if (videoTitle == null) videoTitle = "Demo Video";
            if (videoDescription == null) videoDescription = "Learn how to play Bughisweeper!";
        }
    }

    private void initializeViews() {
        // Find views
        videoView = findViewById(R.id.videoView);
        progressBar = findViewById(R.id.progressBar);
        tvVideoTitle = findViewById(R.id.tvVideoTitle);
        tvVideoDescription = findViewById(R.id.tvVideoDescription);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        btnReplay = findViewById(R.id.btnReplay);

        // Set video info
        tvVideoTitle.setText(videoTitle);
        tvVideoDescription.setText(videoDescription);

        // Set up button listeners
        if (btnReplay != null) {
            btnReplay.setOnClickListener(v -> replayVideo());
        }

        if (btnFullscreen != null) {
            btnFullscreen.setOnClickListener(v -> toggleFullscreen());
        }
    }

    private void setupVideoPlayer() {
        try {
            // Create URI for the video resource
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.demo_video);

            // Set video URI
            videoView.setVideoURI(videoUri);

            // Create and set media controller
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            // Set up listeners
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (btnReplay != null) {
                        btnReplay.setVisibility(View.VISIBLE);
                    }
                    if (btnFullscreen != null) {
                        btnFullscreen.setVisibility(View.VISIBLE);
                    }

                    // Auto-start playback
                    videoView.start();

                    Toast.makeText(VideoPlayerActivity.this, "📹 Video ready! Tap to play/pause", Toast.LENGTH_SHORT).show();
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(VideoPlayerActivity.this, "✅ Video completed!", Toast.LENGTH_SHORT).show();
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(VideoPlayerActivity.this, "❌ Error playing video", Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            // Show loading
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Failed to load video: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void replayVideo() {
        try {
            if (videoView != null) {
                videoView.seekTo(0);
                videoView.start();
                Toast.makeText(this, "🔄 Replaying video", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to replay video", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFullscreen() {
        try {
            // Simple fullscreen toggle
            View decorView = getWindow().getDecorView();
            int uiOptions = decorView.getSystemUiVisibility();

            if ((uiOptions & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // Enter fullscreen
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
                if (btnFullscreen != null) {
                    btnFullscreen.setText("Exit Fullscreen");
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            } else {
                // Exit fullscreen
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                if (btnFullscreen != null) {
                    btnFullscreen.setText("📺 Fullscreen");
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Fullscreen toggle failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}