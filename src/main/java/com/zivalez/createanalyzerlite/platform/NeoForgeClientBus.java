package com.zivalez.createanalyzerlite.platform;

import com.zivalez.createanalyzerlite.CreateAnalyzerLite;
import com.zivalez.createanalyzerlite.input.Keybinds;
import com.zivalez.createanalyzerlite.hud.OverlayRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge client-side event bus registration.
 */
public final class NeoForgeClientBus {
    
    public static void register(final IEventBus modBus) {
        modBus.addListener(NeoForgeClientBus::onClientSetup);
        modBus.addListener(NeoForgeClientBus::onRegisterKeyMappings);
    }
    
    private static void onClientSetup(final FMLClientSetupEvent event) {
        CreateAnalyzerLite.LOGGER.debug("Client setup phase");
        
        // Register overlay renderer to NeoForge event bus
        NeoForge.EVENT_BUS.register(OverlayRenderer.class);
    }
    
    private static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        CreateAnalyzerLite.LOGGER.debug("Registering key mappings");
        Keybinds.register(event);
    }
    
    private NeoForgeClientBus() {
        throw new UnsupportedOperationException("Utility class");
    }
}