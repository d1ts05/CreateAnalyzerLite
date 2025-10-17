package com.zivalez.createanalyzerlite.integration.create;

import com.zivalez.createanalyzerlite.CreateAnalyzerLite;
import net.neoforged.fml.ModList;

/**
 * Detect if Create mod is present and available.
 * <p>
 * All Create-specific code should be gated behind {@link #isLoaded()}.
 */
public final class CreatePresent {
    
    private static final boolean IS_LOADED = checkLoaded();
    
    private static boolean checkLoaded() {
        final boolean loaded = ModList.get().isLoaded("create");
        if (loaded) {
            CreateAnalyzerLite.LOGGER.info("Create mod detected - full features enabled");
        } else {
            CreateAnalyzerLite.LOGGER.info("Create mod not found - overlay will be disabled");
        }
        return loaded;
    }
    
    /**
     * @return true if Create mod is loaded
     */
    public static boolean isLoaded() {
        return IS_LOADED;
    }
    
    private CreatePresent() {
        throw new UnsupportedOperationException("Utility class");
    }
}