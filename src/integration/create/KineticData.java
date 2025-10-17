package com.zivalez.createanalyzerlite.integration.create;

/**
 * Immutable record holding kinetic network metrics.
 * <p>
 * Returned by queries and used for overlay rendering.
 * Flags indicate whether values are approximate (estimated) or exact.
 * 
 * @param speed Rotational speed in RPM (negative = counter-clockwise)
 * @param stressConsumption Current stress consumption
 * @param stressCapacity Maximum stress capacity
 * @param nodes Number of kinetic blocks in network
 * @param stressApproximate True if stress values are estimated
 * @param nodesApproximate True if node count is capped/estimated
 */
public record KineticData(
    float speed,
    double stressConsumption,
    double stressCapacity,
    int nodes,
    boolean stressApproximate,
    boolean nodesApproximate
) {
    /**
     * Check if network is overloaded.
     */
    public boolean isOverloaded() {
        return stressCapacity > 0 && stressConsumption > stressCapacity;
    }
    
    /**
     * Get stress ratio (0.0 to 1.0+).
     */
    public double stressRatio() {
        return stressCapacity > 0 ? stressConsumption / stressCapacity : 0.0;
    }
    
    /**
     * Check if any values are approximate.
     */
    public boolean hasApproximateValues() {
        return stressApproximate || nodesApproximate;
    }
}