package com.example.bughisweeper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bughisweeper.R;
import com.example.bughisweeper.Score;

import java.util.List;

/**
 * Adapter for displaying scores in a ListView
 */
public class ScoreAdapter extends ArrayAdapter<Score> {

    private final Context context;
    private final List<Score> scores;

    public ScoreAdapter(Context context, List<Score> scores) {
        super(context, R.layout.item_high_score, scores);
        this.context = context;
        this.scores = scores;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_high_score, parent, false);

            holder = new ViewHolder();
            holder.tvRank = convertView.findViewById(R.id.tvRank);
            holder.tvPlayerName = convertView.findViewById(R.id.tvPlayerName);
            holder.tvDifficulty = convertView.findViewById(R.id.tvDifficulty);
            holder.tvTime = convertView.findViewById(R.id.tvTime);
            holder.tvDate = convertView.findViewById(R.id.tvDate);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get score at position
        Score score = scores.get(position);

        // Set values
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvPlayerName.setText(score.getPlayerName());
        holder.tvDifficulty.setText(score.getFormattedDifficulty());
        holder.tvTime.setText(score.getFormattedTime());
        holder.tvDate.setText(formatDate(score.getDate()));

        // Highlight winning scores
        if (score.isGridCleared()) {
            convertView.setBackgroundResource(R.drawable.bg_high_score_win);
        } else {
            convertView.setBackgroundResource(0);
        }

        return convertView;
    }

    /**
     * Format date string for display
     * @param dateString Date string from database
     * @return Formatted date
     */
    private String formatDate(String dateString) {
        // Simple format for display
        // In a real app, we would parse the date and format it properly
        if (dateString != null && dateString.length() > 10) {
            return dateString.substring(0, 10);
        }
        return dateString;
    }

    /**
     * ViewHolder pattern for better performance
     */
    private static class ViewHolder {
        TextView tvRank;
        TextView tvPlayerName;
        TextView tvDifficulty;
        TextView tvTime;
        TextView tvDate;
    }
}