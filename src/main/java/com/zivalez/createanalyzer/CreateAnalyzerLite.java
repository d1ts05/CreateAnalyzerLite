package com.zivalez.createanalyzerlite;

import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.platform.NeoForgeClientBus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for CreateAnalyzerLite mod.
 * Client-only mod for displaying Create metrics via HUD overlay.
 */
@Mod(CreateAnalyzerLite.MOD_ID)
public final class CreateAnalyzerLite {
    
    public static final String MOD_ID = "createanalyzerlite";
    public static final Logger LOGGER = LoggerFactory.getLogger("CreateAnalyzerLite");
    
    public CreateAnalyzerLite(IEventBus modBus) {
        LOGGER.info("Initializing CreateAnalyzerLite...");
        
        // Register client config
        ModLoadingContext.get().registerConfig(
            ModConfig.Type.CLIENT, 
            ClientConfig.SPEC,
            "createanalyzerlite-client.toml"
        );
        
        // Register client-side event handlers
        NeoForgeClientBus.register(modBus);
        
        LOGGER.info("CreateAnalyzerLite initialized successfully!");
    }
}