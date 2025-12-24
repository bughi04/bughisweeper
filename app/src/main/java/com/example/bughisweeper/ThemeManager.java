package com.example.bughisweeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.bughisweeper.R;

/**
 * Manages the themes for the Bughisweeper game.
 * Handles theme switching and provides themed resources.
 */
public class ThemeManager {

    // Theme constants
    public static final String THEME_CLASSIC = "classic";
    public static final String THEME_DARK = "dark";
    public static final String THEME_FOREST = "forest";
    public static final String THEME_OCEAN = "ocean";
    public static final String THEME_SPACE = "space";

    // Shared preferences
    private static final String PREFS_NAME = "bughisweeper_prefs";
    private static final String PREF_THEME = "theme";

    // Default theme
    private static final String DEFAULT_THEME = THEME_CLASSIC;

    private final Context context;
    private final SharedPreferences prefs;
    private String currentTheme;

    // Singleton instance
    private static ThemeManager instance;

    /**
     * Get the singleton instance of ThemeManager
     * @param context Application context
     * @return ThemeManager instance
     */
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Private constructor to prevent direct instantiation
     * @param context Application context
     */
    private ThemeManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentTheme = prefs.getString(PREF_THEME, DEFAULT_THEME);
    }

    /**
     * Get the current theme
     * @return Current theme name
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Set the current theme and save to preferences
     * @param themeName Name of the theme to set
     */
    public void setTheme(String themeName) {
        if (!isValidTheme(themeName)) {
            themeName = DEFAULT_THEME;
        }

        currentTheme = themeName;

        // Save to preferences
        prefs.edit().putString(PREF_THEME, themeName).apply();

        // Apply night mode for dark theme
        if (THEME_DARK.equals(themeName)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Apply the current theme to an activity
     * @param context The activity context
     */
    public void applyTheme(Context context) {
        switch (currentTheme) {
            case THEME_DARK:
                context.setTheme(R.style.Theme_Bughisweeper_Dark);
                break;
            case THEME_FOREST:
                context.setTheme(R.style.Theme_Bughisweeper_Forest);
                break;
            case THEME_OCEAN:
                context.setTheme(R.style.Theme_Bughisweeper_Ocean);
                break;
            case THEME_SPACE:
                context.setTheme(R.style.Theme_Bughisweeper_Space);
                break;
            case THEME_CLASSIC:
            default:
                context.setTheme(R.style.Theme_Bughisweeper_Classic);
                break;
        }
    }

    /**
     * Check if the given theme name is valid
     * @param themeName Name of the theme to check
     * @return True if valid, false otherwise
     */
    public boolean isValidTheme(String themeName) {
        return THEME_CLASSIC.equals(themeName) ||
                THEME_DARK.equals(themeName) ||
                THEME_FOREST.equals(themeName) ||
                THEME_OCEAN.equals(themeName) ||
                THEME_SPACE.equals(themeName);
    }

    /**
     * Get all available theme names
     * @return Array of theme names
     */
    public String[] getAvailableThemes() {
        return new String[] {
                THEME_CLASSIC,
                THEME_DARK,
                THEME_FOREST,
                THEME_OCEAN,
                THEME_SPACE
        };
    }

    /**
     * Get the display name for a theme
     * @param themeName Theme name
     * @return Display name resource ID
     */
    public int getThemeDisplayNameResId(String themeName) {
        switch (themeName) {
            case THEME_DARK:
                return R.string.theme_dark;
            case THEME_FOREST:
                return R.string.theme_forest;
            case THEME_OCEAN:
                return R.string.theme_ocean;
            case THEME_SPACE:
                return R.string.theme_space;
            case THEME_CLASSIC:
            default:
                return R.string.theme_classic;
        }
    }

    /**
     * Get a color from the current theme
     * @param colorType Type of color to get (e.g., "primary", "background")
     * @return Color resource ID
     */
    @ColorRes
    public int getThemeColorRes(String colorType) {
        String prefix = currentTheme + "_";

        switch (colorType) {
            case "primary":
                return getResourceId("color", prefix + "primary");
            case "primary_dark":
                return getResourceId("color", prefix + "primary_dark");
            case "accent":
                return getResourceId("color", prefix + "accent");
            case "background":
                return getResourceId("color", prefix + "background");
            case "cell_revealed":
                return getResourceId("color", prefix + "cell_revealed");
            case "cell_unrevealed":
                return getResourceId("color", prefix + "cell_unrevealed");
            case "bug":
                return getResourceId("color", prefix + "bug");
            case "flag":
                return getResourceId("color", prefix + "flag");
            default:
                // Fallback to classic theme
                return getResourceId("color", "classic_" + colorType);
        }
    }

    /**
     * Get a drawable resource from the current theme
     * @param drawableType Type of drawable to get (e.g., "bug", "flag")
     * @return Drawable resource ID
     */
    @DrawableRes
    public int getThemeDrawableRes(String drawableType) {
        return getResourceId("drawable", drawableType + "_" + currentTheme);
    }

    /**
     * Get a themed drawable
     * @param drawableType Type of drawable to get
     * @return Drawable object
     */
    public Drawable getThemeDrawable(String drawableType) {
        int resId = getThemeDrawableRes(drawableType);
        return ContextCompat.getDrawable(context, resId);
    }

    /**
     * Get a themed color
     * @param colorType Type of color to get
     * @return Color int value
     */
    public int getThemeColor(String colorType) {
        int resId = getThemeColorRes(colorType);
        return ContextCompat.getColor(context, resId);
    }

    /**
     * Get a resource ID by name and type
     * @param resType Resource type (e.g., "drawable", "color")
     * @param resName Resource name
     * @return Resource ID
     */
    private int getResourceId(String resType, String resName) {
        Resources res = context.getResources();
        return res.getIdentifier(resName, resType, context.getPackageName());
    }
}