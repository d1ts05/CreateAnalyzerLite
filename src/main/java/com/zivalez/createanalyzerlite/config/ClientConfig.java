package com.zivalez.createanalyzerlite.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {

    public static final ModConfigSpec SPEC;
    public static final Values V;

    static {
        final ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        V = new Values(b);
        SPEC = b.build();
    }

    public static final class Values {

        // === UI ===
        public final ModConfigSpec.EnumValue<Theme> theme;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.EnumValue<Anchor> anchor;
        public final ModConfigSpec.IntValue offsetX;
        public final ModConfigSpec.IntValue offsetY;
        public final ModConfigSpec.IntValue opacity;
        public final ModConfigSpec.IntValue padding;
        public final ModConfigSpec.IntValue cornerRadius;

        // === Content ===
        public final ModConfigSpec.BooleanValue showRPM;
        public final ModConfigSpec.BooleanValue showStress;
        public final ModConfigSpec.BooleanValue showNodes;
        public final ModConfigSpec.EnumValue<DisplayMode> defaultDisplayMode;

        // === Behaviour ===
        public final ModConfigSpec.BooleanValue hideInMenus;
        public final ModConfigSpec.BooleanValue onlyWhenHoldingGoggles;
        public final ModConfigSpec.BooleanValue lockTargetPersist;

        // === Performance ===
        public final ModConfigSpec.IntValue sampleEveryTicks;
        public final ModConfigSpec.IntValue maxBfsNodes;
        public final ModConfigSpec.IntValue cacheTtlTicks;

        private Values(final ModConfigSpec.Builder b) {
            b.push("UI");
            theme = b.defineEnum("theme", Theme.AUTO);
            scale = b.defineInRange("scale", 1.0D, 0.5D, 2.0D);
            anchor = b.defineEnum("anchor", Anchor.TOP_LEFT);
            offsetX = b.defineInRange("offsetX", 8, -4096, 4096);
            offsetY = b.defineInRange("offsetY", 8, -4096, 4096);
            opacity = b.defineInRange("opacity", 180, 0, 255);
            padding = b.defineInRange("padding", 8, 0, 64);
            cornerRadius = b.defineInRange("cornerRadius", 8, 0, 32);
            b.pop();

            b.push("Content");
            showRPM = b.define("showRPM", true);
            showStress = b.define("showStress", true);
            showNodes = b.define("showNodes", true);
            // ✅ change only this line: default EXPANDED
            defaultDisplayMode = b.defineEnum("defaultDisplayMode", DisplayMode.EXPANDED);
            b.pop();

            b.push("Behaviour");
            hideInMenus = b.define("hideInMenus", true);
            onlyWhenHoldingGoggles = b.define("onlyWhenHoldingGoggles", false);
            lockTargetPersist = b.define("lockTargetPersist", true);
            b.pop();

            b.push("Performance");
            sampleEveryTicks = b.defineInRange("sampleEveryTicks", 5, 1, 200);
            maxBfsNodes = b.defineInRange("maxBfsNodes", 2048, 64, 20000);
            cacheTtlTicks = b.defineInRange("cacheTtlTicks", 5, 1, 200);
            b.pop();
        }
    }

    public enum Theme { AUTO, LIGHT, DARK }
    public enum Anchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    public enum DisplayMode { COMPACT, EXPANDED }

    private ClientConfig() { }
}
