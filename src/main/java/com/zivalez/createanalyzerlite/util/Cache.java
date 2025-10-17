package com.zivalez.createanalyzerlite.util;

import javax.annotation.Nullable;

/**
 * Simple TTL (time-to-live) cache for single values.
 * <p>
 * Thread-safe for single-threaded client use.
 * 
 * @param <T> Cached value type
 */
public final class Cache<T> {
    
    private final int ttlTicks;
    
    @Nullable
    private T value;
    private long expiryTick;
    
    /**
     * @param ttlTicks Time-to-live in game ticks (20 ticks = 1 second)
     */
    public Cache(final int ttlTicks) {
        if (ttlTicks < 1) {
            throw new IllegalArgumentException("TTL must be >= 1 tick");
        }
        this.ttlTicks = ttlTicks;
        this.expiryTick = -1;
    }
    
    /**
     * Get cached value if still valid.
     * 
     * @param currentTick Current game tick
     * @return Cached value or null if expired
     */
    @Nullable
    public T get(final long currentTick) {
        if (currentTick < expiryTick) {
            return value;
        }
        return null;
    }
    
    /**
     * Store value with TTL.
     * 
     * @param value Value to cache
     * @param currentTick Current game tick
     */
    public void put(@Nullable final T value, final long currentTick) {
        this.value = value;
        this.expiryTick = currentTick + ttlTicks;
    }
    
    /**
     * Clear cached value immediately.
     */
    public void invalidate() {
        this.value = null;
        this.expiryTick = -1;
    }
    
    /**
     * Check if cache contains valid value.
     * 
     * @param currentTick Current game tick
     * @return true if value is cached and not expired
     */
    public boolean isValid(final long currentTick) {
        return currentTick < expiryTick && value != null;
    }
}