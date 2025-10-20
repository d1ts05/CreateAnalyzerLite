package com.zivalez.createanalyzerlite.hud;

import com.zivalez.createanalyzerlite.CreateAnalyzerLite;
import com.zivalez.createanalyzerlite.config.ConfigData;
import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.integration.create.KineticData;
import com.zivalez.createanalyzerlite.integration.create.KineticQuery;
import com.zivalez.createanalyzerlite.probe.TargetSelector;
import com.zivalez.createanalyzerlite.util.Cache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;

public final class OverlayRenderer {

    private static ClientConfig.DisplayMode currentMode = null;
    private static boolean overlayEnabled = true;
    @Nullable private static net.minecraft.core.BlockPos lockedTarget = null;

    private static final Cache<KineticData> dataCache = new Cache<>();
    private static long lastSampleTick = 0L;

    @SubscribeEvent
    public static void onRenderGui(final RenderGuiEvent.Post evt) {
        if (!overlayEnabled) return;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        final ConfigData cfg = ConfigData.fromSpec();
        if (cfg.hideInMenus() && mc.screen != null) return;

        if (currentMode == null) {
            currentMode = cfg.defaultDisplayMode();
        }

        final Theme theme = Theme.resolve(cfg);

        final BlockEntity be = (lockedTarget != null)
            ? getLocked(mc)
            : TargetSelector.getTargetedKineticBlock(mc);

        final KineticData kd = sampleKinetic(be, cfg);
        if (kd == null || !cfg.hasAnyContent()) return;

        final boolean expanded = (currentMode == ClientConfig.DisplayMode.EXPANDED);
        final int baseW = LayoutEngine.baseWidth(expanded);
        final int pad = cfg.padding();
        final int contentW = baseW - pad * 2;

        final int screenW = evt.getWindow().getGuiScaledWidth();
        final int screenH = evt.getWindow().getGuiScaledHeight();

        final int estH = expanded ? 64 : 16;
        final int x = LayoutEngine.computeX(cfg.anchor(), screenW, baseW, cfg.offsetX(), cfg.scale());
        final int y = LayoutEngine.computeY(cfg.anchor(), screenH, estH, cfg.offsetY(), cfg.scale());

        final GuiGraphics gfx = evt.getGuiGraphics();
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, 0);
        gfx.pose().scale((float) cfg.scale(), (float) cfg.scale(), 1.0F);

        // panel
        Widgets.panel(gfx, 0, 0, baseW, expanded ? 64 : 18, theme, cfg.opacity());

        if (expanded) {
            drawExpanded(gfx, pad, theme, kd, contentW);
        } else {
            final int rowH = Widgets.compactRow(gfx, pad, pad, contentW, theme, kd);
            // locked badge
            if (lockedTarget != null) {
                Widgets.badge(gfx, "🔒", pad + contentW - 16, pad - 2, theme);
            }
        }

        gfx.pose().popPose();
    }

    private static void drawExpanded(final GuiGraphics gfx, final int pad, final Theme theme, final KineticData kd, final int w) {
        // Header RPM
        final String rpm = String.format("RPM %s%d", kd.speed() < 0 ? "-" : "", Math.abs((int) kd.speed()));
        Widgets.text(gfx, rpm, pad, pad, theme.textPrimary());

        // Stress bar full width
        Widgets.stressBar(gfx, pad, pad + 12, w, 8, kd, theme);

        // Cards: Capacity & Nodes
        final int cardY = pad + 24;
        final int cardW = (w - 6) / 2;

        // Capacity
        final String cap = "Capacity " + (int) kd.stressCapacity();
        Widgets.text(gfx, cap, pad, cardY, theme.textSecondary());

        // Nodes (+≈ if approx)
        final String nodes = "Nodes " + kd.nodes();
        Widgets.text(gfx, nodes, pad + cardW + 6, cardY, theme.textSecondary());
        if (kd.stressApproximate() || kd.nodesApproximate()) {
            Widgets.badge(gfx, "≈", pad + w - 18, cardY - 2, theme);
        }

        // Lock indicator (top-right)
        if (lockedTarget != null) {
            Widgets.badge(gfx, "🔒", pad + w - 36, pad - 2, theme);
        }
    }

    @Nullable
    private static KineticData sampleKinetic(@Nullable final BlockEntity be, final ConfigData cfg) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        final long now = mc.level.getGameTime();
        final KineticData cached = dataCache.get(now);
        if (cached != null) return cached;

        if (now - lastSampleTick < cfg.sampleEveryTicks()) return null;
        lastSampleTick = now;

        if (be == null) return null;
        final KineticData out = KineticQuery.query(be, cfg);
        dataCache.set(out, now + cfg.cacheTtlTicks());
        return out;
    }

    @Nullable
    private static BlockEntity getLocked(final Minecraft mc) {
        if (lockedTarget == null) return null;
        return mc.level.getBlockEntity(lockedTarget);
    }

    private OverlayRenderer() { }
}
