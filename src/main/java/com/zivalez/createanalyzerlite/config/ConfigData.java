package com.zivalez.createanalyzerlite.config;

import com.zivalez.createanalyzerlite.config.ClientConfig.*;

public record ConfigData(
    Theme theme,
    double scale,
    Anchor anchor,
    int offsetX,
    int offsetY,
    int opacity,
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
    public static ConfigData fromSpec() {
        final var v = ClientConfig.V;
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

    public boolean hasAnyContent() {
        return showRPM || showStress || showNodes;
    }
}
