package com.zivalez.createanalyzerlite.hud;

import com.zivalez.createanalyzerlite.config.ClientConfig;

public final class LayoutEngine {
    private static final int COMPACT_BASE_WIDTH = 180;
    private static final int EXPANDED_BASE_WIDTH = 240;

    public static int baseWidth(final boolean expanded) {
        return expanded ? EXPANDED_BASE_WIDTH : COMPACT_BASE_WIDTH;
    }

    public static int scaled(final int v, final double scale) {
        return (int) Math.round(v * scale);
    }

    public static int computeX(final ClientConfig.Anchor anchor, final int screenWidth, final int width, final int offsetX, final double scale) {
        final int w = (int) (width * scale);
        return switch (anchor) {
            case TOP_LEFT, BOTTOM_LEFT -> Math.max(0, offsetX);
            case TOP_RIGHT, BOTTOM_RIGHT -> Math.max(0, screenWidth - w - Math.max(0, offsetX));
        };
    }

    public static int computeY(final ClientConfig.Anchor anchor, final int screenHeight, final int height, final int offsetY, final double scale) {
        final int h = (int) (height * scale);
        return switch (anchor) {
            case TOP_LEFT, TOP_RIGHT -> Math.max(0, offsetY);
            case BOTTOM_LEFT, BOTTOM_RIGHT -> Math.max(0, screenHeight - h - Math.max(0, offsetY));
        };
    }

    private LayoutEngine() { }
}
