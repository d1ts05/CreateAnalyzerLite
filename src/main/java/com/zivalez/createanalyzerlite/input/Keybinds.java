package com.zivalez.createanalyzerlite.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.zivalez.createanalyzerlite.CreateAnalyzerLite;
import com.zivalez.createanalyzerlite.hud.OverlayRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;  // ✅ CORRECT for NeoForge 21.1.x
import org.lwjgl.glfw.GLFW;

/**
 * Keybind definitions and registration.
 * <p>
 * Polls keys every client tick to trigger overlay actions.
 */
public final class Keybinds {
    
    private static final String CATEGORY = "key.categories." + CreateAnalyzerLite.MOD_ID;
    
    public static final KeyMapping TOGGLE_OVERLAY = new KeyMapping(
        "key." + CreateAnalyzerLite.MOD_ID + ".toggle_overlay",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        CATEGORY
    );
    
    public static final KeyMapping CYCLE_MODE = new KeyMapping(
        "key." + CreateAnalyzerLite.MOD_ID + ".cycle_mode",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        CATEGORY
    );
    
    public static final KeyMapping LOCK_TARGET = new KeyMapping(
        "key." + CreateAnalyzerLite.MOD_ID + ".lock_target",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        CATEGORY
    );
    
    /**
     * Register keybinds with NeoForge.
     */
    public static void register(final RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_OVERLAY);
        event.register(CYCLE_MODE);
        event.register(LOCK_TARGET);
        
        CreateAnalyzerLite.LOGGER.debug("Registered {} keybinds", 3);
        
        // Register client tick handler for key polling
        // Use LevelTickEvent.Post (client-side) as workaround
        NeoForge.EVENT_BUS.addListener(Keybinds::onClientLevelTickEnd);
    }
    
    /**
     * Poll keys at end of client level tick.
     * <p>
     * NeoForge 21.1.x workaround: Use LevelTickEvent.Post on client level.
     */
    private static void onClientLevelTickEnd(final LevelTickEvent.Post event) {
        final Minecraft mc = Minecraft.getInstance();
        
        // Only poll on client-side level
        if (!event.getLevel().isClientSide()) {
            return;
        }
        
        // Only poll when player exists (in-game)
        if (mc.player == null) {
            return;
        }
        
        // Check keys (using consumeClick to prevent spam)
        while (TOGGLE_OVERLAY.consumeClick()) {
            OverlayRenderer.toggleOverlay();
        }
        
        while (CYCLE_MODE.consumeClick()) {
            OverlayRenderer.cycleMode();
        }
        
        while (LOCK_TARGET.consumeClick()) {
            OverlayRenderer.toggleLock();
        }
    }
    
    private Keybinds() {
        throw new UnsupportedOperationException("Utility class");
    }
}