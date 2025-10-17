package com.zivalez.createanalyzerlite.hud;

import com.zivalez.createanalyzerlite.config.ConfigData;
import com.zivalez.createanalyzerlite.integration.create.KineticData;
import com.zivalez.createanalyzerlite.util.ColorUtil;
import com.zivalez.createanalyzerlite.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Reusable UI widget components for overlay rendering.
 * <p>
 * Provides methods for drawing panels, bars, badges, icons, and text
 * with consistent styling.
 */
public final class Widgets {
    
    /**
     * Draw background panel with rounded corners.
     */
    public static void drawPanel(
        final GuiGraphics gfx,
        final int x,
        final int y,
        final int width,
        final int height,
        final Theme theme,
        final ConfigData config
    ) {
        // Background
        gfx.fill(x, y, x + width, y + height, theme.backgroundColor());
        
        // Border (optional, subtle)
        final int borderColor = ColorUtil.withAlpha(theme.textColor(), 0.2);
        gfx.fill(x, y, x + width, y + 1, borderColor); // Top
        gfx.fill(x, y + height - 1, x + width, y + height, borderColor); // Bottom
        gfx.fill(x, y, x + 1, y + height, borderColor); // Left
        gfx.fill(x + width - 1, y, x + width, y + height, borderColor); // Right
    }
    
    /**
     * Draw stress progress bar with color coding.
     */
    public static void drawStressBar(
        final GuiGraphics gfx,
        final int x,
        final int y,
        final int width,
        final int height,
        final KineticData data,
        final Theme theme
    ) {
        final float ratio = data.stressCapacity() > 0 
            ? (float) (data.stressConsumption() / data.stressCapacity())
            : 0.0f;
        
        final float clampedRatio = MathUtil.clamp(ratio, 0.0f, 1.0f);
        final int fillWidth = (int) (width * clampedRatio);
        
        // Background (empty bar)
        final int bgColor = ColorUtil.withAlpha(theme.textColor(), 0.2);
        gfx.fill(x, y, x + width, y + height, bgColor);
        
        // Foreground (filled bar)
        final int fillColor = getStressColor(clampedRatio, theme);
        if (fillWidth > 0) {
            gfx.fill(x, y, x + fillWidth, y + height, fillColor);
        }
    }
    
    /**
     * Get stress bar color based on load ratio.
     */
    private static int getStressColor(final float ratio, final Theme theme) {
        if (ratio < 0.7f) {
            return theme.stressSafeColor(); // Green
        } else if (ratio < 0.9f) {
            return theme.stressWarningColor(); // Yellow/Orange
        } else {
            return theme.stressDangerColor(); // Red
        }
    }
    
    /**
     * Draw text with font.
     */
    public static void drawText(
        final GuiGraphics gfx,
        final String text,
        final int x,
        final int y,
        final int color
    ) {
        final Minecraft mc = Minecraft.getInstance();
        gfx.drawString(mc.font, text, x, y, color, false);
    }
    
    /**
     * Draw label (muted text for field names).
     */
    public static void drawLabel(
        final GuiGraphics gfx,
        final String label,
        final int x,
        final int y,
        final Theme theme
    ) {
        drawText(gfx, label, x, y, theme.mutedColor());
    }
    
    /**
     * Draw badge (small rounded rectangle with text).
     */
    public static void drawBadge(
        final GuiGraphics gfx,
        final String text,
        final int x,
        final int y,
        final int bgColor,
        final int textColor
    ) {
        final Minecraft mc = Minecraft.getInstance();
        final int textWidth = mc.font.width(text);
        final int badgeWidth = textWidth + 8;
        final int badgeHeight = 12;
        
        // Background
        gfx.fill(x, y, x + badgeWidth, y + badgeHeight, bgColor);
        
        // Text
        gfx.drawString(mc.font, text, x + 4, y + 2, textColor, false);
    }
    
    /**
     * Draw icon (simple symbol/emoji).
     */
    public static void drawIcon(
        final GuiGraphics gfx,
        final String icon,
        final int x,
        final int y,
        final int color
    ) {
        drawText(gfx, icon, x, y, color);
    }
    
    private Widgets() {
        throw new UnsupportedOperationException("Utility class");
    }
}