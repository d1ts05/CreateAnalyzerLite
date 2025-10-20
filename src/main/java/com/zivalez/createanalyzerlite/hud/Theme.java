package com.zivalez.createanalyzerlite.hud;

import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.config.ConfigData;

/** Theme palette for overlay rendering. */
public sealed interface Theme permits Theme.DarkTheme, Theme.LightTheme {

    int panelBg(int alpha);
    int panelBorder();
    int textPrimary();
    int textSecondary();
    int accent();
    int stressSafe();
    int stressWarn();
    int stressDanger();

    /** Resolve current theme from config (AUTO → Dark for now). */
    static Theme resolve(final ConfigData cfg) {
        return switch (cfg.theme()) {
            case DARK -> new DarkTheme();
            case LIGHT -> new LightTheme();
            case AUTO -> new DarkTheme();
        };
    }

    /** Minimal dark with blue accent (#3498DB). */
    final class DarkTheme implements Theme {
        // base dark: #2C3E50
        private static final int BASE = 0xFF2C3E50;
        private static final int BORDER = 0x40FFFFFF;
        private static final int TEXT = 0xFFECEFF4;
        private static final int TEXT_MUTED = 0xFFB0BEC5;
        private static final int ACCENT = 0xFF3498DB;

        private static final int SAFE = 0xFF2ECC71;
        private static final int WARN = 0xFFF1C40F;
        private static final int DANGER = 0xFFE74C3C;

        @Override public int panelBg(int alpha) { return (alpha & 0xFF) << 24 | (BASE & 0x00FFFFFF); }
        @Override public int panelBorder() { return BORDER; }
        @Override public int textPrimary() { return TEXT; }
        @Override public int textSecondary() { return TEXT_MUTED; }
        @Override public int accent() { return ACCENT; }
        @Override public int stressSafe() { return SAFE; }
        @Override public int stressWarn() { return WARN; }
        @Override public int stressDanger() { return DANGER; }
    }

    /** Light variant to keep parity. */
    final class LightTheme implements Theme {
        private static final int BASE = 0xFFF7FAFC;
        private static final int BORDER = 0x19000000;
        private static final int TEXT = 0xFF1F2937;
        private static final int TEXT_MUTED = 0xFF6B7280;
        private static final int ACCENT = 0xFF3498DB;

        private static final int SAFE = 0xFF27AE60;
        private static final int WARN = 0xFFF39C12;
        private static final int DANGER = 0xFFC0392B;

        @Override public int panelBg(int alpha) { return (alpha & 0xFF) << 24 | (BASE & 0x00FFFFFF); }
        @Override public int panelBorder() { return BORDER; }
        @Override public int textPrimary() { return TEXT; }
        @Override public int textSecondary() { return TEXT_MUTED; }
        @Override public int accent() { return ACCENT; }
        @Override public int stressSafe() { return SAFE; }
        @Override public int stressWarn() { return WARN; }
        @Override public int stressDanger() { return DANGER; }
    }
}
