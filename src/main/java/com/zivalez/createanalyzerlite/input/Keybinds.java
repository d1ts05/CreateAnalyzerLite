package com.zivalez.createanalyzer.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.zivalez.createanalyzer.CreateAnalyzer;
import com.zivalez.createanalyzer.hud.OverlayRenderer;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

public final class Keybinds {
    
    private static final String CATEGORY = "key.categories." + CreateAnalyzer.MOD_ID;
    
    public static final KeyMapping TOGGLE_OVERLAY = new KeyMapping(
        "key." + CreateAnalyzer.MOD_ID + ".toggle_overlay",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        CATEGORY
    );
    
    public static final KeyMapping CYCLE_MODE = new KeyMapping(
        "key." + CreateAnalyzer.MOD_ID + ".cycle_mode",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        CATEGORY
    );
    
    public static final KeyMapping LOCK_TARGET = new KeyMapping(
        "key." + CreateAnalyzer.MOD_ID + ".lock_target",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        CATEGORY
    );
    
    public static void register(final RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_OVERLAY);
        event.register(CYCLE_MODE);
        event.register(LOCK_TARGET);
        
        CreateAnalyzer.LOGGER.debug("Registered {} keybinds", 3);
        
        NeoForge.EVENT_BUS.addListener(Keybinds::onClientTick);
    }
    
    private static void onClientTick(final ClientTickEvent.Post event) {
        if (TOGGLE_OVERLAY.consumeClick()) {
            OverlayRenderer.toggleOverlay();
        }
        
        if (CYCLE_MODE.consumeClick()) {
            OverlayRenderer.cycleMode();
        }
        
        if (LOCK_TARGET.consumeClick()) {
            OverlayRenderer.toggleLock();
        }
    }
    
    private Keybinds() {
        throw new UnsupportedOperationException("Utility class");
    }
}