package com.zivalez.createanalyzerlite.util;

/**
 * Math utility functions.
 */
public final class MathUtil {
    
    /**
     * Clamp value between min and max.
     */
    public static float clamp(final float value, final float min, final float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clamp value between min and max (double precision).
     */
    public static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clamp value between min and max (int).
     */
    public static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Linear interpolation between a and b.
     */
    public static float lerp(final float a, final float b, final float t) {
        return a + (b - a) * t;
    }
    
    /**
     * Linear interpolation (double precision).
     */
    public static double lerp(final double a, final double b, final double t) {
        return a + (b - a) * t;
    }
    
    /**
     * Get sign of value (-1, 0, or 1).
     */
    public static int sign(final float value) {
        if (value > 0) return 1;
        if (value < 0) return -1;
        return 0;
    }
    
    /**
     * Get sign of value (double).
     */
    public static int sign(final double value) {
        if (value > 0) return 1;
        if (value < 0) return -1;
        return 0;
    }
    
    /**
     * Check if value is approximately zero.
     */
    public static boolean isZero(final float value, final float epsilon) {
        return Math.abs(value) < epsilon;
    }
    
    /**
     * Check if value is approximately equal to target.
     */
    public static boolean approxEqual(final float a, final float b, final float epsilon) {
        return Math.abs(a - b) < epsilon;
    }
    
    private MathUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
}