package com.zivalez.createanalyzerlite.config;

import com.zivalez.createanalyzerlite.config.ClientConfig.*;

/**
 * Immutable snapshot of client configuration.
 * <p>
 * Used to avoid reading from ModConfigSpec every frame.
 * Create via {@link #fromSpec()} and cache appropriately.
 * 
 * @param theme UI theme
 * @param scale UI scale multiplier
 * @param anchor Overlay anchor position
 * @param offsetX Horizontal offset
 * @param offsetY Vertical offset
 * @param opacity Background opacity
 * @param padding Internal padding
 * @param cornerRadius Corner radius
 * @param showRPM Show RPM indicator
 * @param showStress Show stress indicator
 * @param showNodes Show node count
 * @param defaultDisplayMode Default display mode
 * @param hideInMenus Hide in menus
 * @param onlyWhenHoldingGoggles Only show with goggles
 * @param lockTargetPersist Persist locked target
 * @param sampleEveryTicks Sampling interval
 * @param maxBfsNodes BFS node limit
 * @param cacheTtlTicks Cache TTL
 */
public record ConfigData(
    Theme theme,
    double scale,
    Anchor anchor,
    int offsetX,
    int offsetY,
    double opacity,
    int padding,
    int cornerRadius,
    boolean showRPM,
    boolean showStress,
    boolean showNodes,
    DisplayMode defaultDisplayMode,
    boolean hideInMenus,
    boolean onlyWhenHoldingGoggles,
    boolean lockTargetPersist,
    int sampleEveryTicks,
    int maxBfsNodes,
    int cacheTtlTicks
) {
    
    /**
     * Create snapshot from current ModConfigSpec values.
     */
    public static ConfigData fromSpec() {
        final ClientConfig.Values v = ClientConfig.VALUES;
        
        return new ConfigData(
            v.theme.get(),
            v.scale.get(),
            v.anchor.get(),
            v.offsetX.get(),
            v.offsetY.get(),
            v.opacity.get(),
            v.padding.get(),
            v.cornerRadius.get(),
            v.showRPM.get(),
            v.showStress.get(),
            v.showNodes.get(),
            v.defaultDisplayMode.get(),
            v.hideInMenus.get(),
            v.onlyWhenHoldingGoggles.get(),
            v.lockTargetPersist.get(),
            v.sampleEveryTicks.get(),
            v.maxBfsNodes.get(),
            v.cacheTtlTicks.get()
        );
    }
    
    /**
     * Check if any content flags are enabled.
     */
    public boolean hasAnyContent() {
        return showRPM || showStress || showNodes;
    }
}