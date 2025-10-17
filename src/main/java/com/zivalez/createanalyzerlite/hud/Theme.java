package com.zivalez.createanalyzerlite.hud;

import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.config.ConfigData;
import com.zivalez.createanalyzerlite.util.ColorUtil;

/**
 * Theme system providing color palettes for overlay rendering.
 * <p>
 * Supports Light, Dark, and Auto (system preference) themes.
 */
public sealed interface Theme permits Theme.LightTheme, Theme.DarkTheme {
    
    // === Color Getters ===
    
    int backgroundColor();
    int titleColor();
    int textColor();
    int mutedColor();
    int valueColor();
    int accentColor();
    
    int stressSafeColor();
    int stressWarningColor();
    int stressDangerColor();
    
    // === Factory ===
    
    /**
     * Get theme from configuration.
     */
    static Theme fromConfig(final ConfigData config) {
        return switch (config.theme()) {
            case LIGHT -> new LightTheme(config.opacity());
            case DARK -> new DarkTheme(config.opacity());
            case AUTO -> detectSystemTheme(config.opacity());
        };
    }
    
    /**
     * Detect system theme preference.
     * <p>
     * Falls back to Dark theme (safer for most UIs).
     */
    private static Theme detectSystemTheme(final double opacity) {
        // TODO: Implement system theme detection via OS API
        // For now, default to dark
        return new DarkTheme(opacity);
    }
    
    // === Implementations ===
    
    /**
     * Light theme with bright background and dark text.
     */
    final class LightTheme implements Theme {
        private final double opacity;
        
        public LightTheme(final double opacity) {
            this.opacity = opacity;
        }
        
        @Override
        public int backgroundColor() {
            return ColorUtil.withAlpha(0xF0F0F0, opacity);
        }
        
        @Override
        public int titleColor() {
            return 0xFF2C3E50; // Dark blue-gray
        }
        
        @Override
        public int textColor() {
            return 0xFF34495E; // Medium dark gray
        }
        
        @Override
        public int mutedColor() {
            return 0xFF7F8C8D; // Light gray
        }
        
        @Override
        public int valueColor() {
            return 0xFF2980B9; // Blue
        }
        
        @Override
        public int accentColor() {
            return 0xFF8E44AD; // Purple
        }
        
        @Override
        public int stressSafeColor() {
            return 0xFF27AE60; // Green
        }
        
        @Override
        public int stressWarningColor() {
            return 0xFFF39C12; // Orange
        }
        
        @Override
        public int stressDangerColor() {
            return 0xFFE74C3C; // Red
        }
    }
    
    /**
     * Dark theme with dark background and light text.
     */
    final class DarkTheme implements Theme {
        private final double opacity;
        
        public DarkTheme(final double opacity) {
            this.opacity = opacity;
        }
        
        @Override
        public int backgroundColor() {
            return ColorUtil.withAlpha(0x1E1E1E, opacity);
        }
        
        @Override
        public int titleColor() {
            return 0xFFECF0F1; // Almost white
        }
        
        @Override
        public int textColor() {
            return 0xFFBDC3C7; // Light gray
        }
        
        @Override
        public int mutedColor() {
            return 0xFF7F8C8D; // Medium gray
        }
        
        @Override
        public int valueColor() {
            return 0xFF3498DB; // Bright blue
        }
        
        @Override
        public int accentColor() {
            return 0xFF9B59B6; // Light purple
        }
        
        @Override
        public int stressSafeColor() {
            return 0xFF2ECC71; // Bright green
        }
        
        @Override
        public int stressWarningColor() {
            return 0xFFF1C40F; // Bright yellow
        }
        
        @Override
        public int stressDangerColor() {
            return 0xFFE74C3C; // Bright red
        }
    }
}