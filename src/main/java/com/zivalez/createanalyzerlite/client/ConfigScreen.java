package com.zivalez.createanalyzerlite.client;

import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.config.ConfigData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Simple, safe, single-screen config UI with tabs.
 * - Uses ONLY existing ClientConfig values (no new APIs).
 * - Autosave: write to ClientConfig.V.* on every change.
 * - Reset per-setting (⟲) and Reset All.
 * - Done to close.
 */
public class ConfigScreen extends Screen {

    private enum Tab { UI, CONTENT, BEHAVIOUR, PERFORMANCE }

    private final Screen parent;
    private Tab current = Tab.UI;

    // Layout constants
    private static final int PANE_PADDING = 12;
    private static final int ROW_H = 22;
    private static final int GAP = 8;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Create Analyzer — Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        buildUi();
    }

    private void buildUi() {
        this.clearWidgets();

        final int fullW = this.width;
        final int fullH = this.height;

        // Tabs
        int tabX = PANE_PADDING;
        int tabY = PANE_PADDING;
        final int tabW = 110;
        addRenderableWidget(Button.builder(Component.literal("UI"), b -> switchTab(Tab.UI))
            .pos(tabX, tabY).size(tabW, 20).build());
        tabX += tabW + 6;
        addRenderableWidget(Button.builder(Component.literal("Content"), b -> switchTab(Tab.CONTENT))
            .pos(tabX, tabY).size(tabW, 20).build());
        tabX += tabW + 6;
        addRenderableWidget(Button.builder(Component.literal("Behaviour"), b -> switchTab(Tab.BEHAVIOUR))
            .pos(tabX, tabY).size(tabW, 20).build());
        tabX += tabW + 6;
        addRenderableWidget(Button.builder(Component.literal("Performance"), b -> switchTab(Tab.PERFORMANCE))
            .pos(tabX, tabY).size(tabW, 20).build());

        // Reset All (top-right)
        addRenderableWidget(Button.builder(Component.literal("Reset All ⟲"), b -> resetAllToDefaults())
            .pos(fullW - PANE_PADDING - 110, PANE_PADDING).size(110, 20).build());

        // Done
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
            .pos(fullW - PANE_PADDING - 80, fullH - PANE_PADDING - 20).size(80, 20).build());

        // Section body
        final int startY = PANE_PADDING + 28; // under tabs
        switch (current) {
            case UI -> buildSectionUI(startY);
            case CONTENT -> buildSectionContent(startY);
            case BEHAVIOUR -> buildSectionBehaviour(startY);
            case PERFORMANCE -> buildSectionPerformance(startY);
        }
    }

    private void switchTab(final Tab next) {
        this.current = next;
        buildUi();
    }

    // =========================
    // Sections
    // =========================

    private void buildSectionUI(final int startY) {
        final var V = ClientConfig.V;
        int y = startY;
        int x = PANE_PADDING;

        // Theme (AUTO/LIGHT/DARK)
        addEnumCycler(x, y, 180, "Theme",
            () -> V.theme.get(),
            v -> V.theme.set(v),
            ClientConfig.Theme.values(),
            ClientConfig.Theme::name);
        addResetPerSetting(x + 190, y, () -> V.theme.set(ClientConfig.Theme.AUTO));
        y += ROW_H + GAP;

        // Scale 0.5..2.0 step 0.1
        addDoubleStepper(x, y, 260, "Scale", 0.5, 2.0, 0.1,
            () -> V.scale.get(),
            d -> V.scale.set(clamp(d, 0.5, 2.0)));
        addResetPerSetting(x + 270, y, () -> V.scale.set(1.0D));
        y += ROW_H + GAP;

        // Anchor
        addEnumCycler(x, y, 180, "Anchor",
            () -> V.anchor.get(),
            v -> V.anchor.set(v),
            ClientConfig.Anchor.values(),
            ClientConfig.Anchor::name);
        addResetPerSetting(x + 190, y, () -> V.anchor.set(ClientConfig.Anchor.TOP_LEFT));
        y += ROW_H + GAP;

        // Offsets
        addIntStepper(x, y, 260, "Offset X", -4096, 4096, 2,
            () -> V.offsetX.get(), v -> V.offsetX.set(v));
        addResetPerSetting(x + 270, y, () -> V.offsetX.set(8));
        y += ROW_H + GAP;

        addIntStepper(x, y, 260, "Offset Y", -4096, 4096, 2,
            () -> V.offsetY.get(), v -> V.offsetY.set(v));
        addResetPerSetting(x + 270, y, () -> V.offsetY.set(8));
        y += ROW_H + GAP;

        // Opacity 0..255 step 5
        addIntStepper(x, y, 260, "Opacity", 0, 255, 5,
            () -> V.opacity.get(), v -> V.opacity.set(v));
        addResetPerSetting(x + 270, y, () -> V.opacity.set(180));
        y += ROW_H + GAP;

        // Padding
        addIntStepper(x, y, 260, "Padding", 0, 64, 1,
            () -> V.padding.get(), v -> V.padding.set(v));
        addResetPerSetting(x + 270, y, () -> V.padding.set(8));
        y += ROW_H + GAP;

        // Corner radius
        addIntStepper(x, y, 260, "Corner Radius", 0, 32, 1,
            () -> V.cornerRadius.get(), v -> V.cornerRadius.set(v));
        addResetPerSetting(x + 270, y, () -> V.cornerRadius.set(8));
        y += ROW_H + GAP;

        // Default display mode (COMPACT/EXPANDED)
        addEnumCycler(x, y, 220, "Default Mode",
            () -> V.defaultDisplayMode.get(),
            v -> V.defaultDisplayMode.set(v),
            ClientConfig.DisplayMode.values(),
            ClientConfig.DisplayMode::name);
        addResetPerSetting(x + 230, y, () -> V.defaultDisplayMode.set(ClientConfig.DisplayMode.EXPANDED));
    }

    private void buildSectionContent(final int startY) {
        final var V = ClientConfig.V;
        int y = startY;
        int x = PANE_PADDING;

        addBooleanToggle(x, y, 200, "Show RPM",
            () -> V.showRPM.get(),
            v -> V.showRPM.set(v));
        addResetPerSetting(x + 210, y, () -> V.showRPM.set(true));
        y += ROW_H + GAP;

        addBooleanToggle(x, y, 200, "Show Stress",
            () -> V.showStress.get(),
            v -> V.showStress.set(v));
        addResetPerSetting(x + 210, y, () -> V.showStress.set(true));
        y += ROW_H + GAP;

        addBooleanToggle(x, y, 200, "Show Nodes",
            () -> V.showNodes.get(),
            v -> V.showNodes.set(v));
        addResetPerSetting(x + 210, y, () -> V.showNodes.set(true));
    }

    private void buildSectionBehaviour(final int startY) {
        final var V = ClientConfig.V;
        int y = startY;
        int x = PANE_PADDING;

        addBooleanToggle(x, y, 260, "Hide in Menus",
            () -> V.hideInMenus.get(),
            v -> V.hideInMenus.set(v));
        addResetPerSetting(x + 270, y, () -> V.hideInMenus.set(true));
        y += ROW_H + GAP;

        addBooleanToggle(x, y, 260, "Only When Holding Goggles",
            () -> V.onlyWhenHoldingGoggles.get(),
            v -> V.onlyWhenHoldingGoggles.set(v));
        addResetPerSetting(x + 270, y, () -> V.onlyWhenHoldingGoggles.set(false));
        y += ROW_H + GAP;

        addBooleanToggle(x, y, 260, "Persist Locked Target",
            () -> V.lockTargetPersist.get(),
            v -> V.lockTargetPersist.set(v));
        addResetPerSetting(x + 270, y, () -> V.lockTargetPersist.set(true));
    }

    private void buildSectionPerformance(final int startY) {
        final var V = ClientConfig.V;
        int y = startY;
        int x = PANE_PADDING;

        addIntStepper(x, y, 300, "Sample Every Ticks", 1, 200, 1,
            () -> V.sampleEveryTicks.get(), v -> V.sampleEveryTicks.set(v));
        addResetPerSetting(x + 310, y, () -> V.sampleEveryTicks.set(5));
        y += ROW_H + GAP;

        addIntStepper(x, y, 300, "Max BFS Nodes", 64, 20000, 64,
            () -> V.maxBfsNodes.get(), v -> V.maxBfsNodes.set(v));
        addResetPerSetting(x + 310, y, () -> V.maxBfsNodes.set(2048));
        y += ROW_H + GAP;

        addIntStepper(x, y, 300, "Cache TTL (ticks)", 1, 200, 1,
            () -> V.cacheTtlTicks.get(), v -> V.cacheTtlTicks.set(v));
        addResetPerSetting(x + 310, y, () -> V.cacheTtlTicks.set(5));
    }

    // =========================
    // Widgets helpers (safe)
    // =========================

    private void addBooleanToggle(int x, int y, int w, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(renderBoolLabel(label, getter.get()), b -> {
            setter.accept(!getter.get());
            // re-label
            b.setMessage(renderBoolLabel(label, getter.get()));
        }).pos(x, y).size(w, 20).build());
    }

    private Component renderBoolLabel(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ON" : "OFF"));
    }

    private <E extends Enum<E>> void addEnumCycler(
        int x, int y, int w, String label,
        Supplier<E> getter, Consumer<E> setter,
        E[] values, java.util.function.Function<E, String> toText
    ) {
        addRenderableWidget(Button.builder(enumLabel(label, getter.get(), toText), b -> {
            final E cur = getter.get();
            int idx = java.util.Arrays.asList(values).indexOf(cur);
            idx = (idx + 1) % values.length;
            setter.accept(values[idx]);
            b.setMessage(enumLabel(label, values[idx], toText));
        }).pos(x, y).size(w, 20).build());
    }

    private <E extends Enum<E>> Component enumLabel(String label, E val, java.util.function.Function<E, String> toText) {
        return Component.literal(label + ": " + toText.apply(val));
    }

    private void addIntStepper(int x, int y, int w, String label, int min, int max, int step,
                               Supplier<Integer> getter, Consumer<Integer> setter) {
        final int btnW = 20;
        final int labelW = w - (btnW * 2 + 8);

        // - button
        addRenderableWidget(Button.builder(Component.literal("−"), b -> {
            int v = getter.get();
            v = clamp(v - step, min, max);
            setter.accept(v);
            buildUi(); // refresh numbers
        }).pos(x, y).size(btnW, 20).build());

        // label (value)
        addRenderableWidget(Button.builder(Component.literal(label + ": " + getter.get()), b -> {})
            .pos(x + btnW + 4, y).size(labelW, 20).build()).active = false;

        // + button
        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            int v = getter.get();
            v = clamp(v + step, min, max);
            setter.accept(v);
            buildUi();
        }).pos(x + btnW + 4 + labelW + 4, y).size(btnW, 20).build());
    }

    private void addDoubleStepper(int x, int y, int w, String label, double min, double max, double step,
                                  Supplier<Double> getter, Consumer<Double> setter) {
        final int btnW = 20;
        final int labelW = w - (btnW * 2 + 8);

        addRenderableWidget(Button.builder(Component.literal("−"), b -> {
            double v = getter.get();
            v = clamp(round1(v - step), min, max);
            setter.accept(v);
            buildUi();
        }).pos(x, y).size(btnW, 20).build());

        addRenderableWidget(Button.builder(Component.literal(label + ": " + format1(getter.get())), b -> {})
            .pos(x + btnW + 4, y).size(labelW, 20).build()).active = false;

        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            double v = getter.get();
            v = clamp(round1(v + step), min, max);
            setter.accept(v);
            buildUi();
        }).pos(x + btnW + 4 + labelW + 4, y).size(btnW, 20).build());
    }

    private void addResetPerSetting(int x, int y, Runnable resetAction) {
        addRenderableWidget(Button.builder(Component.literal("⟲"), b -> {
            resetAction.run();
            buildUi();
        }).pos(x, y).size(24, 20).build());
    }

    private void resetAllToDefaults() {
        final var V = ClientConfig.V;
        // UI
        V.theme.set(ClientConfig.Theme.AUTO);
        V.scale.set(1.0D);
        V.anchor.set(ClientConfig.Anchor.TOP_LEFT);
        V.offsetX.set(8);
        V.offsetY.set(8);
        V.opacity.set(180);
        V.padding.set(8);
        V.cornerRadius.set(8);
        V.defaultDisplayMode.set(ClientConfig.DisplayMode.EXPANDED);
        // Content
        V.showRPM.set(true);
        V.showStress.set(true);
        V.showNodes.set(true);
        // Behaviour
        V.hideInMenus.set(true);
        V.onlyWhenHoldingGoggles.set(false);
        V.lockTargetPersist.set(true);
        // Performance
        V.sampleEveryTicks.set(5);
        V.maxBfsNodes.set(2048);
        V.cacheTtlTicks.set(5);

        buildUi();
    }

    // =========================
    // Util
    // =========================

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }
    private static String format1(double v) { return String.format("%.1f", v); }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void render(final GuiGraphics gfx, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);

        // Title + Subtitle
        final int cx = this.width / 2;
        gfx.drawCenteredString(this.font, this.title, cx, 6, 0xFFFFFF);
        final ConfigData cfg = ConfigData.fromSpec();
        gfx.drawCenteredString(this.font,
            Component.literal("Theme: " + cfg.theme() + " • Mode: " + cfg.defaultDisplayMode() + " • Scale: " + String.format("%.1f", cfg.scale())),
            cx, 18, 0xA0A0A0);
    }
}
