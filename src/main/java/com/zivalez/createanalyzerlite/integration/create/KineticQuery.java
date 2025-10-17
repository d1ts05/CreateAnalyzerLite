package com.zivalez.createanalyzerlite.integration.create;

import com.zivalez.createanalyzerlite.CreateAnalyzerLite;
import com.zivalez.createanalyzerlite.config.ConfigData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

/**
 * Query kinetic data from Create's block entities.
 * <p>
 * Safely reads client-available data from KineticBlockEntity
 * via reflection/API to avoid hard dependency on Create internals.
 */
public final class KineticQuery {
    
    // Create API class names (reflection fallback)
    private static final String KINETIC_BE_CLASS = "com.simibubi.create.content.kinetics.base.KineticBlockEntity";
    
    /**
     * Query kinetic data from block entity.
     * 
     * @param be Block entity to query
     * @param config Configuration data
     * @return Kinetic data or null if unavailable
     */
    @Nullable
    public static KineticData query(final BlockEntity be, final ConfigData config) {
        if (!CreatePresent.isLoaded()) {
            return null;
        }
        
        try {
            // Try to cast to KineticBlockEntity
            final Class<?> kineticClass = Class.forName(KINETIC_BE_CLASS);
            if (!kineticClass.isInstance(be)) {
                return null; // Not a kinetic block
            }
            
            // Read speed (client-synced field)
            final float speed = getSpeed(be, kineticClass);
            
            // Try to read stress from network (client may not have this)
            final StressData stress = getStressData(be, kineticClass, config);
            
            return new KineticData(
                speed,
                stress.consumption(),
                stress.capacity(),
                stress.nodes(),
                stress.approximate(),
                stress.approximate()
            );
            
        } catch (final Exception e) {
            CreateAnalyzerLite.LOGGER.debug("Failed to query kinetic data: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get speed from kinetic block entity.
     */
    private static float getSpeed(final BlockEntity be, final Class<?> kineticClass) throws Exception {
        final var speedField = kineticClass.getDeclaredField("speed");
        speedField.setAccessible(true);
        return speedField.getFloat(be);
    }
    
    /**
     * Get stress data (network info).
     * <p>
     * If client network data unavailable, falls back to NetworkEstimator.
     */
    private static StressData getStressData(
        final BlockEntity be,
        final Class<?> kineticClass,
        final ConfigData config
    ) {
        try {
            // Try to read from network (Create 0.5.1+ may expose this)
            // This is a simplified approach - real implementation would need
            // to access the network graph properly
            
            // For now, use estimator as primary method
            final Minecraft mc = Minecraft.getInstance();
            return NetworkEstimator.estimate(be, mc.level, config);
            
        } catch (final Exception e) {
            CreateAnalyzerLite.LOGGER.debug("Network query failed, using estimator: {}", e.getMessage());
            final Minecraft mc = Minecraft.getInstance();
            return NetworkEstimator.estimate(be, mc.level, config);
        }
    }
    
    /**
     * Internal stress data holder.
     */
    record StressData(
        double consumption,
        double capacity,
        int nodes,
        boolean approximate
    ) {}
    
    private KineticQuery() {
        throw new UnsupportedOperationException("Utility class");
    }
}