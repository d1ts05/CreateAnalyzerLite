package com.zivalez.createanalyzerlite.util;

/**
 * Color manipulation utilities for UI rendering.
 */
public final class ColorUtil {
    
    /**
     * Apply alpha to color (ARGB format).
     * 
     * @param rgb RGB color (0xRRGGBB)
     * @param alpha Alpha value (0.0 to 1.0)
     * @return ARGB color with alpha
     */
    public static int withAlpha(final int rgb, final double alpha) {
        final int a = (int) (MathUtil.clamp(alpha, 0.0, 1.0) * 255);
        return (a << 24) | (rgb & 0xFFFFFF);
    }
    
    /**
     * Extract alpha channel from ARGB color.
     */
    public static int getAlpha(final int argb) {
        return (argb >> 24) & 0xFF;
    }
    
    /**
     * Extract red channel.
     */
    public static int getRed(final int argb) {
        return (argb >> 16) & 0xFF;
    }
    
    /**
     * Extract green channel.
     */
    public static int getGreen(final int argb) {
        return (argb >> 8) & 0xFF;
    }
    
    /**
     * Extract blue channel.
     */
    public static int getBlue(final int argb) {
        return argb & 0xFF;
    }
    
    /**
     * Create ARGB color from components.
     * 
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @param a Alpha (0-255)
     * @return ARGB color
     */
    public static int argb(final int r, final int g, final int b, final int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Linear blend between two colors.
     * 
     * @param color1 First color (ARGB)
     * @param color2 Second color (ARGB)
     * @param t Blend factor (0.0 to 1.0)
     * @return Blended color
     */
    public static int blend(final int color1, final int color2, final float t) {
        final float t1 = 1.0f - t;
        
        final int a = (int) (getAlpha(color1) * t1 + getAlpha(color2) * t);
        final int r = (int) (getRed(color1) * t1 + getRed(color2) * t);
        final int g = (int) (getGreen(color1) * t1 + getGreen(color2) * t);
        final int b = (int) (getBlue(color1) * t1 + getBlue(color2) * t);
        
        return argb(r, g, b, a);
    }
    
    /**
     * Darken color by factor.
     * 
     * @param color Original color
     * @param factor Darken factor (0.0 to 1.0, where 0 = black)
     * @return Darkened color
     */
    public static int darken(final int color, final float factor) {
        final float f = MathUtil.clamp(factor, 0.0f, 1.0f);
        final int a = getAlpha(color);
        final int r = (int) (getRed(color) * f);
        final int g = (int) (getGreen(color) * f);
        final int b = (int) (getBlue(color) * f);
        return argb(r, g, b, a);
    }
    
    /**
     * Lighten color by factor.
     * 
     * @param color Original color
     * @param factor Lighten factor (0.0 to 1.0, where 1 = white)
     * @return Lightened color
     */
    public static int lighten(final int color, final float factor) {
        final float f = MathUtil.clamp(factor, 0.0f, 1.0f);
        final int a = getAlpha(color);
        final int r = (int) (getRed(color) + (255 - getRed(color)) * f);
        final int g = (int) (getGreen(color) + (255 - getGreen(color)) * f);
        final int b = (int) (getBlue(color) + (255 - getBlue(color)) * f);
        return argb(r, g, b, a);
    }
    
    private ColorUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
}