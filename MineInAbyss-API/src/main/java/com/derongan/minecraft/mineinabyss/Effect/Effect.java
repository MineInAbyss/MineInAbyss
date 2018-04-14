package com.derongan.minecraft.mineinabyss.Effect;

import org.bukkit.entity.Entity;

/**
 * This interface defines buffs/debuffs intended to be applied
 * one off or periodically to entities.
 */
@FunctionalInterface
public interface Effect<T extends Entity> {
    /**
     * Apply effect to entity.
     * @param entity
     * @param strength Some effects have variable strength
     */
    void apply(T entity, int strength);
}
