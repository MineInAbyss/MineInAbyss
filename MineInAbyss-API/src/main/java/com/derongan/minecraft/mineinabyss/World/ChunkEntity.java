package com.derongan.minecraft.mineinabyss.World;

import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;

/**
 * An entity that can be loaded/unloaded out of sync with chunk loading.
 * Implementations are responsible for serializing the entity for saving.
 */
public interface ChunkEntity extends ConfigurationSerializable {
    final String TIME_REMAINING_KEY = "time_remaining";
    final String TIME_SERIALIZED_KEY = "time_serialized";
    final String X_KEY = "x";
    final String Y_KEY = "y";
    final String Z_KEY = "z";

    /**
     * @return amount of time in seconds remaining before this entity despawns. -1 if the entity is immortal.
     */
    long getTimeRemaining();


    /**
     * @return the system time in ms when this method was called
     */
    long getCurrentTime();

    /**
     * Creates the associated entity (on chunk load for example) at location
     * @param world
     */
    Entity createEntity(World world);

    /**
     * Destroys the associated entity (on chunk unload for example)
     */
    void destroyEntity();

    Entity getEntity();

    /**
     * Get the X location of the entity
     */
    int getX();

    /**
     * Get the Y location of the entity
     */
    int getY();

    /**
     * Get the Z location of the entity
     */
    int getZ();
}
