package com.zivalez.createanalyzerlite.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zivalez.createanalyzerlite.CreateAnalyzerLite;
import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.config.ConfigData;
import com.zivalez.createanalyzerlite.integration.create.CreatePresent;
import com.zivalez.createanalyzerlite.integration.create.KineticData;
import com.zivalez.createanalyzerlite.integration.create.KineticQuery;
import com.zivalez.createanalyzerlite.probe.TargetSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;

/**
 * Overlay renderer (UI-only).
 * - Uses existing helpers: Theme.resolve, Widgets.panel/text/stressBar/badge, LayoutEngine.*.
 * - Default mode follows config (set default to EXPANDED in ClientConfig).
 * - Compact: adaptive width (no overflow), dynamic panel height.
 * - Expanded: adds Load% and clearer capacity line.
 */
public final class OverlayRenderer {

    private static boolean overlayEnabled = true;

    @Nullable
    private static ClientConfig.DisplayMode currentMode = null;

    @Nullable
    private static BlockPos lockedTarget = null;

    // Simple throttle snapshot
    private static long lastSampleTick = 0L;
    @Nullable
    private static KineticData lastData = null;

    @SubscribeEvent
    public static void onRenderGui(final RenderGuiEvent.Post evt) {
        if (!overlayEnabled) return;
        if (!CreatePresent.isLoaded()) return;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        final ConfigData cfg = ConfigData.fromSpec();
        if (cfg.hideInMenus() && mc.screen != null) return;
        if (!cfg.hasAnyContent()) return;

        if (currentMode == null) {
            currentMode = cfg.defaultDisplayMode(); // default EXPANDED via ClientConfig
        }

        final BlockEntity target = (lockedTarget != null)
            ? mc.level.getBlockEntity(lockedTarget)
            : TargetSelector.getTargetedKineticBlock(mc);

        if (target == null) return;

        final KineticData kd = queryKineticData(target, mc, cfg);
        if (kd == null) return;

        renderOverlay(evt.getGuiGraphics(), kd, cfg, mc);
    }

    @Nullable
    private static KineticData queryKineticData(final BlockEntity target, final Minecraft mc, final ConfigData cfg) {
        final long now = mc.level.getGameTime();
        if (lastData != null && (now - lastSampleTick) < cfg.sampleEveryTicks()) return lastData;

        final KineticData fresh = KineticQuery.query(target, cfg);
        lastData = fresh;
        lastSampleTick = now;
        return fresh;
    }

    private static void renderOverlay(final GuiGraphics gfx, final KineticData kd, final ConfigData cfg, final Minecraft mc) {
        final PoseStack pose = gfx.pose();
        pose.pushPose();
        try {
            final Theme theme = Theme.resolve(cfg);
            final boolean expanded = (currentMode == ClientConfig.DisplayMode.EXPANDED);
            final int baseW = LayoutEngine.baseWidth(expanded);
            final int pad = cfg.padding();

            final int screenW = mc.getWindow().getGuiScaledWidth();
            final int screenH = mc.getWindow().getGuiScaledHeight();

            // --- dynamic panel height (avoid overflow) ---
            final int panelH = expanded ? calcExpandedHeight(pad) : calcCompactHeight(pad);

            final int x = LayoutEngine.computeX(cfg.anchor(), screenW, baseW, cfg.offsetX(), cfg.scale());
            final int y = LayoutEngine.computeY(cfg.anchor(), screenH, panelH, cfg.offsetY(), cfg.scale());

            pose.translate(x, y, 0);
            pose.scale((float) cfg.scale(), (float) cfg.scale(), 1.0f);

            Widgets.panel(gfx, 0, 0, baseW, panelH, theme, cfg.opacity());

            if (expanded) {
                drawExpanded(gfx, kd, theme, baseW, pad);
            } else {
                drawCompact(gfx, kd, theme, baseW, pad);
            }
        } finally {
            pose.popPose();
        }
    }

    // ====== sizing helpers ======

    private static int calcCompactHeight(final int pad) {
        final int lh = Minecraft.getInstance().font.lineHeight; // text height
        return Math.max(18, lh + pad * 2); // at least 18
    }

    private static int calcExpandedHeight(final int pad) {
        final int lh = Minecraft.getInstance().font.lineHeight;
        // header (lh) + gap(2) + bar(8) + gap(6) + row(lh) + pad*2
        return lh + 2 + 8 + 6 + lh + pad * 2;
    }

    // ====== COMPACT ======

    private static void drawCompact(final GuiGraphics gfx, final KineticData kd, final Theme theme, final int width, final int pad) {
        final var font = Minecraft.getInstance().font;
        int x = pad;
        final int y = pad - 1;
        final int contentW = width - pad * 2;

        // text: RPM
        final int rpmAbs = Math.abs((int) kd.speed());
        final String rpm = "RPM " + (kd.speed() < 0 ? "-" : "") + rpmAbs;
        final int rpmW = font.width(rpm);

        // text: Nodes (may be ellipsized)
        final String nodesFull = "Nodes " + kd.nodes();
        int nodesW = font.width(nodesFull);

        // reserve space for badges (≈ and 🔒) only if needed
        final boolean approx = kd.stressApproximate() || kd.nodesApproximate();
        final boolean locked = (lockedTarget != null);
        final int badgesW = (approx ? 14 : 0) + (locked ? 14 : 0);

        // compute bar width adaptively
        int remaining = contentW - rpmW - 8 /*gap*/ - nodesW - 6 /*gap*/ - badgesW;
        int barW = Math.max(32, Math.min(Math.max(40, contentW / 3), remaining));

        // if still negative, shrink nodes text (ellipsis) or drop it
        String nodesToDraw = nodesFull;
        if (barW < 32) {
            // try reduce nodes first
            final int maxNodesW = Math.max(0, contentW - rpmW - 8 - 32 - 6 - badgesW);
            if (maxNodesW <= 12) {
                // no space for nodes at all
                nodesToDraw = null;
                nodesW = 0;
                remaining = contentW - rpmW - 8 - badgesW;
                barW = Math.max(32, remaining);
            } else {
                nodesToDraw = ellipsize(nodesFull, maxNodesW, font);
                nodesW = font.width(nodesToDraw);
                remaining = contentW - rpmW - 8 - nodesW - 6 - badgesW;
                barW = Math.max(32, remaining);
            }
        }

        // draw RPM
        Widgets.text(gfx, rpm, x, y, theme.textPrimary());
        x += rpmW + 8;

        // draw bar
        final int barY = y + 2;
        Widgets.stressBar(gfx, x, barY, Math.max(0, barW), 6, kd, theme);
        x += barW + 8;

        // draw nodes (if any)
        if (nodesToDraw != null) {
            Widgets.text(gfx, nodesToDraw, x, y, theme.textSecondary());
            x += nodesW + 6;
        }

        // badges
        if (approx) {
            Widgets.badge(gfx, "≈", x, y - 2, theme);
            x += 14;
        }
        if (locked) {
            Widgets.badge(gfx, "🔒", x, y - 2, theme);
        }
    }

    // ellipsize helper (fits within maxW pixels)
    private static String ellipsize(final String s, final int maxW, final net.minecraft.client.gui.Font font) {
        if (font.width(s) <= maxW) return s;
        final String ell = "...";
        final int ellW = font.width(ell);
        int lo = 0, hi = s.length();
        while (lo < hi) {
            final int mid = (lo + hi) >>> 1;
            final String cut = s.substring(0, mid) + ell;
            if (font.width(cut) <= maxW) lo = mid + 1;
            else hi = mid;
        }
        final int take = Math.max(0, lo - 1);
        return (take <= 0) ? ell : s.substring(0, take) + ell;
    }

    // ====== EXPANDED ======

    private static void drawExpanded(final GuiGraphics gfx, final KineticData kd, final Theme theme, final int width, final int pad) {
        final var font = Minecraft.getInstance().font;
        int y = pad;

        // RPM header
        final int rpmAbs = Math.abs((int) kd.speed());
        final String rpm = "RPM " + (kd.speed() < 0 ? "-" : "") + rpmAbs;
        Widgets.text(gfx, rpm, pad, y, theme.textPrimary());
        y += font.lineHeight + 2;

        // Stress bar full-width
        final int barW = width - pad * 2;
        Widgets.stressBar(gfx, pad, y, barW, 8, kd, theme);

        // Load % (right-aligned above/beside the bar)
        final int pct = (int) Math.round(Math.max(0, Math.min(1, kd.stressRatio())) * 100.0);
        final String loadPct = pct + "%";
        final int pctX = pad + barW - font.width(loadPct);
        Widgets.text(gfx, loadPct, pctX, y - 1, theme.textSecondary());

        y += 8 + 6;

        // Capacity & Nodes on one row
        final int capacityInt = (int) Math.max(0, kd.stressRatio() * (kd.stressCapacity() <= 0 ? 0 : kd.stressCapacity()));
        final String capacity = "Load " + capacityInt + " / " + (int) kd.stressCapacity();
        Widgets.text(gfx, capacity, pad, y, theme.textSecondary());

        final String nodes = "Nodes " + kd.nodes();
        final int nodesX = width - pad - font.width(nodes);
        Widgets.text(gfx, nodes, nodesX, y, theme.textSecondary());

        // badges top-right
        int badgeX = width - pad - 16;
        if (kd.stressApproximate() || kd.nodesApproximate()) {
            Widgets.badge(gfx, "≈", badgeX, pad - 2, theme);
            badgeX -= 18;
        }
        if (lockedTarget != null) {
            Widgets.badge(gfx, "🔒", badgeX, pad - 2, theme);
        }
    }

    // ====== keybind hooks ======
    public static void toggleOverlay() {
        overlayEnabled = !overlayEnabled;
        CreateAnalyzerLite.LOGGER.info("Overlay: {}", overlayEnabled ? "ON" : "OFF");
    }

    public static void cycleMode() {
        currentMode = (currentMode == ClientConfig.DisplayMode.COMPACT)
            ? ClientConfig.DisplayMode.EXPANDED
            : ClientConfig.DisplayMode.COMPACT;
        CreateAnalyzerLite.LOGGER.debug("Display mode: {}", currentMode);
    }

    public static void toggleLock() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (lockedTarget != null) {
            lockedTarget = null;
            CreateAnalyzerLite.LOGGER.info("Target unlocked");
            return;
        }
        final BlockEntity be = TargetSelector.getTargetedKineticBlock(mc);
        if (be != null) {
            lockedTarget = be.getBlockPos();
            CreateAnalyzerLite.LOGGER.info("Target locked: {}", lockedTarget);
        }
    }

    private OverlayRenderer() { }
}
