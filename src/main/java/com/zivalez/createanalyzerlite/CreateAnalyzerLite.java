package com.zivalez.createanalyzerlite;

import com.zivalez.createanalyzerlite.config.ClientConfig;
import com.zivalez.createanalyzerlite.platform.NeoForgeClientBus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateAnalyzerLite.MOD_ID)
public final class CreateAnalyzerLite {
    
    public static final String MOD_ID = "createanalyzerlite";
    public static final Logger LOGGER = LoggerFactory.getLogger("CreateAnalyzerLite");
    
    public CreateAnalyzerLite(IEventBus modBus, ModContainer modContainer) {
        LOGGER.info("Initializing CreateAnalyzerLite...");
        
        modContainer.registerConfig(
            ModConfig.Type.CLIENT, 
            ClientConfig.SPEC,
            "createanalyzerlite-client.toml"
        );
        
        NeoForgeClientBus.register(modBus);
        
        LOGGER.info("CreateAnalyzerLite initialized successfully!");
    }
}