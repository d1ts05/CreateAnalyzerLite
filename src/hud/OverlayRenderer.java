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
 * <p>
 * Hooks into NeoForge GUI overlay event (AFTER vanilla HUD) to render
 * kinetic metrics when targeting Create blocks.
 * <p>
 * Performance: Uses cached samples and throttled queries to avoid per-frame overhead.
 */
public final class OverlayRenderer {
    
    private static boolean overlayEnabled = true;
    private static ClientConfig.DisplayMode currentMode = ClientConfig.DisplayMode.COMPACT;
    
    private static final Cache<KineticData> dataCache = new Cache<>(20); // 1 second default
    private static long lastSampleTick = 0;
    
    @Nullable
    private static BlockPos lockedTarget = null;
    
    /**
     * Render overlay on GUI layer event.
     */
    @SubscribeEvent
    public static void onRenderGuiLayer(final RenderGuiLayerEvent.Post event) {
        // Only render after vanilla HUD elements
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
            return;
        }
        
        if (!overlayEnabled) {
            return;
        }
        
        // Graceful degradation: disable if Create not present
        if (!CreatePresent.isLoaded()) {
            return;
        }
        
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        
        // Check config: hide in menus?
        final ConfigData config = ConfigData.fromSpec();
        if (config.hideInMenus() && mc.screen != null) {
            return;
        }
        
        // Get target block entity
        final BlockEntity target = getTargetBlockEntity(mc, config);
        if (target == null) {
            return;
        }
        
        // Query kinetic data (cached)
        final KineticData data = queryKineticData(target, mc, config);
        if (data == null) {
            return;
        }
        
        // Render overlay
        renderOverlay(event.getGuiGraphics(), data, config, mc);
    }
    
    /**
     * Get target block entity (locked or raycast).
     */
    @Nullable
    private static BlockEntity getTargetBlockEntity(final Minecraft mc, final ConfigData config) {
        // If locked, use locked position
        if (lockedTarget != null) {
            return mc.level.getBlockEntity(lockedTarget);
        }
        
        // Raycast from crosshair
        return TargetSelector.getTargetedKineticBlock(mc);
    }
    
    /**
     * Query kinetic data with throttling and caching.
     */
    @Nullable
    private static KineticData queryKineticData(
        final BlockEntity target,
        final Minecraft mc,
        final ConfigData config
    ) {
        final long currentTick = mc.level.getGameTime();
        
        // Check cache first
        final KineticData cached = dataCache.get(currentTick);
        if (cached != null) {
            return cached;
        }
        
        // Throttle sampling
        if (currentTick - lastSampleTick < config.sampleEveryTicks()) {
            return null; // Skip this frame
        }
        
        lastSampleTick = currentTick;
        
        // Query fresh data
        final KineticData fresh = KineticQuery.query(target, config);
        if (fresh != null) {
            dataCache.put(fresh, currentTick);
        }
        
        return fresh;
    }
    
    /**
     * Render overlay UI.
     */
    private static void renderOverlay(
        final GuiGraphics gfx,
        final KineticData data,
        final ConfigData config,
        final Minecraft mc
    ) {
        final PoseStack pose = gfx.pose();
        pose.pushPose();
        
        try {
            // Get theme
            final Theme theme = Theme.fromConfig(config);
            
            // Calculate layout
            final LayoutEngine.Layout layout = LayoutEngine.calculate(
                config,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                currentMode
            );
            
            // Apply transform
            pose.translate(layout.x(), layout.y(), 0);
            pose.scale((float) config.scale(), (float) config.scale(), 1.0f);
            
            // Render based on mode
            switch (currentMode) {
                case COMPACT -> renderCompact(gfx, data, config, theme, layout);
                case EXPANDED -> renderExpanded(gfx, data, config, theme, layout);
            }
            
        } finally {
            pose.popPose();
        }
    }
    
    /**
     * Render compact mode (1-2 lines).
     */
    private static void renderCompact(
        final GuiGraphics gfx,
        final KineticData data,
        final ConfigData config,
        final Theme theme,
        final LayoutEngine.Layout layout
    ) {
        int yOffset = 0;
        
        // Background panel
        final int panelHeight = calculateCompactHeight(config);
        Widgets.drawPanel(gfx, 0, 0, layout.width(), panelHeight, theme, config);
        
        yOffset += config.padding();
        
        // RPM
        if (config.showRPM()) {
            final String rpmText = String.format("⚙ %.1f RPM", Math.abs(data.speed()));
            Widgets.drawText(gfx, rpmText, config.padding(), yOffset, theme.textColor());
            yOffset += 12;
        }
        
        // Stress bar (compact)
        if (config.showStress()) {
            final int barY = yOffset;
            final int barWidth = layout.width() - config.padding() * 2;
            Widgets.drawStressBar(gfx, config.padding(), barY, barWidth, 6, data, theme);
            yOffset += 10;
        }
        
        // Nodes badge
        if (config.showNodes()) {
            final String nodeText = data.nodesApproximate() 
                ? String.format("≈%d nodes", data.nodes())
                : String.format("%d nodes", data.nodes());
            Widgets.drawText(gfx, nodeText, config.padding(), yOffset, theme.mutedColor());
        }
    }
    
    /**
     * Render expanded mode (detailed panel).
     */
    private static void renderExpanded(
        final GuiGraphics gfx,
        final KineticData data,
        final ConfigData config,
        final Theme theme,
        final LayoutEngine.Layout layout
    ) {
        int yOffset = 0;
        
        // Background panel
        final int panelHeight = calculateExpandedHeight(config);
        Widgets.drawPanel(gfx, 0, 0, layout.width(), panelHeight, theme, config);
        
        yOffset += config.padding();
        
        // Title
        Widgets.drawText(gfx, "Create Metrics", config.padding(), yOffset, theme.titleColor());
        yOffset += 14;
        
        // RPM section
        if (config.showRPM()) {
            Widgets.drawLabel(gfx, "Speed:", config.padding(), yOffset, theme);
            final String rpmValue = String.format("%.1f RPM %s", 
                Math.abs(data.speed()),
                data.speed() > 0 ? "⟳" : "⟲"
            );
            Widgets.drawText(gfx, rpmValue, config.padding() + 50, yOffset, theme.valueColor());
            yOffset += 14;
        }
        
        // Stress section
        if (config.showStress()) {
            Widgets.drawLabel(gfx, "Stress:", config.padding(), yOffset, theme);
            yOffset += 12;
            
            final int barWidth = layout.width() - config.padding() * 2;
            Widgets.drawStressBar(gfx, config.padding(), yOffset, barWidth, 8, data, theme);
            yOffset += 12;
            
            final String stressText = data.stressApproximate()
                ? String.format("≈%.1f / %.1f", data.stressConsumption(), data.stressCapacity())
                : String.format("%.1f / %.1f", data.stressConsumption(), data.stressCapacity());
            Widgets.drawText(gfx, stressText, config.padding(), yOffset, theme.mutedColor());
            yOffset += 14;
        }
        
        // Nodes section
        if (config.showNodes()) {
            Widgets.drawLabel(gfx, "Network:", config.padding(), yOffset, theme);
            final String nodeText = data.nodesApproximate()
                ? String.format("≈%d nodes", data.nodes())
                : String.format("%d nodes", data.nodes());
            Widgets.drawText(gfx, nodeText, config.padding() + 60, yOffset, theme.valueColor());
            yOffset += 14;
        }
        
        // Lock indicator
        if (lockedTarget != null) {
            Widgets.drawText(gfx, "🔒 Locked", config.padding(), yOffset, theme.accentColor());
        }
    }
    
    private static int calculateCompactHeight(final ConfigData config) {
        int height = config.padding() * 2;
        if (config.showRPM()) height += 12;
        if (config.showStress()) height += 10;
        if (config.showNodes()) height += 12;
        return height;
    }
    
    private static int calculateExpandedHeight(final ConfigData config) {
        int height = config.padding() * 2 + 14; // Title
        if (config.showRPM()) height += 14;
        if (config.showStress()) height += 38; // Label + bar + text
        if (config.showNodes()) height += 14;
        if (lockedTarget != null) height += 14; // Lock indicator
        return height;
    }
    
    // === Public API for keybinds ===
    
    public static void toggleOverlay() {
        overlayEnabled = !overlayEnabled;
        CreateAnalyzerLite.LOGGER.info("Overlay: {}", overlayEnabled ? "ON" : "OFF");
    }
    
    public static void cycleMode() {
        currentMode = currentMode == ClientConfig.DisplayMode.COMPACT 
            ? ClientConfig.DisplayMode.EXPANDED 
            : ClientConfig.DisplayMode.COMPACT;
        CreateAnalyzerLite.LOGGER.debug("Display mode: {}", currentMode);
    }
    
    public static void toggleLock() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        if (lockedTarget != null) {
            lockedTarget = null;
            CreateAnalyzerLite.LOGGER.info("Target unlocked");
        } else {
            final BlockEntity target = TargetSelector.getTargetedKineticBlock(mc);
            if (target != null) {
                lockedTarget = target.getBlockPos();
                CreateAnalyzerLite.LOGGER.info("Target locked: {}", lockedTarget);
            }
        }
    }
    
    private OverlayRenderer() {
        throw new UnsupportedOperationException("Utility class");
    }
}