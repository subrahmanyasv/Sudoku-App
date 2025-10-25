// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/utils/ProfileColorUtil.java
package com.example.sudoku.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.content.res.TypedArray;
import com.example.sudoku.R;

import androidx.core.content.ContextCompat;

import com.example.sudoku.R; // Ensure this import is correct for your R file

public class ProfileColorUtil {

    private static final int[] PROFILE_COLORS = {
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#CDDC39"), // Lime
            Color.parseColor("#FF5722")  // Deep Orange
    };

    /**
     * Generates a circular Drawable with a unique background color based on the initial character.
     *
     * @param context The application context.
     * @param initial The first character of the username.
     * @return A Drawable for the profile picture background.
     */
    public static Drawable getProfileDrawable(Context context, char initial) {
        int colorIndex = Character.toUpperCase(initial) % PROFILE_COLORS.length;
        int backgroundColor = PROFILE_COLORS[colorIndex];

        // Create a circular shape drawable for the background color
        GradientDrawable backgroundCircle = new GradientDrawable();
        backgroundCircle.setShape(GradientDrawable.OVAL);
        backgroundCircle.setColor(backgroundColor);

        // Load the base circle_profile_background.xml drawable (which defines shape and default border)
        // We'll replace its default solid color with our dynamic color
        Drawable baseDrawable = ContextCompat.getDrawable(context, R.drawable.circle_profile_background);

        if (baseDrawable instanceof GradientDrawable) {
            // If it's a simple shape drawable, we can directly set its color
            ((GradientDrawable) baseDrawable).setColor(backgroundColor);
            return baseDrawable;
        } else if (baseDrawable instanceof LayerDrawable) {
            // If it's a layered drawable, iterate and find the one to color
            LayerDrawable layerDrawable = (LayerDrawable) baseDrawable;
            for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
                Drawable layer = layerDrawable.getDrawable(i);
                if (layer instanceof GradientDrawable) {
                    // Assuming the first GradientDrawable is the background to color
                    ((GradientDrawable) layer).setColor(backgroundColor);
                    return layerDrawable;
                }
            }
        }

        // Fallback: If for some reason circle_profile_background.xml isn't a GradientDrawable
        // or LayerDrawable, just return the simple colored circle.
        return backgroundCircle;
    }

    public static void setProfileColor(TextView textView, String initial) {
        if (textView == null || initial == null || initial.isEmpty()) {
            return;
        }

        Context context = textView.getContext();
        TypedArray colors = context.getResources().obtainTypedArray(R.array.profile_colors_array);
        int charCode = initial.toUpperCase().charAt(0);
        int colorIndex = (charCode % colors.length()); // Simple hash based on first letter
        int backgroundColor = colors.getColor(colorIndex, ContextCompat.getColor(context, R.color.accent_blue)); // Fallback color
        colors.recycle();

        // Get the circular background drawable
        Drawable background = ContextCompat.getDrawable(context, R.drawable.circle_profile_background);
        if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background.mutate(); // Use mutate() to avoid affecting other instances
            gradientDrawable.setColor(backgroundColor);
            textView.setBackground(gradientDrawable);
        } else {
            // Fallback if the drawable isn't a GradientDrawable (e.g., set solid color)
            // This part might need adjustment based on your exact drawable type if not GradientDrawable
            try {
                // Attempt to apply color filter if it's not a shape drawable
                background.setColorFilter(backgroundColor, android.graphics.PorterDuff.Mode.SRC_IN);
                textView.setBackground(background);
            } catch (Exception e) {
                // Last resort: set background color directly (might lose shape)
                textView.setBackgroundColor(backgroundColor);
            }
        }
    }
}

