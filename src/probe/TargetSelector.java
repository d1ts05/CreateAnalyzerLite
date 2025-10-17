package com.zivalez.createanalyzerlite.probe;

import com.zivalez.createanalyzerlite.integration.create.CreatePresent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

/**
 * Selects kinetic block entities via raycasting from player crosshair.
 * <p>
 * Used by overlay to determine which block to display metrics for.
 */
public final class TargetSelector {
    
    private static final String KINETIC_BE_CLASS = "com.simibubi.create.content.kinetics.base.KineticBlockEntity";
    
    private static final double MAX_REACH_DISTANCE = 20.0; // blocks
    
    /**
     * Get the kinetic block entity currently targeted by player crosshair.
     * 
     * @param mc Minecraft instance
     * @return Targeted kinetic block entity or null
     */
    @Nullable
    public static BlockEntity getTargetedKineticBlock(final Minecraft mc) {
        if (!CreatePresent.isLoaded()) {
            return null;
        }
        
        if (mc.player == null || mc.level == null) {
            return null;
        }
        
        // Get block player is looking at
        final HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        
        final BlockHitResult blockHit = (BlockHitResult) hitResult;
        final BlockPos pos = blockHit.getBlockPos();
        
        // Check distance
        if (mc.player.position().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > MAX_REACH_DISTANCE * MAX_REACH_DISTANCE) {
            return null;
        }
        
        // Get block entity
        final BlockEntity be = mc.level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        
        // Check if it's a kinetic block
        if (!isKineticBlock(be)) {
            return null;
        }
        
        return be;
    }
    
    /**
     * Check if block entity is a Create kinetic block.
     */
    private static boolean isKineticBlock(final BlockEntity be) {
        try {
            final Class<?> kineticClass = Class.forName(KINETIC_BE_CLASS);
            return kineticClass.isInstance(be);
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
    
    private TargetSelector() {
        throw new UnsupportedOperationException("Utility class");
    }
}