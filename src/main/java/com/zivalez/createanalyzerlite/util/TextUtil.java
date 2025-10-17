package com.zivalez.createanalyzerlite.util;

/**
 * Text formatting utilities for overlay UI.
 */
public final class TextUtil {
    
    /**
     * Format number with K/M/B suffixes.
     * 
     * @param value Number to format
     * @return Formatted string (e.g., "1.5K", "2.3M")
     */
    public static String formatCompact(final double value) {
        final double abs = Math.abs(value);
        
        if (abs >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000);
        } else if (abs >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000);
        } else if (abs >= 1_000) {
            return String.format("%.1fK", value / 1_000);
        } else {
            return String.format("%.0f", value);
        }
    }
    
    /**
     * Format RPM with sign indicator.
     * 
     * @param rpm RPM value (negative = counter-clockwise)
     * @return Formatted string with direction symbol
     */
    public static String formatRPM(final float rpm) {
        final String direction = rpm >= 0 ? "⟳" : "⟲";
        return String.format("%.1f RPM %s", Math.abs(rpm), direction);
    }
    
    /**
     * Format percentage.
     */
    public static String formatPercent(final double value) {
        return String.format("%.0f%%", value * 100);
    }
    
    /**
     * Format decimal with specified precision.
     */
    public static String formatDecimal(final double value, final int decimals) {
        return String.format("%%.%df".formatted(decimals), value);
    }
    
    /**
     * Truncate text to max length with ellipsis.
     */
    public static String truncate(final String text, final int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Pad text to fixed width (right-aligned).
     */
    public static String padLeft(final String text, final int width) {
        return String.format("%" + width + "s", text);
    }
    
    /**
     * Pad text to fixed width (left-aligned).
     */
    public static String padRight(final String text, final int width) {
        return String.format("%-" + width + "s", text);
    }
    
    private TextUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
}