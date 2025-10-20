package com.zivalez.createanalyzerlite.hud;

import com.zivalez.createanalyzerlite.integration.create.KineticData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class Widgets {

    public static void panel(final GuiGraphics gfx, final int x, final int y, final int w, final int h, final Theme theme, final int bgAlpha) {
        gfx.fill(x, y, x + w, y + h, theme.panelBg(bgAlpha));
        // border 1px
        final int b = theme.panelBorder();
        gfx.fill(x, y, x + w, y + 1, b);
        gfx.fill(x, y + h - 1, x + w, y + h, b);
        gfx.fill(x, y, x + 1, y + h, b);
        gfx.fill(x + w - 1, y, x + w, y + h, b);
    }

    public static void text(final GuiGraphics gfx, final String s, final int x, final int y, final int color) {
        gfx.drawString(Minecraft.getInstance().font, s, x, y, color, false);
    }

    public static void badge(final GuiGraphics gfx, final String s, final int x, final int y, final Theme theme) {
        final var font = Minecraft.getInstance().font;
        final int tw = font.width(s);
        final int padX = 4, padY = 2;
        final int w = tw + padX * 2;
        final int h = font.lineHeight + padY * 2;
        final int bg = (0x80 << 24) | (theme.textSecondary() & 0x00FFFFFF);
        gfx.fill(x, y, x + w, y + h, bg);
        gfx.drawString(font, s, x + padX, y + padY, theme.textPrimary(), false);
    }

    public static void stressBar(final GuiGraphics gfx, final int x, final int y, final int w, final int h, final KineticData kd, final Theme theme) {
        gfx.fill(x, y, x + w, y + h, 0x40000000); // track
        final double r = Math.max(0.0, kd.stressRatio());
        final int fill = (int) Math.round(Math.min(1.0, r) * w);
        final int col = r < 0.7 ? theme.stressSafe() : (r < 0.9 ? theme.stressWarn() : theme.stressDanger());
        gfx.fill(x, y, x + fill, y + h, col);
    }

    /** One-line compact row: RPM · mini bar · Nodes [+ badges]. */
    public static int compactRow(final GuiGraphics gfx, final int x, final int y, final int w, final Theme theme, final KineticData kd) {
        final var font = Minecraft.getInstance().font;
        int cx = x, cy = y;

        final String rpm = String.format("RPM %s%d", kd.speed() < 0 ? "-" : "", Math.abs((int) kd.speed()));
        text(gfx, rpm, cx, cy, theme.textPrimary());
        cx += font.width(rpm) + 8;

        final int barW = Math.max(40, w / 3);
        stressBar(gfx, cx, cy + 2, barW, 6, kd, theme);
        cx += barW + 8;

        final String nodes = "Nodes " + kd.nodes();
        text(gfx, nodes, cx, cy, theme.textSecondary());
        cx += font.width(nodes) + 6;

        if (kd.stressApproximate() || kd.nodesApproximate()) {
            badge(gfx, "≈", cx, cy - 2, theme);
            cx += font.width("≈") + 12;
        }
        // lock icon text will be drawn by OverlayRenderer when needed
        return font.lineHeight + 2;
    }

    private Widgets() { }
}
