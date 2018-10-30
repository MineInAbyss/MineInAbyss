package com.derongan.minecraft.mineinabyss.world;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;


import java.io.IOException;
import java.util.*;

public class EntityChunkManagerImpl implements EntityChunkManager {
    private Map<ChunkKey, Collection<ChunkEntity>> chunkInfoMap;
    private Map<UUID, ChunkEntity> chunkEntityMap;
    private AbyssWorldManager manager;
    private AbyssContext context;
    private WorldDataConfigManager configManager;

    public EntityChunkManagerImpl(AbyssContext context) {
        chunkInfoMap = new HashMap<>(100);
        chunkEntityMap = new HashMap<>(100);
        configManager = new WorldDataConfigManager(context);

        this.context = context;
        this.manager = context.getWorldManager();
    }

    @Override
    public void loadChunk(Chunk chunk) {
        Collection<ChunkEntity> chunkEntities = configManager.loadChunkData(chunk);
        chunkInfoMap.put(new ChunkKey(chunk), chunkEntities);

        chunkEntities.forEach((a) -> {
            Entity e = a.createEntity(chunk.getWorld());
            chunkEntityMap.put(e.getUniqueId(), a);
        });
    }

    @Override
    public void unloadChunk(Chunk chunk) {
        Collection<ChunkEntity> entities = chunkInfoMap.getOrDefault(new ChunkKey(chunk), Collections.emptyList());
        chunkInfoMap.remove(new ChunkKey(chunk));

        try {
            configManager.saveChunkData(chunk, entities);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to save chunk data for chunk %s_%s", chunk.getX(), chunk.getZ()));
        }

        entities.forEach(a->{
            chunkEntityMap.remove(a.getEntity().getUniqueId());
            a.destroyEntity();
        });
    }

    @Override
    public void addEntity(Chunk chunk, ChunkEntity chunkEntity) {
        ChunkKey key = new ChunkKey(chunk);

        chunkInfoMap.computeIfAbsent(key, (a) -> new ArrayList<>());

        chunkInfoMap.get(key).add(chunkEntity);

        chunkEntityMap.put(chunkEntity.getEntity().getUniqueId(), chunkEntity);
    }

    @Override
    public void removeEntity(Chunk chunk, Entity entity) {
        ChunkEntity e = chunkEntityMap.get(entity.getUniqueId());
        chunkInfoMap.get(new ChunkKey(chunk)).remove(e);
        chunkEntityMap.remove(entity.getUniqueId());
        e.destroyEntity();
    }

    private class ChunkKey {
        int x;
        int z;
        String worldName;

        ChunkKey(Chunk chunk) {
            this.x = chunk.getX();
            this.z = chunk.getZ();

            this.worldName = chunk.getWorld().getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChunkKey)) return false;
            ChunkKey key = (ChunkKey) o;
            return x == key.x &&
                    z == key.z &&
                    Objects.equals(worldName, key.worldName);
        }

        @Override
        public int hashCode() {

            return Objects.hash(x, z, worldName);
        }
    }
}
