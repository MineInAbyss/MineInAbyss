package com.derongan.minecraft.mineinabyss.World;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

/**
 * Manages special entities on chunks. Special entities are those that we want to be able
 * to spawn/despawn while a chunk is not loaded
 */
public interface EntityChunkManager {
    void loadChunk(Chunk chunk);

    void unloadChunk(Chunk chunk);

    void addEntity(Chunk chunk, ChunkEntity chunkEntity);

    void removeEntity(Chunk chunk, Entity entity);
}
