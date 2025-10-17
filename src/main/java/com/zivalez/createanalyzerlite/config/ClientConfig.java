package com.zivalez.createanalyzerlite.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Client-side configuration using ModConfigSpec (TOML backend).
 * <p>
 * Organizes settings into categories: UI, Content, Behaviour, Performance.
 * 
 * @see ConfigData for snapshot POJO
 */
public final class ClientConfig {
    
    public static final ModConfigSpec SPEC;
    public static final Values VALUES;
    
    static {
        final Pair<Values, ModConfigSpec> pair = new ModConfigSpec.Builder()
            .configure(Values::new);
        VALUES = pair.getLeft();
        SPEC = pair.getRight();
    }
    
    /**
     * Config values with defaults, ranges, and validation.
     */
    public static final class Values {
        
        // === UI Category ===
        public final ModConfigSpec.EnumValue<Theme> theme;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.EnumValue<Anchor> anchor;
        public final ModConfigSpec.IntValue offsetX;
        public final ModConfigSpec.IntValue offsetY;
        public final ModConfigSpec.DoubleValue opacity;
        public final ModConfigSpec.IntValue padding;
        public final ModConfigSpec.IntValue cornerRadius;
        
        // === Content Category ===
        public final ModConfigSpec.BooleanValue showRPM;
        public final ModConfigSpec.BooleanValue showStress;
        public final ModConfigSpec.BooleanValue showNodes;
        public final ModConfigSpec.EnumValue<DisplayMode> defaultDisplayMode;
        
        // === Behaviour Category ===
        public final ModConfigSpec.BooleanValue hideInMenus;
        public final ModConfigSpec.BooleanValue onlyWhenHoldingGoggles;
        public final ModConfigSpec.BooleanValue lockTargetPersist;
        
        // === Performance Category ===
        public final ModConfigSpec.IntValue sampleEveryTicks;
        public final ModConfigSpec.IntValue maxBfsNodes;
        public final ModConfigSpec.IntValue cacheTtlTicks;
        
        // === Internal ===
        public final ModConfigSpec.IntValue configVersion;
        
        Values(final ModConfigSpec.Builder builder) {
            
            // UI
            builder.comment("UI Appearance Settings")
                .push("ui");
            
            theme = builder
                .comment("Theme: AUTO (follow system), LIGHT, or DARK")
                .defineEnum("theme", Theme.AUTO);
            
            scale = builder
                .comment("UI scale multiplier (0.5 = 50%, 2.0 = 200%)")
                .defineInRange("scale", 1.0, 0.5, 2.0);
            
            anchor = builder
                .comment("Overlay anchor position")
                .defineEnum("anchor", Anchor.TOP_LEFT);
            
            offsetX = builder
                .comment("Horizontal offset from anchor (pixels)")
                .defineInRange("offsetX", 10, -1000, 1000);
            
            offsetY = builder
                .comment("Vertical offset from anchor (pixels)")
                .defineInRange("offsetY", 10, -1000, 1000);
            
            opacity = builder
                .comment("Background opacity (0.0 = transparent, 1.0 = opaque)")
                .defineInRange("opacity", 0.8, 0.0, 1.0);
            
            padding = builder
                .comment("Internal padding (pixels)")
                .defineInRange("padding", 8, 0, 32);
            
            cornerRadius = builder
                .comment("Corner radius for rounded edges (pixels)")
                .defineInRange("cornerRadius", 4, 0, 16);
            
            builder.pop();
            
            // Content
            builder.comment("Content Display Settings")
                .push("content");
            
            showRPM = builder
                .comment("Show RPM/speed indicator")
                .define("showRPM", true);
            
            showStress = builder
                .comment("Show stress consumption vs capacity")
                .define("showStress", true);
            
            showNodes = builder
                .comment("Show network node count")
                .define("showNodes", true);
            
            defaultDisplayMode = builder
                .comment("Default display mode: COMPACT or EXPANDED")
                .defineEnum("defaultDisplayMode", DisplayMode.COMPACT);
            
            builder.pop();
            
            // Behaviour
            builder.comment("Behaviour Settings")
                .push("behaviour");
            
            hideInMenus = builder
                .comment("Hide overlay when in menus (inventory, pause, etc.)")
                .define("hideInMenus", true);
            
            onlyWhenHoldingGoggles = builder
                .comment("Only show overlay when holding Engineer's Goggles (requires Create)")
                .define("onlyWhenHoldingGoggles", false);
            
            lockTargetPersist = builder
                .comment("Persist locked target across game sessions")
                .define("lockTargetPersist", false);
            
            builder.pop();
            
            // Performance
            builder.comment("Performance & Optimization")
                .push("perf");
            
            sampleEveryTicks = builder
                .comment("Sample target data every N ticks (lower = more responsive, higher = better performance)")
                .defineInRange("sampleEveryTicks", 5, 1, 20);
            
            maxBfsNodes = builder
                .comment("Maximum nodes to scan during BFS network estimation")
                .defineInRange("maxBfsNodes", 256, 16, 2048);
            
            cacheTtlTicks = builder
                .comment("Cache time-to-live in ticks (20 ticks = 1 second)")
                .defineInRange("cacheTtlTicks", 20, 5, 100);
            
            builder.pop();
            
            // Internal
            builder.comment("Internal (do not modify)")
                .push("internal");
            
            configVersion = builder
                .comment("Config schema version for migration")
                .defineInRange("configVersion", 1, 1, Integer.MAX_VALUE);
            
            builder.pop();
        }
    }
    
    // Enums
    
    public enum Theme {
        AUTO, LIGHT, DARK
    }
    
    public enum Anchor {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
    
    public enum DisplayMode {
        COMPACT, EXPANDED
    }
    
    private ClientConfig() {
        throw new UnsupportedOperationException("Utility class");
    }
}