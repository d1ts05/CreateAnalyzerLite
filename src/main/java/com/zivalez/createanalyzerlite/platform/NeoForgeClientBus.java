package com.zivalez.createanalyzer.platform;

import com.zivalez.createanalyzer.CreateAnalyzer;
import com.zivalez.createanalyzer.input.Keybinds;
import com.zivalez.createanalyzer.hud.OverlayRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class NeoForgeClientBus {
    
    public static void register(final IEventBus modBus) {
        modBus.addListener(NeoForgeClientBus::onClientSetup);
        modBus.addListener(NeoForgeClientBus::onRegisterKeyMappings);
    }
    
    private static void onClientSetup(final FMLClientSetupEvent event) {
        CreateAnalyzer.LOGGER.debug("Client setup phase");
        
        NeoForge.EVENT_BUS.register(OverlayRenderer.class);
    }
    
    private static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        CreateAnalyzer.LOGGER.debug("Registering key mappings");
        Keybinds.register(event);
    }
    
    private NeoForgeClientBus() {
        throw new UnsupportedOperationException("Utility class");
    }
}