package com.zivalez.createanalyzerlite.hud;

import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.config.ConfigData;

/**
 * Layout calculation engine for overlay positioning.
 * <p>
 * Handles anchor points, offsets, and scaling to position the overlay
 * correctly on screen based on configuration.
 */
public final class LayoutEngine {
    
    private static final int COMPACT_BASE_WIDTH = 180;
    private static final int EXPANDED_BASE_WIDTH = 220;
    
    /**
     * Layout result with calculated position and dimensions.
     */
    public record Layout(int x, int y, int width, int height) {}
    
    /**
     * Calculate layout for overlay based on config and screen dimensions.
     * 
     * @param config Configuration data
     * @param screenWidth Screen width in scaled pixels
     * @param screenHeight Screen height in scaled pixels
     * @param mode Display mode
     * @return Calculated layout
     */
    public static Layout calculate(
        final ConfigData config,
        final int screenWidth,
        final int screenHeight,
        final ClientConfig.DisplayMode mode
    ) {
        // Base dimensions (before scaling)
        final int baseWidth = mode == ClientConfig.DisplayMode.COMPACT 
            ? COMPACT_BASE_WIDTH 
            : EXPANDED_BASE_WIDTH;
        
        // Apply scale (note: actual scaling happens in render transform)
        final int width = baseWidth;
        final int height = 100; // Dynamic based on content
        
        // Calculate anchor position
        final int anchorX = calculateAnchorX(config.anchor(), screenWidth, width, config.scale());
        final int anchorY = calculateAnchorY(config.anchor(), screenHeight, height, config.scale());
        
        // Apply offsets
        final int finalX = anchorX + config.offsetX();
        final int finalY = anchorY + config.offsetY();
        
        return new Layout(finalX, finalY, width, height);
    }
    
    /**
     * Calculate X position based on anchor.
     */
    private static int calculateAnchorX(
        final ClientConfig.Anchor anchor,
        final int screenWidth,
        final int width,
        final double scale
    ) {
        return switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> 0;
            case TOP_RIGHT, BOTTOM_RIGHT -> (int) (screenWidth - width * scale);
        };
    }
    
    /**
     * Calculate Y position based on anchor.
     */
    private static int calculateAnchorY(
        final ClientConfig.Anchor anchor,
        final int screenHeight,
        final int height,
        final double scale
    ) {
        return switch (anchor) {
            case TOP_LEFT, TOP_RIGHT -> 0;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> (int) (screenHeight - height * scale);
        };
    }
    
    private LayoutEngine() {
        throw new UnsupportedOperationException("Utility class");
    }
}