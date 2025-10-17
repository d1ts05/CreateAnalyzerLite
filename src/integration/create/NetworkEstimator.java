package com.zivalez.createanalyzerlite.integration.create;

import com.zivalez.createanalyzerlite.CreateAnalyzerLite;
import com.zivalez.createanalyzerlite.config.ConfigData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

/**
 * Estimates kinetic network size and stress via BFS traversal.
 * <p>
 * Used when Create's client network data is unavailable.
 * Respects maxBfsNodes config limit to prevent performance issues.
 */
public final class NetworkEstimator {
    
    // Create block class names for identification
    private static final String KINETIC_BE_CLASS = "com.simibubi.create.content.kinetics.base.KineticBlockEntity";
    
    /**
     * Estimate network metrics via BFS.
     */
    public static KineticQuery.StressData estimate(
        final BlockEntity startBe,
        final Level level,
        final ConfigData config
    ) {
        final Set<BlockPos> visited = new HashSet<>();
        final Queue<BlockPos> queue = new ArrayDeque<>();
        
        queue.add(startBe.getBlockPos());
        visited.add(startBe.getBlockPos());
        
        int nodeCount = 0;
        double estimatedConsumption = 0.0;
        double estimatedCapacity = 0.0;
        
        final int maxNodes = config.maxBfsNodes();
        
        while (!queue.isEmpty() && nodeCount < maxNodes) {
            final BlockPos current = queue.poll();
            nodeCount++;
            
            // Estimate stress contribution of this block
            final BlockEntity be = level.getBlockEntity(current);
            if (be != null && isKineticBlock(be)) {
                final StressContribution contrib = estimateBlockStress(be);
                estimatedConsumption += contrib.consumption();
                estimatedCapacity += contrib.capacity();
            }
            
            // Add neighbors
            for (final Direction dir : Direction.values()) {
                final BlockPos neighbor = current.relative(dir);
                
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                final BlockEntity neighborBe = level.getBlockEntity(neighbor);
                if (neighborBe != null && isKineticBlock(neighborBe)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }
        
        final boolean capped = nodeCount >= maxNodes;
        if (capped) {
            CreateAnalyzerLite.LOGGER.debug("BFS capped at {} nodes", maxNodes);
        }
        
        return new KineticQuery.StressData(
            estimatedConsumption,
            estimatedCapacity,
            nodeCount,
            true // Always approximate
        );
    }
    
    /**
     * Check if block entity is a kinetic block.
     */
    private static boolean isKineticBlock(final BlockEntity be) {
        try {
            final Class<?> kineticClass = Class.forName(KINETIC_BE_CLASS);
            return kineticClass.isInstance(be);
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Estimate stress contribution of a block.
     * <p>
     * This is a simplified heuristic - real values would need Create's
     * stress configuration data.
     */
    private static StressContribution estimateBlockStress(final BlockEntity be) {
        // TODO: Implement proper stress estimation based on block type
        // For now, return placeholder values
        
        final String blockName = be.getBlockState().getBlock().getDescriptionId();
        
        // Simple heuristics (these would need to be calibrated)
        if (blockName.contains("motor")) {
            return new StressContribution(0.0, 256.0); // Generator
        } else if (blockName.contains("fan") || blockName.contains("press")) {
            return new StressContribution(16.0, 0.0); // Consumer
        } else if (blockName.contains("furnace")) {
            return new StressContribution(32.0, 0.0); // Heavy consumer
        } else {
            return new StressContribution(0.0, 0.0); // Passive (shaft, cogwheel)
        }
    }
    
    /**
     * Stress contribution record.
     */
    private record StressContribution(double consumption, double capacity) {}
    
    private NetworkEstimator() {
        throw new UnsupportedOperationException("Utility class");
    }
}