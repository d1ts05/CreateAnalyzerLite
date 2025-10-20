package com.zivalez.createanalyzerlite.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zivalez.createanalyzerlite.CreateAnalyzerLite;
import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.config.ConfigData;
import com.zivalez.createanalyzerlite.integration.create.CreatePresent;
import com.zivalez.createanalyzerlite.integration.create.KineticData;
import com.zivalez.createanalyzerlite.integration.create.KineticQuery;
import com.zivalez.createanalyzerlite.probe.TargetSelector;
import com.zivalez.createanalyzerlite.util.Cache;
import com.zivalez.createanalyzerlite.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import javax.annotation.Nullable;

/**
 * Main overlay renderer for CreateAnalyzerLite HUD.
 */
public final class OverlayRenderer {

    private static boolean overlayEnabled = true;

    // ✅ Default to EXPANDED (request)
    private static ClientConfig.DisplayMode currentMode = ClientConfig.DisplayMode.EXPANDED;

    // TTL cache (~1s). Actual expiry handled via put(currentTick).
    private static final Cache<KineticData> dataCache = new Cache<>(20);
    private static long lastSampleTick = 0L;

    @Nullable
    private static BlockPos lockedTarget = null;

    @SubscribeEvent
    public static void onRenderGuiLayer(final RenderGuiLayerEvent.Post event) {
        // Render after crosshair so it sits above vanilla HUD
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) return;
        if (!overlayEnabled) return;
        if (!CreatePresent.isLoaded()) return;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        final ConfigData config = ConfigData.fromSpec();
        if (config.hideInMenus() && mc.screen != null) return;
        if (!config.hasAnyContent()) return;

        // Target (locked → raycast)
        final BlockEntity target = (lockedTarget != null)
            ? mc.level.getBlockEntity(lockedTarget)
            : TargetSelector.getTargetedKineticBlock(mc);
        if (target == null) return;

        // Data (throttled + cached)
        final KineticData data = queryKineticData(target, mc, config);
        if (data == null) return;

        renderOverlay(event.getGuiGraphics(), data, config, mc);
    }

    @Nullable
    private static KineticData queryKineticData(final BlockEntity target, final Minecraft mc, final ConfigData config) {
        final long currentTick = mc.level.getGameTime();

        // Cache hit?
        final KineticData cached = dataCache.get(currentTick);
        if (cached != null) return cached;

        // Throttle
        if (currentTick - lastSampleTick < config.sampleEveryTicks()) return null;
        lastSampleTick = currentTick;

        // Fresh query
        final KineticData fresh = KineticQuery.query(target, config);
        if (fresh != null) {
            dataCache.put(fresh, currentTick);
        }
        return fresh;
    }

    private static void renderOverlay(final GuiGraphics gfx, final KineticData data, final ConfigData config, final Minecraft mc) {
        final PoseStack pose = gfx.pose();
        pose.pushPose();
        try {
            final Theme theme = Theme.fromConfig(config);

            // Layout
            final LayoutEngine.Layout layout = LayoutEngine.calculate(
                config,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                currentMode
            );

            // Transform
            pose.translate(layout.x(), layout.y(), 0);
            pose.scale((float) config.scale(), (float) config.scale(), 1.0f);

            if (currentMode == ClientConfig.DisplayMode.EXPANDED) {
                renderExpanded(gfx, data, config, theme, layout);
            } else {
                renderCompact(gfx, data, config, theme, layout);
            }
        } finally {
            pose.popPose();
        }
    }

    /** Compact: RPM · mini stress bar · Nodes + badges (≈/🔒) */
    private static void renderCompact(
        final GuiGraphics gfx,
        final KineticData data,
        final ConfigData config,
        final Theme theme,
        final LayoutEngine.Layout layout
    ) {
        final int pad = config.padding();
        final int panelH = 18;
        Widgets.drawPanel(gfx, 0, 0, layout.width(), panelH, theme, config);

        int x = pad;
        int y = pad - 1;

        // RPM
        final int rpmAbs = Math.abs((int) data.speed());
        final String rpm = "RPM " + (data.speed() < 0 ? "-" : "") + rpmAbs;
        Widgets.drawText(gfx, rpm, x, y, theme.textColor());
        x += Minecraft.getInstance().font.width(rpm) + 8;

        // mini stress bar
        final int barW = Math.max(40, layout.width() / 3);
        Widgets.drawStressBar(gfx, x, y + 2, barW, 6, data, theme);
        x += barW + 8;

        // Nodes
        final String nodes = "Nodes " + data.nodes();
        Widgets.drawText(gfx, nodes, x, y, theme.mutedColor());
        x += Minecraft.getInstance().font.width(nodes) + 6;

        // Badges
        final boolean approx = data.stressApproximate() || data.nodesApproximate();
        if (approx) {
            final int bg = ColorUtil.withAlpha(theme.accentColor(), 0.25);
            Widgets.drawBadge(gfx, "≈", x, y - 2, bg, theme.textColor());
            x += 14;
        }
        if (lockedTarget != null) {
            final int bg = ColorUtil.withAlpha(theme.textColor(), 0.25);
            Widgets.drawBadge(gfx, "🔒", x, y - 2, bg, theme.textColor());
        }
    }

    /** Expanded: RPM header, full-width stress bar, Capacity + Nodes rows, approx & lock badges */
    private static void renderExpanded(
        final GuiGraphics gfx,
        final KineticData data,
        final ConfigData config,
        final Theme theme,
        final LayoutEngine.Layout layout
    ) {
        final int pad = config.padding();
        final int panelH = 64;
        Widgets.drawPanel(gfx, 0, 0, layout.width(), panelH, theme, config);

        int y = pad;

        // Header RPM
        final int rpmAbs = Math.abs((int) data.speed());
        final String rpm = "RPM " + (data.speed() < 0 ? "-" : "") + rpmAbs;
        Widgets.drawText(gfx, rpm, pad, y, theme.titleColor());
        y += 12;

        // Stress bar
        Widgets.drawStressBar(gfx, pad, y, layout.width() - pad * 2, 8, data, theme);
        y += 14;

        // Capacity (left) + Nodes (right)
        final String capacity = "Capacity " + (int) data.stressCapacity();
        Widgets.drawText(gfx, capacity, pad, y, theme.mutedColor());

        final String nodes = "Nodes " + data.nodes();
        final int nodesX = layout.width() - pad - Minecraft.getInstance().font.width(nodes);
        Widgets.drawText(gfx, nodes, nodesX, y, theme.mutedColor());

        // Badges (top-right)
        int badgeX = layout.width() - pad - 16;
        if (data.stressApproximate() || data.nodesApproximate()) {
            final int bg = ColorUtil.withAlpha(theme.accentColor(), 0.25);
            Widgets.drawBadge(gfx, "≈", badgeX, pad - 2, bg, theme.textColor());
            badgeX -= 18;
        }
        if (lockedTarget != null) {
            final int bg = ColorUtil.withAlpha(theme.textColor(), 0.25);
            Widgets.drawBadge(gfx, "🔒", badgeX, pad - 2, bg, theme.textColor());
        }
    }

    // === Keybind actions (used by Keybinds) ===

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
