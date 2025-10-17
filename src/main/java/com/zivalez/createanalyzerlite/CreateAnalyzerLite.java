package com.zivalez.createanalyzer;

import com.zivalez.createanalyzer.config.ClientConfig;
import com.zivalez.createanalyzer.platform.NeoForgeClientBus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CreateAnalyzer.MOD_ID)
public final class CreateAnalyzer {
    
    public static final String MOD_ID = "createanalyzer";
    public static final Logger LOGGER = LoggerFactory.getLogger("CreateAnalyzer");
    
    public CreateAnalyzer(IEventBus modBus, ModContainer modContainer) {
        LOGGER.info("Initializing CreateAnalyzer...");
        
        // Register client config
        modContainer.registerConfig(
            ModConfig.Type.CLIENT, 
            ClientConfig.SPEC,
            "createanalyzer-client.toml"
        );
        
        // Register client-side event handlers
        NeoForgeClientBus.register(modBus);
        
        LOGGER.info("CreateAnalyzer initialized successfully!");
    }
}