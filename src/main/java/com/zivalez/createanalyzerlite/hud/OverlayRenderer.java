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
 * Overlay renderer (UI-only adjustments).
 * Uses existing helper APIs:
 *  - Theme.resolve(cfg)
 *  - Widgets.panel/text/stressBar/badge
 *  - LayoutEngine.baseWidth/computeX/computeY
 *
 * Default display mode will follow config.defaultDisplayMode (set to EXPANDED by default via ClientConfig).
 */
public final class OverlayRenderer {

    private static boolean overlayEnabled = true;

    // Lazy-init from config so it truly follows defaultDisplayMode
    @Nullable
    private static ClientConfig.DisplayMode currentMode = null;

    @Nullable
    private static BlockPos lockedTarget = null;

    // Simple throttle + last snapshot (no custom Cache type)
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
            // respect config default (we set EXPANDED as default in ClientConfig)
            currentMode = cfg.defaultDisplayMode();
        }

        // Resolve target: locked first, else raycast
        final BlockEntity target = (lockedTarget != null)
            ? mc.level.getBlockEntity(lockedTarget)
            : TargetSelector.getTargetedKineticBlock(mc);

        if (target == null) return;

        // Kinetic data with throttling
        final KineticData kd = queryKineticData(target, mc, cfg);
        if (kd == null) return;

        renderOverlay(evt.getGuiGraphics(), kd, cfg, mc);
    }

    @Nullable
    private static KineticData queryKineticData(final BlockEntity target, final Minecraft mc, final ConfigData cfg) {
        final long now = mc.level.getGameTime();

        // Throttle: reuse last snapshot within sampleEveryTicks window
        if (lastData != null && (now - lastSampleTick) < cfg.sampleEveryTicks()) {
            return lastData;
        }

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
            final int panelH = expanded ? 64 : 18;
            final int pad = cfg.padding();

            final int screenW = mc.getWindow().getGuiScaledWidth();
            final int screenH = mc.getWindow().getGuiScaledHeight();

            final int x = LayoutEngine.computeX(cfg.anchor(), screenW, baseW, cfg.offsetX(), cfg.scale());
            final int y = LayoutEngine.computeY(cfg.anchor(), screenH, panelH, cfg.offsetY(), cfg.scale());

            pose.translate(x, y, 0);
            pose.scale((float) cfg.scale(), (float) cfg.scale(), 1.0f);

            // Panel background
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

    /** COMPACT: "RPM · mini stress bar · Nodes" + badges (≈ / 🔒) */
    private static void drawCompact(final GuiGraphics gfx, final KineticData kd, final Theme theme, final int width, final int pad) {
        int x = pad;
        final int y = pad - 1;

        // RPM (stable text width not enforced here to avoid extra helpers)
        final int rpmAbs = Math.abs((int) kd.speed());
        final String rpm = "RPM " + (kd.speed() < 0 ? "-" : "") + rpmAbs;
        Widgets.text(gfx, rpm, x, y, theme.textPrimary());
        x += Minecraft.getInstance().font.width(rpm) + 8;

        // Mini stress bar
        final int barW = Math.max(40, width / 3);
        Widgets.stressBar(gfx, x, y + 2, barW, 6, kd, theme);
        x += barW + 8;

        // Nodes
        final String nodes = "Nodes " + kd.nodes();
        Widgets.text(gfx, nodes, x, y, theme.textSecondary());
        x += Minecraft.getInstance().font.width(nodes) + 6;

        // Badges
        if (kd.stressApproximate() || kd.nodesApproximate()) {
            Widgets.badge(gfx, "≈", x, y - 2, theme);
            x += 14;
        }
        if (lockedTarget != null) {
            Widgets.badge(gfx, "🔒", x, y - 2, theme);
        }
    }

    /** EXPANDED: Header RPM, full-width stress bar, Capacity & Nodes, badges top-right */
    private static void drawExpanded(final GuiGraphics gfx, final KineticData kd, final Theme theme, final int width, final int pad) {
        int y = pad;

        // Header RPM
        final int rpmAbs = Math.abs((int) kd.speed());
        final String rpm = "RPM " + (kd.speed() < 0 ? "-" : "") + rpmAbs;
        Widgets.text(gfx, rpm, pad, y, theme.textPrimary());
        y += 12;

        // Stress bar full width
        Widgets.stressBar(gfx, pad, y, width - pad * 2, 8, kd, theme);
        y += 14;

        // Capacity (left)
        final String capacity = "Capacity " + (int) kd.stressCapacity();
        Widgets.text(gfx, capacity, pad, y, theme.textSecondary());

        // Nodes (right)
        final String nodes = "Nodes " + kd.nodes();
        final int nodesX = width - pad - Minecraft.getInstance().font.width(nodes);
        Widgets.text(gfx, nodes, nodesX, y, theme.textSecondary());

        // Badges (top-right)
        int badgeX = width - pad - 16;
        if (kd.stressApproximate() || kd.nodesApproximate()) {
            Widgets.badge(gfx, "≈", badgeX, pad - 2, theme);
            badgeX -= 18;
        }
        if (lockedTarget != null) {
            Widgets.badge(gfx, "🔒", badgeX, pad - 2, theme);
        }
    }

    // ====== Keybind hooks (called from Keybinds) ======
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
